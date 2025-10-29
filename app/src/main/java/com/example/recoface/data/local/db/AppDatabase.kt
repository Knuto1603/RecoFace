package com.example.recoface.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recoface.data.local.entity.AttendanceEntity
import com.example.recoface.data.local.entity.PersonEntity

@Database(
    entities = [PersonEntity::class, AttendanceEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao
    abstract fun attendanceDao(): AttendanceDao
}