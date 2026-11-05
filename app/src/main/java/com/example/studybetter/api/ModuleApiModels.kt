package com.example.studybetter.api

data class ModuleRequest(
    val code: String,
    val title: String,
    val description: String
)

data class ModuleResponse(
    val moduleID: Int,
    val code: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String
)
