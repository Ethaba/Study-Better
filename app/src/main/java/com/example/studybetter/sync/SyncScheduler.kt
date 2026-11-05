
package com.example.studybetter.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val PERIODIC_SYNC_NAME = "study_better_periodic_sync"
    private const val ONE_TIME_SYNC_NAME = "study_better_one_time_sync"

    private fun networkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    fun schedulePeriodicSync(context: Context) {

        val request =
            PeriodicWorkRequestBuilder<StudyBetterSyncWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(networkConstraints())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun requestImmediateSync(context: Context) {

        val request =
            OneTimeWorkRequestBuilder<StudyBetterSyncWorker>()
                .setConstraints(networkConstraints())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
