package com.example.recoface.data.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.example.recoface.utils.toBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

/**
 * Resultado del análisis de una cara detectada
 */
data class AnalyzedFace(
    val boundingBox: Rect,
    val embedding: FloatArray,
    val bitmap: Bitmap
) {
    // Override equals y hashCode para comparación correcta (por el FloatArray)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AnalyzedFace
        if (boundingBox != other.boundingBox) return false
        if (!embedding.contentEquals(other.embedding)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = boundingBox.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}

class FaceAnalyzer(
    private val faceNetModel: FaceNetModel,
    private val onResults: (List<AnalyzedFace>) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: FaceDetector

    // Usar SupervisorJob para que un fallo no cancele todo el scope
    private val analysisScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Flag para evitar procesar múltiples frames simultáneamente
    @Volatile
    private var isProcessing = false

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f) // Detectar caras que ocupen al menos 15% del frame
            .build()
        detector = FaceDetection.getClient(options)
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Evitar procesar si ya hay un análisis en curso
        if (isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true

        analysisScope.launch {
            try {
                val rotatedBitmap = convertAndRotateBitmap(imageProxy)
                if (rotatedBitmap == null) {
                    Log.w("FaceAnalyzer", "No se pudo convertir la imagen")
                    return@launch
                }

                val faces = detectFaces(rotatedBitmap)

                if (faces.isEmpty()) {
                    // Notificar que no se detectaron caras
                    withContext(Dispatchers.Main) {
                        onResults(emptyList())
                    }
                    return@launch
                }

                val results = faces.mapNotNull { face ->
                    try {
                        val faceBitmap = cropFace(rotatedBitmap, face)
                        val embedding = faceNetModel.getEmbedding(faceBitmap)
                        AnalyzedFace(
                            boundingBox = face.boundingBox,
                            embedding = embedding,
                            bitmap = faceBitmap
                        )
                    } catch (e: Exception) {
                        Log.e("FaceAnalyzer", "Error al procesar cara individual", e)
                        null // Omitir esta cara si hay error
                    }
                }

                // Notificar resultados en el hilo principal
                withContext(Dispatchers.Main) {
                    onResults(results)
                }

            } catch (e: Exception) {
                Log.e("FaceAnalyzer", "Error en análisis de frame", e)
            } finally {
                imageProxy.close()
                isProcessing = false
            }
        }
    }

    private fun convertAndRotateBitmap(imageProxy: ImageProxy): Bitmap? {
        val bitmap = imageProxy.toBitmap() ?: return null

        // Optimización: solo rotar si es necesario
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Error al rotar bitmap", e)
            bitmap // Devolver sin rotar si hay error
        }
    }

    private suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        return try {
            detector.process(InputImage.fromBitmap(bitmap, 0)).await()
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Error en detección de caras", e)
            emptyList()
        }
    }

    private fun cropFace(bitmap: Bitmap, face: Face): Bitmap {
        val rect = face.boundingBox

        // Agregar padding del 20% (más generoso para capturar toda la cara)
        val padding = (rect.width() * 0.2f).toInt()

        val left = max(0, rect.left - padding)
        val top = max(0, rect.top - padding)
        val right = min(bitmap.width, rect.right + padding)
        val bottom = min(bitmap.height, rect.bottom + padding)

        val width = right - left
        val height = bottom - top

        return try {
            Bitmap.createBitmap(bitmap, left, top, width, height)
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "Error al recortar cara", e)
            // Fallback: devolver bitmap completo
            bitmap
        }
    }

    /**
     * Libera recursos. Llamar cuando ya no se necesite el analyzer.
     */
    fun close() {
        analysisScope.cancel()
        detector.close()
    }
}