package com.example.recoface.data.local

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Guarda un Bitmap en el almacenamiento interno de la app.
     * @param bitmap El bitmap de la cara a guardar.
     * @param dni El DNI de la persona, usado para crear un nombre de archivo único.
     * @return El 'path' absoluto del archivo guardado.
     */
    suspend fun saveFaceBitmap(bitmap: Bitmap, dni: String): String {
        return withContext(Dispatchers.IO) {
            val dir = context.getDir("face_images", Context.MODE_PRIVATE)
            val file = File(dir, "face_$dni.jpg")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            file.absolutePath // Devuelve la ruta completa
        }
    }

    /**
     * Elimina un archivo de imagen del almacenamiento interno.
     * @param path El 'path' absoluto del archivo a borrar.
     */
    suspend fun deleteFaceBitmap(path: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Loggear el error si es necesario
            }
        }
    }
}