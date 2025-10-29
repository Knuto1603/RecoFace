package com.example.recoface.domain.usecase

import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar una persona de la base de datos.
 */
class DeletePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {

    suspend operator fun invoke(person: Person): Result<Unit> {
        return try {
            personRepository.deletePerson(person)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}