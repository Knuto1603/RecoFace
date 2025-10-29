package com.example.recoface.data.repository

import com.example.recoface.data.local.entity.AttendanceEntity
import com.example.recoface.data.local.entity.PersonEntity
import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.model.Person
import com.example.recoface.utils.byteArrayToFloatArray
import com.example.recoface.utils.floatArrayToByteArray

// --- Mappers de Persona ---
fun PersonEntity.toDomainModel(): Person {
    return Person(
        id = this.id,
        dni = this.dni,
        firstName = this.firstName,
        lastName = this.lastName,
        embedding = FaceEmbedding(byteArrayToFloatArray(this.embedding)),
        facePhotoPath = this.facePhotoPath
    )
}

fun Person.toEntity(): PersonEntity {
    return PersonEntity(
        id = this.id,
        dni = this.dni,
        firstName = this.firstName,
        lastName = this.lastName,
        embedding = floatArrayToByteArray(this.embedding.value),
        facePhotoPath = this.facePhotoPath
    )
}

// --- Mappers de Asistencia ---

fun AttendanceEntity.toDomainModel(): AttendanceRecord {
    return AttendanceRecord(
        id = this.id,
        personId = this.personId,
        personDni = "", // Se llena después si es necesario
        personName = "", // Se llena después si es necesario
        timestamp = this.timestamp,
        confidence = 0f // ✅ Valor por defecto - no se guarda en BD
    )
}

fun AttendanceRecord.toEntity(): AttendanceEntity {
    return AttendanceEntity(
        id = this.id,
        personId = this.personId,
        timestamp = this.timestamp
        // ✅ No incluimos confidence - es un dato temporal
    )
}