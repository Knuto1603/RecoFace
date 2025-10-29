package com.example.recoface.domain.model

data class Person(
    val id: Int = 0,
    val dni: String,
    val firstName: String,
    val lastName: String,
    val embedding: FaceEmbedding,
    val facePhotoPath: String
)