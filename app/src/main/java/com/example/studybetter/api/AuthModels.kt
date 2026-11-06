
package com.example.studybetter.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class AuthResponse(
    val userID: Int,
    val fullName: String,
    val email: String,
    val token: String,
    val message: String
)
