
package com.example.studybetter.data

import android.content.Context
import androidx.room.withTransaction
import com.example.studybetter.Assignment
import com.example.studybetter.AssignmentStorage
import com.example.studybetter.sync.SyncScheduler

class AssignmentRoomRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = StudyBetterDatabase.getDatabase(appContext)
    private val dao = database.assignmentDao()

    private fun userId(): String {
        val id = appContext.getSharedPreferences(
            "study_better_settings",
            Context.MODE_PRIVATE
        ).getInt("user_id", -1)

        return id.toString()
    }

    suspend fun migrateOldAssignments() {
        val preferences = appContext.getSharedPreferences(
            "study_better_room_migration",
            Context.MODE_PRIVATE
        )

        val migrationKey = "assignments_migrated_${userId()}"

        if (preferences.getBoolean(migrationKey, false)) return

        val oldAssignments = AssignmentStorage.getAssignments(appContext)

        oldAssignments.forEach { assignment ->
            dao.insertAssignment(
                AssignmentEntity(
                    userId = userId(),
                    id = assignment.id,
                    title = assignment.title,
                    description = assignment.description,
                    module = assignment.module,
                    dueDateMillis = assignment.dueDateMillis,
                    priority = assignment.priority,
                    progress = assignment.progress,
                    syncStatus = if (assignment.id > Int.MAX_VALUE) {
                        "PENDING"
                    } else {
                        "SYNCED"
                    }
                )
            )
        }

        preferences.edit()
            .putBoolean(migrationKey, true)
            .apply()

        if (oldAssignments.any { it.id > Int.MAX_VALUE }) {
            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun getAssignments(): List<Assignment> {
        migrateOldAssignments()
        return dao.getAssignments(userId()).map { it.toAssignment() }
    }

    suspend fun getPendingAssignments(): List<AssignmentEntity> {
        migrateOldAssignments()
        return dao.getPendingAssignments(userId())
    }

    suspend fun saveAssignment(
        assignment: Assignment,
        pending: Boolean
    ) {
        migrateOldAssignments()

        dao.insertAssignment(
            AssignmentEntity(
                userId = userId(),
                id = assignment.id,
                title = assignment.title,
                description = assignment.description,
                module = assignment.module,
                dueDateMillis = assignment.dueDateMillis,
                priority = assignment.priority,
                progress = assignment.progress,
                syncStatus = if (pending) "PENDING" else "SYNCED",
                isDeleted = false
            )
        )

        if (pending) {
            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun deleteAssignment(assignmentId: Long) {
        migrateOldAssignments()

        val assignment = dao.getAssignments(userId())
            .firstOrNull { it.id == assignmentId }

        if (assignment != null) {
            if (assignment.id > Int.MAX_VALUE) {
                dao.permanentlyDeleteAssignment(userId(), assignmentId)
            } else {
                dao.markAssignmentDeleted(userId(), assignmentId)
                SyncScheduler.requestImmediateSync(appContext)
            }
        }
    }

    suspend fun permanentlyDeleteAssignment(assignmentId: Long) {
        dao.permanentlyDeleteAssignment(userId(), assignmentId)
    }

    suspend fun markSynced(assignmentId: Long) {
        dao.markAssignmentSynced(userId(), assignmentId)
    }


    suspend fun replaceServerAssignments(serverAssignments: List<Assignment>) {
        migrateOldAssignments()

        database.withTransaction {
            // Remove old server records, but retain pending offline changes.
            dao.deleteAllSyncedAssignments(userId())

            val entities = serverAssignments.map { assignment ->
                AssignmentEntity(
                    userId = userId(),
                    id = assignment.id,
                    title = assignment.title,
                    description = assignment.description,
                    module = assignment.module,
                    dueDateMillis = assignment.dueDateMillis,
                    priority = assignment.priority,
                    progress = assignment.progress,
                    syncStatus = "SYNCED",
                    isDeleted = false
                )
            }

            // IGNORE prevents server records from overwriting pending local records.
            if (entities.isNotEmpty()) {
                dao.insertServerAssignments(entities)
            }
        }
    }


    private fun AssignmentEntity.toAssignment(): Assignment {
        return Assignment(
            id = id,
            title = title,
            description = description,
            module = module,
            dueDateMillis = dueDateMillis,
            priority = priority,
            progress = progress
        )
    }
}
