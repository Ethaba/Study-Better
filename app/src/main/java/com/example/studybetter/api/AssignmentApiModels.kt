package com.example.studybetter.api

data class AssignmentRequest(
    val title: String,
    val description: String,
    val moduleID: Int?,
    val moduleName: String,
    val dueDateMillis: Long,
    val priority: String,
    val progress: Int
)

data class AssignmentResponse(
    val assignmentID: Int,
    val title: String,
    val description: String,
    val moduleID: Int?,
    val moduleName: String,
    val dueDateMillis: Long,
    val priority: String,
    val progress: Int,
    val createdAt: String,
    val updatedAt: String
)
