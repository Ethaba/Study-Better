package com.example.studybetter.api

data class StudySessionRequest(
    val startedAt: String,
    val durationSeconds: Int,
    val description: String
)

data class StudySessionResponse(
    val studySessionID: Int,
    val startedAt: String,
    val durationSeconds: Int,
    val description: String,
    val createdAt: String
)