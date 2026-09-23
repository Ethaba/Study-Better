package com.example.studybetter.api

data class AuthResponse(
    val userID: Int,
    val fullName: String,
    val email: String,
    val token: String,
    val message: String
)