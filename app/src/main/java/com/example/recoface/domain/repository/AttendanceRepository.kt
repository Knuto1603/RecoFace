package com.example.recoface.domain.repository

import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.model.Person

interface AttendanceRepository {

    /**
     * Registra una nueva asistencia para una persona.
     */
    suspend fun markAttendance(person: Person): AttendanceRecord

    suspend fun updateAttendance(record: AttendanceRecord)

    suspend fun deleteAttendance(record: AttendanceRecord)

    /**
     * Obtiene los registros de asistencia entre dos fechas/horas.
     */
    suspend fun getAttendanceBetween(startTime: Long, endTime: Long): List<AttendanceRecord>

    /**
     * Obtiene el último registro de una persona específica.
     */
    suspend fun getLastAttendanceFor(person: Person): AttendanceRecord?
}