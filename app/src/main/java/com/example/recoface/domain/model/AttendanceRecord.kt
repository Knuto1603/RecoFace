package com.example.recoface.domain.model

data class AttendanceRecord(
    val id: Int = 0,
    val personId: Int,
    val timestamp: Long,

    val personDni: String? = null,
    val personName: String? = null,
    val confidence: Float
)