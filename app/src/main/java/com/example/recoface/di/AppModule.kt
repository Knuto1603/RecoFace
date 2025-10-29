package com.example.recoface.di

import android.content.Context
import com.example.recoface.data.ml.FaceNetModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton // El modelo TFLite es pesado, lo creamos una sola vez
    fun provideFaceNetModel(@ApplicationContext context: Context): FaceNetModel {
        return FaceNetModel(context)
    }

    // Nota: No necesitamos proveer 'FaceComparator'
    // porque ya tiene la anotación '@Inject constructor()'.
    // Hilt sabe cómo construirlo automáticamente.
}