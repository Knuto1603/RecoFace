package com.example.recoface.domain.usecase

import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar los datos de una persona existente.
 * Nota: No permitimos cambiar el embedding aquí, solo los datos personales.
 */
class UpdatePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {

    suspend operator fun invoke(person: Person): Result<Unit> {
        return try {
            // Lógica de negocio: Validar campos
            if (person.dni.isBlank() || person.firstName.isBlank() || person.lastName.isBlank()) {
                throw IllegalArgumentException("Los campos no pueden estar vacíos.")
            }

            // Llama al repositorio para actualizar
            personRepository.updatePerson(person)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}