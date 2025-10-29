package com.example.recoface.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ... (imports)
@Entity(
    tableName = "people",
    indices = [Index(value = ["dni"], unique = true)]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val dni: String,
    val firstName: String,
    val lastName: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val embedding: ByteArray,

    val facePhotoPath: String
) {
    // ... (El 'equals' y 'hashCode' necesita ser actualizado)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PersonEntity
        if (id != other.id) return false
        if (dni != other.dni) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (!embedding.contentEquals(other.embedding)) return false
        if (facePhotoPath != other.facePhotoPath) return false // <-- AÑADIDO
        return true
    }
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + dni.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + embedding.contentHashCode()
        result = 31 * result + facePhotoPath.hashCode() // <-- AÑADIDO
        return result
    }
}