
package com.example.studybetter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AcademicResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: AcademicResourceEntity)

    @Query("""
        SELECT * FROM academic_resources
        WHERE userId = :userId
        ORDER BY createdAt DESC
    """)
    suspend fun getResources(userId: String): List<AcademicResourceEntity>

    @Query("""
        SELECT * FROM academic_resources
        WHERE userId = :userId
        AND (
            title LIKE '%' || :searchQuery || '%'
            OR description LIKE '%' || :searchQuery || '%'
            OR category LIKE '%' || :searchQuery || '%'
        )
        ORDER BY createdAt DESC
    """)
    suspend fun searchResources(
        userId: String,
        searchQuery: String
    ): List<AcademicResourceEntity>

    @Query("""
        DELETE FROM academic_resources
        WHERE id = :resourceId AND userId = :userId
    """)
    suspend fun deleteResource(resourceId: Int, userId: String)

    @Query("DELETE FROM academic_resources WHERE userId = :userId")
    suspend fun deleteAllResources(userId: String)
}
