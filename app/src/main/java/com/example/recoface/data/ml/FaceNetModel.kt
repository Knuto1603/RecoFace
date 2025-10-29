package com.example.recoface.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class FaceNetModel(context: Context) {

    private val interpreter: Interpreter

    // Tamaño de entrada del modelo (160x160 es estándar para FaceNet)
    private val inputSize = 160

    // Tamaño del embedding de salida (128 o 512 típicamente)
    private val embeddingSize = 128

    private val imageProcessor: ImageProcessor

    init {
        // Cargar el modelo desde assets
        val modelBuffer = FileUtil.loadMappedFile(context, "facenet.tflite")

        // Configurar opciones del intérprete (opcional: agregar delegates para GPU)
        val options = Interpreter.Options().apply {
            setNumThreads(4) // Usar 4 threads para mejor rendimiento
            // setUseNNAPI(true) // Descomentar si quieres usar NNAPI (aceleración hardware)
        }
        interpreter = Interpreter(modelBuffer, options)

        // Configurar el procesador de imágenes
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            // IMPORTANTE: Verifica qué normalización necesita tu modelo específico
            // Opción 1: Rango [0, 1] - Común en algunos modelos
            .add(NormalizeOp(0f, 255f)) // (pixel - 0) / 255 = [0, 1]
            // Opción 2: Rango [-1, 1] - Más común en FaceNet original
            // .add(NormalizeOp(127.5f, 127.5f)) // (pixel - 127.5) / 127.5 = [-1, 1]
            .build()
    }

    /**
     * Toma un Bitmap (la cara recortada) y devuelve su embedding (vector de características).
     *
     * @param image Bitmap de la cara detectada
     * @return FloatArray con el embedding de tamaño [embeddingSize]
     */
    fun getEmbedding(image: Bitmap): FloatArray {
        try {
            // 1. Pre-procesar la imagen
            var tensorImage = TensorImage.fromBitmap(image)
            tensorImage = imageProcessor.process(tensorImage)

            // 2. Preparar el buffer de salida
            // Forma: [1, embeddingSize] - batch de 1 con un vector de características
            val embeddingBuffer = Array(1) { FloatArray(embeddingSize) }

            // 3. Ejecutar la inferencia
            interpreter.run(tensorImage.buffer, embeddingBuffer)

            // 4. Devolver el embedding
            return embeddingBuffer[0]

        } catch (e: Exception) {
            // Manejar errores de inferencia
            throw RuntimeException("Error al generar embedding: ${e.message}", e)
        }
    }

    /**
     * Libera los recursos del intérprete.
     * Llamar cuando ya no se necesite el modelo (por ejemplo, en onDestroy).
     */
    fun close() {
        interpreter.close()
    }
}