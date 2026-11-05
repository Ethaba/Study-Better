
package com.example.studybetter.data

import android.content.Context
import com.example.studybetter.StudySessionStorage
import com.example.studybetter.sync.SyncScheduler

class StudySessionRoomRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = StudyBetterDatabase.getDatabase(appContext)
    private val dao = database.studySessionDao()

    private fun userId(): String {
        val id = appContext.getSharedPreferences(
            "study_better_settings",
            Context.MODE_PRIVATE
        ).getInt("user_id", -1)

        return id.toString()
    }

    suspend fun migrateOldSessions() {
        val preferences = appContext.getSharedPreferences(
            "study_better_room_migration",
            Context.MODE_PRIVATE
        )

        val migrationKey = "study_sessions_migrated_to_room"

        if (preferences.getBoolean(migrationKey, false)) return

        val oldSessions = StudySessionStorage.getSessions(appContext)
        val entities = mutableListOf<StudySessionEntity>()

        for (index in 0 until oldSessions.length()) {
            val session = oldSessions.getJSONObject(index)

            entities.add(
                StudySessionEntity(
                    userId = userId(),
                    startedAt = session.optString("startedAt"),
                    durationSeconds = session.optInt("durationSeconds"),
                    description = session.optString(
                        "description",
                        "Study session"
                    ),
                    syncStatus = if (session.optBoolean("synced", false)) {
                        "SYNCED"
                    } else {
                        "PENDING"
                    }
                )
            )
        }

        if (entities.isNotEmpty()) {
            dao.insertSessions(entities)
        }

        preferences.edit()
            .putBoolean(migrationKey, true)
            .apply()

        if (entities.any { it.syncStatus == "PENDING" }) {
            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun getSessions(): List<StudySessionEntity> {
        migrateOldSessions()
        return dao.getSessions(userId())
    }

    suspend fun getPendingSessions(): List<StudySessionEntity> {
        migrateOldSessions()
        return dao.getPendingSessions(userId())
    }

    suspend fun getPendingDeletions(): List<StudySessionEntity> {
        migrateOldSessions()
        return dao.getPendingDeletions(userId())
    }

    suspend fun saveSession(
        startedAt: String,
        durationSeconds: Int,
        description: String
    ): Long {
        migrateOldSessions()

        val localId = dao.insertSession(
            StudySessionEntity(
                userId = userId(),
                startedAt = startedAt,
                durationSeconds = durationSeconds,
                description = description,
                syncStatus = "PENDING",
                isDeleted = false
            )
        )

        SyncScheduler.requestImmediateSync(appContext)

        return localId
    }

    suspend fun markSynced(localId: Long, serverId: Int) {
        dao.markSessionSynced(
            userId = userId(),
            localId = localId,
            serverId = serverId
        )
    }

    suspend fun deleteSession(localId: Long) {
        migrateOldSessions()

        val session = dao.getSession(userId(), localId) ?: return

        if (session.serverId == null) {
            // It has never been uploaded, so remove it locally.
            dao.deleteSession(userId(), localId)
        } else {
            // It exists on the server. Keep a deletion marker.
            dao.markSessionDeleted(userId(), localId)

            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun permanentlyDeleteSession(localId: Long) {
        dao.deleteSession(userId(), localId)
    }
}
