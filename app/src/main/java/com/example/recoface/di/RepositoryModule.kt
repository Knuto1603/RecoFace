package com.example.recoface.di

import com.example.recoface.data.repository.AttendanceRepositoryImpl
import com.example.recoface.data.repository.PersonRepositoryImpl
import com.example.recoface.domain.repository.AttendanceRepository
import com.example.recoface.domain.repository.PersonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPersonRepository(
        impl: PersonRepositoryImpl // Hilt sabe construir esta (porque tiene @Inject constructor)
    ): PersonRepository // <-- Cuando alguien pida esto... Hilt le dará esto ^

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        impl: AttendanceRepositoryImpl
    ): AttendanceRepository
}