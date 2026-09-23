package com.example.studybetter.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StudyBetterApi {

    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
}