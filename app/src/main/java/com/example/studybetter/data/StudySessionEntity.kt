
package com.example.studybetter.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,

    val userId: String,
    val serverId: Int? = null,
    val startedAt: String,
    val durationSeconds: Int,
    val description: String,

    // PENDING means an operation still needs synchronisation.
    // SYNCED means the backend has received the session.
    val syncStatus: String = "PENDING",

    // True means the session must be deleted from the server.
    val isDeleted: Boolean = false
)
