package com.example.recoface.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recoface.data.local.entity.AttendanceEntity

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insertRecord(record: AttendanceEntity)

    @Query("SELECT * FROM attendance_records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getRecordsBetween(startTime: Long, endTime: Long): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE personId = :personId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecordForPerson(personId: Int): AttendanceEntity?


    /**
     * Actualiza un registro de asistencia existente.
     * Room usa el 'id' del objeto 'record' para encontrar el registro a actualizar.
     */
    @Update
    suspend fun updateRecord(record: AttendanceEntity)

    /**
     * Elimina un registro de asistencia.
     * Room usa el 'id' del objeto 'record' para encontrar el registro a eliminar.
     */
    @Delete
    suspend fun deleteRecord(record: AttendanceEntity)
}