
package com.example.studybetter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)

    @Query("""
        SELECT * FROM study_sessions
        WHERE userId = :userId
        AND isDeleted = 0
        ORDER BY startedAt DESC
    """)
    suspend fun getSessions(userId: String): List<StudySessionEntity>

    // Sessions waiting to be uploaded.
    @Query("""
        SELECT * FROM study_sessions
        WHERE userId = :userId
        AND syncStatus = 'PENDING'
        AND isDeleted = 0
        ORDER BY startedAt ASC
    """)
    suspend fun getPendingSessions(userId: String): List<StudySessionEntity>

    // Sessions waiting to be deleted from the backend.
    @Query("""
        SELECT * FROM study_sessions
        WHERE userId = :userId
        AND isDeleted = 1
        AND serverId IS NOT NULL
    """)
    suspend fun getPendingDeletions(userId: String): List<StudySessionEntity>

    @Query("""
        SELECT * FROM study_sessions
        WHERE userId = :userId AND localId = :localId
        LIMIT 1
    """)
    suspend fun getSession(userId: String, localId: Long): StudySessionEntity?

    @Query("""
        UPDATE study_sessions
        SET syncStatus = 'SYNCED', serverId = :serverId
        WHERE userId = :userId
        AND localId = :localId
        AND isDeleted = 0
    """)
    suspend fun markSessionSynced(
        userId: String,
        localId: Long,
        serverId: Int
    )

    @Query("""
        UPDATE study_sessions
        SET isDeleted = 1, syncStatus = 'PENDING'
        WHERE userId = :userId AND localId = :localId
    """)
    suspend fun markSessionDeleted(userId: String, localId: Long)

    @Query("""
        DELETE FROM study_sessions
        WHERE userId = :userId AND localId = :localId
    """)
    suspend fun deleteSession(userId: String, localId: Long)

    @Query("""
        DELETE FROM study_sessions
        WHERE userId = :userId
    """)
    suspend fun deleteAllSessions(userId: String)
}
