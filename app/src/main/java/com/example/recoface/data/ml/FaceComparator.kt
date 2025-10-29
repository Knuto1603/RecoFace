package com.example.recoface.data.ml

import android.util.Log
import javax.inject.Inject
import kotlin.math.sqrt

class FaceComparator @Inject constructor() {

    companion object {
        // Threshold para distancia coseno (rango: 0 a 2)
        // 0.0 = idénticos, 1.0 = ortogonales, 2.0 = opuestos
        private const val MATCH_THRESHOLD = 0.6f // Usa 0.4 - 0.7 para distancia coseno
    }

    fun getThreshold(): Float = MATCH_THRESHOLD

    fun areSimilar(embedding1: FloatArray, embedding2: FloatArray): Boolean {
        val distance = calculateCosineDistance(embedding1, embedding2)

        Log.d("FaceComparator", "Distancia coseno: $distance | Threshold: $MATCH_THRESHOLD | Match: ${distance < MATCH_THRESHOLD}")

        return distance < MATCH_THRESHOLD
    }

    /**
     * Distancia Euclidiana L2 (método original)
     */
    fun calculateDistance(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.size != emb2.size) {
            Log.w("FaceComparator", "Embeddings de tamaño diferente: ${emb1.size} vs ${emb2.size}")
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
     * Distancia Coseno: 1 - similitud_coseno
     * Rango: 0 (idénticos) a 2 (opuestos)
     * Más robusto para embeddings normalizados
     */
    fun calculateCosineDistance(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.size != emb2.size) {
            Log.w("FaceComparator", "Embeddings de tamaño diferente: ${emb1.size} vs ${emb2.size}")
            return Float.MAX_VALUE
        }

        // Calcular producto punto
        var dotProduct = 0f
        var magnitude1 = 0f
        var magnitude2 = 0f

        for (i in emb1.indices) {
            dotProduct += emb1[i] * emb2[i]
            magnitude1 += emb1[i] * emb1[i]
            magnitude2 += emb2[i] * emb2[i]
        }

        magnitude1 = sqrt(magnitude1)
        magnitude2 = sqrt(magnitude2)

        if (magnitude1 == 0f || magnitude2 == 0f) {
            Log.w("FaceComparator", "Embedding con magnitud 0")
            return Float.MAX_VALUE
        }

        // Similitud coseno: dot / (mag1 * mag2)
        val cosineSimilarity = dotProduct / (magnitude1 * magnitude2)

        // Convertir a distancia: 1 - similitud (rango 0 a 2)
        val cosineDistance = 1f - cosineSimilarity

        Log.d("FaceComparator", "Similitud coseno: $cosineSimilarity | Distancia: $cosineDistance")

        return cosineDistance.coerceIn(0f, 2f)
    }

    fun calculateSimilarityPercentage(emb1: FloatArray, emb2: FloatArray): Float {
        val distance = calculateCosineDistance(emb1, emb2)
        // Para distancia coseno: 0 = 100%, 0.6 (threshold) = 0%
        val similarity = kotlin.math.max(0f, (1f - (distance / MATCH_THRESHOLD)) * 100f)
        return kotlin.math.min(100f, similarity)
    }
}