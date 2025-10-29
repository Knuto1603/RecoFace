package com.example.recoface.data.local.db

import androidx.room.TypeConverter
import java.nio.ByteBuffer

class Converters {

    @TypeConverter
    fun fromFloatArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4) // 4 bytes por Float
        for (float in floatArray) {
            byteBuffer.putFloat(float)
        }
        return byteBuffer.array()
    }

    @TypeConverter
    fun toFloatArray(byteArray: ByteArray): FloatArray {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        val floatArray = FloatArray(byteArray.size / 4)
        for (i in floatArray.indices) {
            floatArray[i] = byteBuffer.getFloat()
        }
        return floatArray
    }
}