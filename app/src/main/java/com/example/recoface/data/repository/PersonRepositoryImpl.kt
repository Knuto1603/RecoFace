package com.example.recoface.data.repository

import android.graphics.Bitmap
import android.util.Log // <-- ✅ 1. ASEGÚRATE DE IMPORTAR Log
import com.example.recoface.data.local.FileStorageHelper
import com.example.recoface.data.local.db.PersonDao
import com.example.recoface.data.local.entity.PersonEntity
import com.example.recoface.data.ml.FaceComparator
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.PersonRepository
import com.example.recoface.utils.floatArrayToByteArray
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao,
    private val faceComparator: FaceComparator,
    private val fileStorage: FileStorageHelper
) : PersonRepository {

    override suspend fun registerPerson(
        dni: String,
        firstName: String,
        lastName: String,
        embedding: FaceEmbedding,
        faceBitmap: Bitmap
    ) {
        val photoPath = fileStorage.saveFaceBitmap(faceBitmap, dni)
        val personEntity = PersonEntity(
            dni = dni,
            firstName = firstName,
            lastName = lastName,
            embedding = floatArrayToByteArray(embedding.value),
            facePhotoPath = photoPath
        )
        personDao.insertPerson(personEntity)
    }

    override suspend fun updatePerson(person: Person) {
        personDao.updatePerson(person.toEntity())
    }

    override suspend fun deletePerson(person: Person) {
        try {
            fileStorage.deleteFaceBitmap(person.facePhotoPath)
        } catch (e: Exception) {
            Log.e("PersonRepo", "Error al borrar la foto: ${person.facePhotoPath}", e)
        }
        personDao.deletePerson(person.toEntity())
    }

    override suspend fun getPersonByDni(dni: String): Person? {
        return personDao.getPersonByDni(dni)?.toDomainModel()
    }

    override suspend fun getAllPeople(): List<Person> {
        return personDao.getAllPeople().map { it.toDomainModel() }
    }

    override suspend fun getPersonById(id: Int): Person? {
        return personDao.getPersonById(id)?.toDomainModel()
    }

    override suspend fun findClosestPerson(embedding: FaceEmbedding): Pair<Person, Float>? {
        val allPeople = personDao.getAllPeople()
        var closestMatch: Person? = null
        var minDistance = Float.MAX_VALUE

        // --- ✅ LOGS AÑADIDOS ---
        Log.d("FaceRepo", "--- Iniciando Búsqueda ---")
        Log.d("FaceRepo", "Personas en BD: ${allPeople.size}")
        // --- FIN LOGS AÑADIDOS ---

        for (personEntity in allPeople) {
            val person = personEntity.toDomainModel()
            val distance = faceComparator.calculateDistance(
                embedding.value,
                person.embedding.value
            )

            // --- ✅ LOGS AÑADIDOS ---
            Log.d("FaceRepo", "Comparando con ${person.firstName} (ID: ${person.id}). Distancia: $distance")
            // --- FIN LOGS AÑADIDOS ---

            if (distance < minDistance) {
                minDistance = distance
                closestMatch = person
            }
        }

        // --- ✅ LOGS AÑADIDOS ---
        val threshold = faceComparator.getThreshold() // Asegúrate que FaceComparator tiene getThreshold()
        Log.d("FaceRepo", "Mejor coincidencia: ${closestMatch?.firstName} (ID: ${closestMatch?.id}) (Distancia: $minDistance / Umbral: $threshold)")
        // --- FIN LOGS AÑADIDOS ---

        if (closestMatch != null && minDistance < threshold) {
            Log.d("FaceRepo", "--- Búsqueda Exitosa ---") // Log de éxito
            return Pair(closestMatch, minDistance)
        }

        // --- ✅ LOGS AÑADIDOS ---
        Log.d("FaceRepo", "--- Búsqueda fallida (No pasó el umbral o no hay personas) ---")
        // --- FIN LOGS AÑADIDOS ---
        return null
    }
}