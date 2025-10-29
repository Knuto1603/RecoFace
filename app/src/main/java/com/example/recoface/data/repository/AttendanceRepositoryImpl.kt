package com.example.recoface.data.repository

import com.example.recoface.data.local.db.AttendanceDao
import com.example.recoface.data.local.db.PersonDao
import com.example.recoface.data.local.entity.AttendanceEntity
import com.example.recoface.domain.model.AttendanceRecord
import com.example.recoface.domain.model.Person
import com.example.recoface.domain.repository.AttendanceRepository
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val personDao: PersonDao // Opcional, para enriquecer los datos
) : AttendanceRepository {

    override suspend fun markAttendance(person: Person): AttendanceRecord {
        val newRecord = AttendanceEntity(
            personId = person.id,
            timestamp = System.currentTimeMillis()
        )
        attendanceDao.insertRecord(newRecord)

        // Devolvemos el registro creado (Room no devuelve el ID insertado por defecto,
        // así que consultamos el último).
        return attendanceDao.getLastRecordForPerson(person.id)!!.toDomainModel()
    }

    override suspend fun updateAttendance(record: AttendanceRecord) {
        attendanceDao.updateRecord(record.toEntity())
    }

    override suspend fun deleteAttendance(record: AttendanceRecord) {
        attendanceDao.deleteRecord(record.toEntity())
    }

    override suspend fun getAttendanceBetween(startTime: Long, endTime: Long): List<AttendanceRecord> {
        return attendanceDao.getRecordsBetween(startTime, endTime).map { entity ->
            // Enriquecemos el registro con los datos de la persona
            val person = personDao.getPersonById(entity.personId)
            entity.toDomainModel().copy(
                personDni = person?.dni,
                personName = if(person != null) "${person.firstName} ${person.lastName}" else "Desconocido",
            )
        }
    }

    override suspend fun getLastAttendanceFor(person: Person): AttendanceRecord? {
        return attendanceDao.getLastRecordForPerson(person.id)?.toDomainModel()
    }
}