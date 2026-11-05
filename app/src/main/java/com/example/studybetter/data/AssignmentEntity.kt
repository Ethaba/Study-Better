
package com.example.studybetter.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "assignments",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class AssignmentEntity(
    val userId: String,
    val id: Long,
    val title: String,
    val description: String,
    val module: String,
    val dueDateMillis: Long,
    val priority: String,
    val progress: Int,
    val syncStatus: String = "SYNCED",
    val isDeleted: Boolean = false
)
