
package com.example.studybetter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AssignmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    // Used during server refresh. Never overwrite an existing local record.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertServerAssignments(assignments: List<AssignmentEntity>)

    @Query("""
        SELECT * FROM assignments
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY dueDateMillis ASC
    """)
    suspend fun getAssignments(userId: String): List<AssignmentEntity>

    @Query("""
        SELECT * FROM assignments
        WHERE userId = :userId AND syncStatus = 'PENDING'
    """)
    suspend fun getPendingAssignments(userId: String): List<AssignmentEntity>

    @Query("""
        UPDATE assignments
        SET syncStatus = 'SYNCED'
        WHERE userId = :userId AND id = :assignmentId
    """)
    suspend fun markAssignmentSynced(userId: String, assignmentId: Long)

    @Query("""
        UPDATE assignments
        SET isDeleted = 1, syncStatus = 'PENDING'
        WHERE userId = :userId AND id = :assignmentId
    """)
    suspend fun markAssignmentDeleted(userId: String, assignmentId: Long)

    @Query("""
        DELETE FROM assignments
        WHERE userId = :userId AND id = :assignmentId
    """)
    suspend fun permanentlyDeleteAssignment(
        userId: String,
        assignmentId: Long
    )

    @Query("""
        DELETE FROM assignments
        WHERE userId = :userId AND syncStatus = 'SYNCED'
    """)
    suspend fun deleteAllSyncedAssignments(userId: String)
}
