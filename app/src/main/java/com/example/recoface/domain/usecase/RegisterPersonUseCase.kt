package com.example.recoface.domain.usecase

import android.graphics.Bitmap // <-- 1. IMPORTAR BITMAP
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Caso de uso para registrar una nueva persona.
 * Su única responsabilidad es manejar la lógica de negocio para un nuevo registro.
 */
class RegisterPersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {

    /**
     * Ejecuta el caso de uso.
     * @return Un 'Result' que indica éxito o un error (ej. DNI duplicado).
     */
    suspend operator fun invoke(
        dni: String,
        firstName: String,
        lastName: String,
        embedding: FaceEmbedding,
        faceBitmap: Bitmap
    ): Result<Unit> {
        return try {
            // Lógica de negocio: Verificar si el DNI ya existe
            if (personRepository.getPersonByDni(dni) != null) {
                throw DniAlreadyExistsException("El DNI $dni ya está registrado.")
            }

            // Lógica de negocio: Validar campos (simplificado)
            if (dni.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                throw IllegalArgumentException("Los campos no pueden estar vacíos.")
            }

            // Si todo está bien, registrar
            // 3. PASAR EL 'faceBitmap' AL REPOSITORIO
            personRepository.registerPerson(
                dni = dni,
                firstName = firstName,
                lastName = lastName,
                embedding = embedding,
                faceBitmap = faceBitmap // <-- ACTUALIZADO
            )

            Result.success(Unit)

        } catch (e: Exception) {
            // Capturar excepciones personalizadas o de la BD
            Result.failure(e)
        }
    }
}

// Es una buena práctica definir excepciones personalizadas
class DniAlreadyExistsException(message: String) : Exception(message)