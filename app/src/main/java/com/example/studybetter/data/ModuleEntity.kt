package com.example.studybetter.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "modules",
    primaryKeys = ["userId", "id"],
    indices = [Index(value = ["userId"])]
)
data class ModuleEntity(
    val userId: String,
    val id: Long,
    val code: String,
    val title: String,
    val description: String,
    val syncStatus: String = "SYNCED",
    val isDeleted: Boolean = false
)