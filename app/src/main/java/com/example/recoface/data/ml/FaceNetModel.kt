package com.example.recoface.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import kotlin.math.sqrt

class FaceNetModel(context: Context) {

    private val interpreter: Interpreter
    private val inputSize = 160
    private val embeddingSize = 128

    private val imageProcessor: ImageProcessor

    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "facenet.tflite")
        interpreter = Interpreter(modelBuffer, Interpreter.Options().apply {
            setNumThreads(4)
        })

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            // Usa la normalización [0, 1] que te da mejores resultados
            .add(NormalizeOp(0f, 255f))
            .build()
    }

    fun getEmbedding(image: Bitmap): FloatArray {
        try {
            var tensorImage = TensorImage.fromBitmap(image)
            tensorImage = imageProcessor.process(tensorImage)

            val embeddingBuffer = Array(1) { FloatArray(embeddingSize) }
            interpreter.run(tensorImage.buffer, embeddingBuffer)

            var embedding = embeddingBuffer[0]

            // ✅ SOLUCIÓN: Normalizar el embedding con L2 norm
            embedding = normalizeEmbedding(embedding)

            // LOG para debugging
            Log.d("FaceNetModel", "Embedding normalizado: primeros 5 valores = [${embedding.take(5).joinToString(", ")}]")
            Log.d("FaceNetModel", "Magnitud del embedding (debe ser ~1.0): ${calculateMagnitude(embedding)}")

            return embedding

        } catch (e: Exception) {
            throw RuntimeException("Error al generar embedding: ${e.message}", e)
        }
    }

    /**
     * Normaliza el embedding usando L2 norm.
     * Después de esto, la magnitud del vector será 1.0
     */
    private fun normalizeEmbedding(embedding: FloatArray): FloatArray {
        val magnitude = calculateMagnitude(embedding)

        if (magnitude == 0f) {
            Log.w("FaceNetModel", "Embedding con magnitud 0, devolviendo sin normalizar")
            return embedding
        }

        return embedding.map { it / magnitude }.toFloatArray()
    }

    /**
     * Calcula la magnitud (L2 norm) del vector
     */
    private fun calculateMagnitude(embedding: FloatArray): Float {
        var sum = 0f
        for (value in embedding) {
            sum += value * value
        }
        return sqrt(sum)
    }

    fun close() {
        interpreter.close()
    }
}