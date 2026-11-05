
package com.example.studybetter.sync

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studybetter.api.AssignmentRequest
import com.example.studybetter.api.ModuleRequest
import com.example.studybetter.api.RetrofitClient
import com.example.studybetter.api.StudySessionRequest
import com.example.studybetter.data.AssignmentEntity
import com.example.studybetter.data.ModuleEntity
import com.example.studybetter.data.StudyBetterDatabase
import com.example.studybetter.data.StudySessionRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class StudyBetterSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "StudyBetterSync"

        // Prevent immediate and periodic workers from syncing simultaneously.
        private val syncMutex = Mutex()
    }

    override suspend fun doWork(): Result = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val preferences = applicationContext.getSharedPreferences(
                    "study_better_settings",
                    Context.MODE_PRIVATE
                )

                val userId = preferences.all["user_id"]?.toString()

                if (userId.isNullOrBlank() || userId == "-1") {
                    Log.w(TAG, "No logged-in user. Synchronisation skipped.")
                    return@withContext Result.success()
                }

                RetrofitClient.initialize(applicationContext)

                val database = StudyBetterDatabase.getDatabase(applicationContext)
                val moduleDao = database.moduleDao()
                val assignmentDao = database.assignmentDao()

                val sessionRepository =
                    StudySessionRoomRepository(applicationContext)

                var hasFailures = false

                // -----------------------------------------
                // MODULE SYNCHRONISATION
                // -----------------------------------------

                val pendingModules = moduleDao.getPendingModules(userId)

                for (module in pendingModules) {
                    try {
                        if (module.isDeleted) {
                            if (module.id <= Int.MAX_VALUE) {
                                val response =
                                    RetrofitClient.api.deleteModule(module.id.toInt())

                                if (!response.isSuccessful && response.code() != 404) {
                                    hasFailures = true
                                    continue
                                }
                            }

                            moduleDao.permanentlyDeleteModule(userId, module.id)
                            continue
                        }

                        val request = ModuleRequest(
                            code = module.code,
                            title = module.title,
                            description = module.description
                        )

                        if (module.id > Int.MAX_VALUE) {

                            // New local module: create it on the server.
                            val response =
                                RetrofitClient.api.createModule(request)

                            if (!response.isSuccessful || response.body() == null) {
                                hasFailures = true
                                Log.e(TAG, "Module creation failed: ${module.id}")
                                continue
                            }

                            val serverModule = response.body()!!

                            // Replace the temporary local ID with the server ID
                            // in one database transaction.
                            database.withTransaction {
                                moduleDao.insertModule(
                                    ModuleEntity(
                                        userId = userId,
                                        id = serverModule.moduleID.toLong(),
                                        code = serverModule.code,
                                        title = serverModule.title,
                                        description = serverModule.description,
                                        syncStatus = "SYNCED",
                                        isDeleted = false
                                    )
                                )

                                moduleDao.permanentlyDeleteModule(
                                    userId,
                                    module.id
                                )
                            }

                        } else {

                            // Existing server module: update it.
                            val response =
                                RetrofitClient.api.updateModule(
                                    module.id.toInt(),
                                    request
                                )

                            if (response.isSuccessful) {
                                moduleDao.markModuleSynced(userId, module.id)
                            } else {
                                hasFailures = true
                                Log.e(
                                    TAG,
                                    "Module update failed: ${response.code()}"
                                )
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Module sync failed: ${module.id}", e)
                        hasFailures = true
                    }
                }

                // -----------------------------------------
                // ASSIGNMENT SYNCHRONISATION
                // -----------------------------------------

                val pendingAssignments =
                    assignmentDao.getPendingAssignments(userId)

                for (assignment in pendingAssignments) {
                    try {
                        if (assignment.isDeleted) {
                            if (assignment.id <= Int.MAX_VALUE) {
                                val response =
                                    RetrofitClient.api.deleteAssignment(
                                        assignment.id.toInt()
                                    )

                                if (!response.isSuccessful && response.code() != 404) {
                                    hasFailures = true
                                    continue
                                }
                            }

                            assignmentDao.permanentlyDeleteAssignment(
                                userId,
                                assignment.id
                            )
                            continue
                        }

                        val request = AssignmentRequest(
                            title = assignment.title,
                            description = assignment.description,
                            moduleID = null,
                            moduleName = assignment.module,
                            dueDateMillis = assignment.dueDateMillis,
                            priority = assignment.priority,
                            progress = assignment.progress
                        )

                        if (assignment.id > Int.MAX_VALUE) {

                            // New local assignment: create it on the server.
                            val response =
                                RetrofitClient.api.createAssignment(request)

                            if (!response.isSuccessful || response.body() == null) {
                                hasFailures = true
                                Log.e(TAG, "Assignment creation failed: ${assignment.id}")
                                continue
                            }

                            val serverAssignment = response.body()!!

                            // Save the server record and remove the temporary
                            // local record atomically.
                            database.withTransaction {
                                assignmentDao.insertAssignment(
                                    AssignmentEntity(
                                        userId = userId,
                                        id = serverAssignment.assignmentID.toLong(),
                                        title = serverAssignment.title,
                                        description = serverAssignment.description,
                                        module = serverAssignment.moduleName,
                                        dueDateMillis = serverAssignment.dueDateMillis,
                                        priority = serverAssignment.priority,
                                        progress = serverAssignment.progress,
                                        syncStatus = "SYNCED",
                                        isDeleted = false
                                    )
                                )

                                assignmentDao.permanentlyDeleteAssignment(
                                    userId,
                                    assignment.id
                                )
                            }

                        } else {

                            // Existing server assignment: update it.
                            val response =
                                RetrofitClient.api.updateAssignment(
                                    assignment.id.toInt(),
                                    request
                                )

                            if (response.isSuccessful) {
                                assignmentDao.markAssignmentSynced(
                                    userId,
                                    assignment.id
                                )
                            } else {
                                hasFailures = true
                                Log.e(
                                    TAG,
                                    "Assignment update failed: ${response.code()}"
                                )
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Assignment sync failed: ${assignment.id}",
                            e
                        )
                        hasFailures = true
                    }
                }

                // -----------------------------------------
                // STUDY SESSION SYNCHRONISATION
                // -----------------------------------------

                // Delete previously uploaded sessions from the backend.
                val pendingDeletions =
                    sessionRepository.getPendingDeletions()

                for (session in pendingDeletions) {
                    try {
                        val serverId = session.serverId ?: continue

                        val response =
                            RetrofitClient.api.deleteStudySession(serverId).execute()

                        if (response.isSuccessful || response.code() == 404) {
                            // Remove the local record only after the server
                            // confirms deletion or reports that it is absent.
                            sessionRepository.permanentlyDeleteSession(
                                session.localId
                            )
                        } else {
                            hasFailures = true
                            Log.e(
                                TAG,
                                "Study session deletion failed: ${response.code()}"
                            )
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Study session deletion failed", e)
                        hasFailures = true
                    }
                }


                val pendingSessions =
                    sessionRepository.getPendingSessions()

                for (session in pendingSessions) {
                    try {
                        val request = StudySessionRequest(
                            startedAt = session.startedAt,
                            durationSeconds = session.durationSeconds,
                            description = session.description
                        )

                        val response =
                            RetrofitClient.api.createStudySession(request).execute()

                        if (response.isSuccessful && response.body() != null) {

                            sessionRepository.markSynced(
                                session.localId,
                                response.body()!!.studySessionID
                            )

                        } else {
                            hasFailures = true

                            Log.e(
                                TAG,
                                "Study session upload failed: ${response.code()}"
                            )
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Study session sync failed", e)
                        hasFailures = true
                    }
                }

                // Retry failed operations when connectivity or the server
                // prevents successful synchronisation.
                if (hasFailures) {
                    Log.w(TAG, "Some records could not be synchronised. Retrying.")
                    Result.retry()
                } else {
                    Log.i(TAG, "Synchronisation completed successfully.")
                    Result.success()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Synchronisation worker failed", e)
                Result.retry()
            }
        }
    }
}
