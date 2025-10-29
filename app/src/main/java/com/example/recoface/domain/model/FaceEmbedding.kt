package com.example.recoface.domain.model

data class FaceEmbedding(val value: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceEmbedding

        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {

        return value.contentHashCode()
    }
}