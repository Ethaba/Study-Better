package com.example.studybetter

data class Assignment(
    val id: Long,
    val title: String,
    val description: String,
    val module: String,
    val dueDateMillis: Long,
    val priority: String,
    val progress: Int
)
