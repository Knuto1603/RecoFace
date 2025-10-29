package com.example.recoface.domain.usecase

import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.repository.AttendanceRepository
import java.util.Calendar
import javax.inject.Inject

/**
 * Caso de uso para obtener los registros de asistencia de un día específico.
 */
class GetAttendanceReportUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {

    /**
     * Obtiene los registros de asistencia para un día específico.
     * @param day Un objeto 'Calendar' que representa el día a consultar.
     */
    suspend operator fun invoke(day: Calendar): Result<List<AttendanceRecord>> {
        return try {
            // Configurar inicio del día
            val startTime = day.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Configurar fin del día
            val endTime = day.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val records = attendanceRepository.getAttendanceBetween(startTime, endTime)
            Result.success(records)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}