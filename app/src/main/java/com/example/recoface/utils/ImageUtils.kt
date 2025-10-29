package com.example.recoface.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Convierte un ImageProxy (formato YUV_420_888 de CameraX) a un Bitmap.
 *
 * Esta es una operación de conversión necesaria pero costosa.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun ImageProxy.toBitmap(): Bitmap? {
    val image = this.image ?: return null

    try {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // El formato NV21 requiere los planos Y, V, U en ese orden.
        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copiar Y
        yBuffer.get(nv21, 0, ySize)
        // Copiar V (antes que U)
        vBuffer.get(nv21, ySize, vSize)
        // Copiar U
        uBuffer.get(nv21, ySize + vSize, uSize)

        // Convertir YUV a JPG
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)

        // Convertir JPG (en ByteArray) a Bitmap
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    } catch (e: Exception) {
        Log.e("ImageUtils", "Error al convertir ImageProxy a Bitmap", e)
        return null
    }
}

/**
 * Convierte un ByteArray (leído de la base de datos) de nuevo a un FloatArray.
 */
fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
    val byteBuffer = ByteBuffer.wrap(byteArray)
    // Se divide por 4 porque cada Float ocupa 4 bytes
    val floatArray = FloatArray(byteArray.size / 4)
    for (i in floatArray.indices) {
        floatArray[i] = byteBuffer.getFloat()
    }
    return floatArray
}

/**
 * Convierte un FloatArray (del modelo TFLite) a un ByteArray (para guardar en la BD).
 */
fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
    // Se multiplica por 4 porque cada Float ocupa 4 bytes
    val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
    for (float in floatArray) {
        byteBuffer.putFloat(float)
    }
    return byteBuffer.array()
}