
package com.example.studybetter.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import okhttp3.ResponseBody

interface StudyBetterApi {

    // Authentication

    @POST("api/Auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>


    @POST("api/Auth/google")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): Response<AuthResponse>



    // Modules

    @GET("api/Modules")
    suspend fun getModules(): Response<List<ModuleResponse>>

    @GET("api/Modules/{id}")
    suspend fun getModule(
        @Path("id") id: Int
    ): Response<ModuleResponse>

    @POST("api/Modules")
    suspend fun createModule(
        @Body request: ModuleRequest
    ): Response<ModuleResponse>

    @PUT("api/Modules/{id}")
    suspend fun updateModule(
        @Path("id") id: Int,
        @Body request: ModuleRequest
    ): Response<ModuleResponse>

    @DELETE("api/Modules/{id}")
    suspend fun deleteModule(
        @Path("id") id: Int
    ): Response<Unit>

    //Assignment
    @GET("api/Assignments")
    suspend fun getAssignments(): Response<List<AssignmentResponse>>

    @GET("api/Assignments/{id}")
    suspend fun getAssignment(
        @Path("id") id: Int
    ): Response<AssignmentResponse>

    @POST("api/Assignments")
    suspend fun createAssignment(
        @Body request: AssignmentRequest
    ): Response<AssignmentResponse>

    @PUT("api/Assignments/{id}")
    suspend fun updateAssignment(
        @Path("id") id: Int,
        @Body request: AssignmentRequest
    ): Response<AssignmentResponse>

    @DELETE("api/Assignments/{id}")
    suspend fun deleteAssignment(
        @Path("id") id: Int
    ): Response<okhttp3.ResponseBody>

    // StudySessions
    @GET("api/StudySessions")
    fun getStudySessions(): Call<List<StudySessionResponse>>

    @POST("api/StudySessions")
    fun createStudySession(
        @Body request: StudySessionRequest
    ): Call<StudySessionResponse>

    @DELETE("api/StudySessions/{id}")
    fun deleteStudySession(
        @Path("id") id: Int
    ): Call<ResponseBody>
}
