package com.example.recoface.data.local.db


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recoface.data.local.entity.PersonEntity

@Dao
interface PersonDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerson(person: PersonEntity)

    @Query("SELECT * FROM people")
    suspend fun getAllPeople(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Int): PersonEntity?

    @Query("SELECT * FROM people WHERE dni = :dni LIMIT 1")
    suspend fun getPersonByDni(dni: String): PersonEntity?

    /**
     * Actualiza la información de una persona en la base de datos.
     * Room usa el 'id' del objeto 'person' para encontrar el registro a actualizar.
     */
    @Update
    suspend fun updatePerson(person: PersonEntity)

    /**
     * Elimina una persona de la base de datos.
     * Room usa el 'id' del objeto 'person' para encontrar el registro a eliminar.
     */
    @Delete
    suspend fun deletePerson(person: PersonEntity)
}