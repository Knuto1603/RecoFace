package com.example.recoface.domain.usecase

import android.util.Log
import com.example.recoface.data.ml.FaceComparator
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
    private val attendanceRepository: AttendanceRepository,
    private val faceComparator: FaceComparator // ✅ Inyectar FaceComparator
) {

    companion object {
        private const val TAG = "CheckInUseCase"
        // Tiempo de espera (cooldown) en milisegundos (ej. 5 minutos)
        private const val CHECK_IN_COOLDOWN = 5 * 60 * 1000L
    }

    /**
     * Intenta marcar la asistencia usando un embedding facial.
     * @return Un 'Result' con el registro de asistencia si fue exitoso,
     * o un error si no se encontró la persona o ya marcó.
     */
    suspend operator fun invoke(embedding: FaceEmbedding): Result<AttendanceRecord> {
        return try {
            // 1. Obtener todas las personas registradas
            val allPersons = personRepository.getAllPeople()

            if (allPersons.isEmpty()) {
                Log.w(TAG, "No hay personas registradas en el sistema")
                return Result.failure(PersonNotFoundException("No hay personas registradas"))
            }

            Log.d(TAG, "Comparando rostro con ${allPersons.size} personas registradas")

            // 2. Buscar la mejor coincidencia
            var bestMatch: Triple<String, Float, String>? = null // personId, distance, name

            for (person in allPersons) {
                // ✅ Usar distancia coseno (más robusta)
                val distance = faceComparator.calculateCosineDistance(
                    embedding.value,
                    person.embedding.value
                )

                val fullName = "${person.firstName} ${person.lastName}"
                Log.d(TAG, "Comparando con $fullName | Distancia: %.4f".format(distance))

                if (bestMatch == null || distance < bestMatch.second) {
                    bestMatch = Triple(person.id.toString(), distance, fullName)
                }
            }

            if (bestMatch == null) {
                Log.e(TAG, "Error al procesar comparaciones")
                return Result.failure(Exception("Error al comparar rostros"))
            }

            val (matchedPersonId, distance, personName) = bestMatch
            val threshold = faceComparator.getThreshold()

            Log.d(TAG, "Mejor coincidencia: $personName")
            Log.d(TAG, "Distancia: %.4f | Threshold: %.4f | ¿Match?: ${distance < threshold}".format(distance, threshold))

            // 3. Verificar si la distancia está dentro del threshold
            if (distance >= threshold) {
                val message = "Persona no reconocida\n" +
                        "Mejor coincidencia: $personName (%.1f%% de confianza)\n" +
                        "Distancia: %.4f (threshold: %.4f)".format(
                            calculateConfidence(distance, threshold),
                            distance,
                            threshold
                        )
                Log.w(TAG, message)
                return Result.failure(PersonNotFoundException(message))
            }

            // 4. Obtener la persona reconocida
            val person = allPersons.first { it.id == matchedPersonId.toInt() }
            Log.i(TAG, "Persona reconocida: ${person.firstName} ${person.lastName} con ${calculateConfidence(distance, threshold)}% de confianza")

            // 5. Verificar cooldown
            val lastRecord = attendanceRepository.getLastAttendanceFor(person)
            if (lastRecord != null) {
                val timeSinceLastCheck = System.currentTimeMillis() - lastRecord.timestamp
                if (timeSinceLastCheck < CHECK_IN_COOLDOWN) {
                    val remainingSeconds = (CHECK_IN_COOLDOWN - timeSinceLastCheck) / 1000
                    val message = "Ya marcó asistencia. Intente de nuevo en $remainingSeconds segundos."
                    Log.w(TAG, message)
                    throw AlreadyCheckedInException(message)
                }
            }

            // 6. Marcar la asistencia
            val newRecord = attendanceRepository.markAttendance(person)

            // 7. Devolver el registro enriquecido
            val enrichedRecord = newRecord.copy(
                personDni = person.dni,
                personName = "${person.firstName} ${person.lastName}",
                confidence = calculateConfidence(distance, threshold)
            )

            Log.i(TAG, "Check-in exitoso para ${enrichedRecord.personName}")
            Result.success(enrichedRecord)

        } catch (e: AlreadyCheckedInException) {
            Log.w(TAG, "Intento de check-in duplicado: ${e.message}")
            Result.failure(e)
        } catch (e: PersonNotFoundException) {
            Log.w(TAG, "Persona no reconocida: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error en check-in", e)
            Result.failure(Exception("Error al procesar check-in: ${e.message}"))
        }
    }

    /**
     * Calcula el porcentaje de confianza basado en la distancia.
     * 0% = threshold (no match), 100% = distancia 0 (match perfecto)
     */
    private fun calculateConfidence(distance: Float, threshold: Float): Float {
        val confidence = ((1f - (distance / threshold)) * 100f).coerceIn(0f, 100f)
        return confidence
    }
}

class PersonNotFoundException(message: String) : Exception(message)
class AlreadyCheckedInException(message: String) : Exception(message)