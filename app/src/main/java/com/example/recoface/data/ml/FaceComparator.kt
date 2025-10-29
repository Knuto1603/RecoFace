package com.example.recoface.data.ml

import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FaceComparator @Inject constructor() {

    companion object {
        // Threshold típico para FaceNet:
        // < 1.0 = match muy confiable
        // 1.0 - 1.2 = match probable
        // 1.2 - 1.4 = match dudoso
        // > 1.4 = no match
        private const val MATCH_THRESHOLD = 1.0f
    }

    fun getThreshold(): Float = MATCH_THRESHOLD

    /**
     * Compara dos embeddings y determina si pertenecen a la misma persona.
     * @return true si la distancia es menor al threshold (son similares)
     */
    fun areSimilar(embedding1: FloatArray, embedding2: FloatArray): Boolean {
        return calculateDistance(embedding1, embedding2) < MATCH_THRESHOLD
    }

    /**
     * Calcula la distancia Euclidiana L2 entre dos vectores.
     * Distancias más pequeñas = caras más similares.
     */
    fun calculateDistance(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.size != emb2.size) {
            //Log.w("FaceComparator", "Embeddings de tamaño diferente: ${emb1.size} vs ${emb2.size}")
            return Float.MAX_VALUE
        }

        var sum = 0.0f
        for (i in emb1.indices) {
            val diff = emb1[i] - emb2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    /**
     * Calcula la similitud como porcentaje (0-100%).
     * Útil para mostrar en la UI.
     */
    fun calculateSimilarityPercentage(emb1: FloatArray, emb2: FloatArray): Float {
        val distance = calculateDistance(emb1, emb2)
        // Convertir distancia a porcentaje (0 = 100%, threshold = 0%)
        val similarity = max(0f, (1f - (distance / MATCH_THRESHOLD)) * 100f)
        return min(100f, similarity)
    }
}