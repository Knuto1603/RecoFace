package com.example.recoface.di

import android.content.Context
import androidx.room.Room
import com.example.recoface.data.local.db.AppDatabase
import com.example.recoface.data.local.db.AttendanceDao
import com.example.recoface.data.local.db.PersonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Esto hace que las dependencias vivan durante toda la app
object DatabaseModule {

    @Provides
    @Singleton // Creamos una sola instancia de la BD para toda la app
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "recoface_db" // Nombre del archivo de la base de datos
        ).build()
    }

    @Provides
    fun providePersonDao(db: AppDatabase): PersonDao {
        return db.personDao() // Hilt sabe cómo proveer 'db' gracias a la función de arriba
    }

    @Provides
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao {
        return db.attendanceDao()
    }
}