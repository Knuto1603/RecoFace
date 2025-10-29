package com.example.recoface.domain.usecase

import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.model.FaceEmbedding
import com.example.recoface.domain.repository.AttendanceRepository
import com.example.recoface.domain.repository.PersonRepository
import javax.inject.Inject

/**
 * Caso de uso para marcar la asistencia (Check-In).
 * Contiene la lógica de negocio para validar un marcado.
 */
class CheckInUseCase @Inject constructor(
    private val personRepository: PersonRepository,
    private val attendanceRepository: AttendanceRepository
) {

    // Tiempo de espera (cooldown) en milisegundos (ej. 5 minutos)
    private val CHECK_IN_COOLDOWN = 5 * 60 * 1000

    /**
     * Intenta marcar la asistencia usando un embedding facial.
     * @return Un 'Result' con el registro de asistencia si fue exitoso,
     * o un error si no se encontró la persona o ya marcó.
     */
    suspend operator fun invoke(embedding: FaceEmbedding): Result<AttendanceRecord> {
        return try {
            // 1. Encontrar a la persona por su rostro
            val match = personRepository.findClosestPerson(embedding)
                ?: throw PersonNotFoundException("Persona no reconocida.")

            val person = match.first

            // 2. Lógica de negocio: Verificar el 'cooldown'
            val lastRecord = attendanceRepository.getLastAttendanceFor(person)
            if (lastRecord != null) {
                val timeSinceLastCheck = System.currentTimeMillis() - lastRecord.timestamp
                if (timeSinceLastCheck < CHECK_IN_COOLDOWN) {
                    val remainingSeconds = (CHECK_IN_COOLDOWN - timeSinceLastCheck) / 1000
                    throw AlreadyCheckedInException("Ya marcó. Intente de nuevo en $remainingSeconds seg.")
                }
            }

            // 3. Marcar la asistencia
            val newRecord = attendanceRepository.markAttendance(person)

            // 4. Devolver el registro enriquecido (con el nombre)
            val enrichedRecord = newRecord.copy(
                personDni = person.dni,
                personName = "${person.firstName} ${person.lastName}"
            )

            Result.success(enrichedRecord)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class PersonNotFoundException(message: String) : Exception(message)
class AlreadyCheckedInException(message: String) : Exception(message)