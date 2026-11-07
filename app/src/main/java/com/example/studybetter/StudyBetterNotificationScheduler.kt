
package com.example.studybetter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object StudyBetterNotificationScheduler {

    private const val CHANNEL_ID = "study_better_notifications"
    private const val DAILY_REMINDER = "daily_study_reminder"

    fun scheduleAssignmentReminders(
        context: Context,
        assignmentId: Long,
        title: String,
        dueDateMillis: Long,
        progress: Int
    ) {
        cancelAssignmentReminders(context, assignmentId)

        if (progress >= 100) return

        val now = System.currentTimeMillis()

        // Reminder 24 hours before the due date.
        val dayBefore = dueDateMillis - TimeUnit.DAYS.toMillis(1)

        if (dayBefore > now) {
            scheduleNotification(
                context,
                "assignment_24h_$assignmentId",
                "Assignment due tomorrow",
                "$title is due tomorrow.",
                dayBefore - now,
                "assignment_24h_$assignmentId".hashCode() and 0x7fffffff
            )
        }

        // Reminder at 10 AM on the due date.
        val dueDayReminder = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (dueDayReminder > now) {
            scheduleNotification(
                context,
                "assignment_due_$assignmentId",
                "Assignment due today",
                "Remember to work on $title today.",
                dueDayReminder - now,
                "assignment_due_$assignmentId".hashCode() and 0x7fffffff
            )
        }
    }

    fun cancelAssignmentReminders(
        context: Context,
        assignmentId: Long
    ) {
        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork("assignment_24h_$assignmentId")
        workManager.cancelUniqueWork("assignment_due_$assignmentId")
    }

    fun scheduleDailyStudyReminder(context: Context) {
        val now = Calendar.getInstance()

        val nextReminder = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay =
            nextReminder.timeInMillis - now.timeInMillis

        val request =
            PeriodicWorkRequestBuilder<StudyBetterNotificationWorker>(
                24,
                TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        "title" to "Time to study!",
                        "body" to "Take some time today to work towards your academic goals.",
                        "notification_id" to 900001
                    )
                )
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun scheduleNotification(
        context: Context,
        workName: String,
        title: String,
        body: String,
        delay: Long,
        notificationId: Int
    ) {
        if (delay <= 0) return

        val request =
            OneTimeWorkRequestBuilder<StudyBetterNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        "title" to title,
                        "body" to body,
                        "notification_id" to notificationId
                    )
                )
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

class StudyBetterNotificationWorker(
    context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Study Better"
        val body = inputData.getString("body") ?: "You have a reminder."
        val notificationId = inputData.getInt("notification_id", 1)

        return try {
            showNotification(title, body, notificationId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        notificationId: Int
    ) {
        val channelId = "study_better_notifications"

        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Study Better Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(channel)
        }

        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                applicationContext,
                notificationId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(applicationContext.applicationInfo.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        manager.notify(notificationId, notification)
    }
}
