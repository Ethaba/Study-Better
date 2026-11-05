
package com.example.studybetter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_resources")
data class AcademicResourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val resourceUrl: String,
    val createdAt: String
)
