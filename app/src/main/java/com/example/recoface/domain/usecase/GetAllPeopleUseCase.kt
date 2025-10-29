package com.example.recoface.domain.usecase

import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener una lista de todas las personas registradas.
 * Tiene una única responsabilidad: consultar al repositorio por todas las personas.
 */
class GetAllPeopleUseCase @Inject constructor(
    private val personRepository: PersonRepository // Inyecta la interfaz, no la implementación
) {

    /**
     * Ejecuta el caso de uso como si fuera una función.
     * @return Un 'Result' que contiene la lista de 'Person' en caso de éxito,
     * o una excepción en caso de fallo.
     */
    suspend operator fun invoke(): Result<List<Person>> {
        return try {
            // Llama al método del repositorio
            val peopleList = personRepository.getAllPeople()
            // Envuelve el resultado exitoso
            Result.success(peopleList)
        } catch (e: Exception) {
            // Captura cualquier error de la base de datos y lo envuelve
            Result.failure(e)
        }
    }
}