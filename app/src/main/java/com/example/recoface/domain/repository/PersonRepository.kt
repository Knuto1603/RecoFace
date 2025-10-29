package com.example.recoface.domain.repository

import android.graphics.Bitmap
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.model.Person

interface PersonRepository {

    suspend fun registerPerson(
        dni: String,
        firstName: String,
        lastName: String,
        embedding: FaceEmbedding,
        faceBitmap: Bitmap
    )

    suspend fun updatePerson(person: Person)

    suspend fun deletePerson(person: Person)

    suspend fun getPersonByDni(dni: String): Person?

    suspend fun getAllPeople(): List<Person>

    suspend fun getPersonById(id: Int): Person?

    /**
     * Busca en toda la BD la persona cuyo embedding sea más cercano.
     * @return La 'Persona' y la 'distancia' (similitud) si se encuentra.
     */
    suspend fun findClosestPerson(embedding: FaceEmbedding): Pair<Person, Float>?
}