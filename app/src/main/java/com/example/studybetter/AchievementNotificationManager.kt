
package com.example.studybetter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object AchievementNotificationManager {

    private const val CHANNEL_ID = "study_better_notifications"
    private const val PREFS_NAME = "study_better_achievements"
    private const val UNLOCKED_KEY = "unlocked_names"

    fun checkAndNotify(
        context: Context,
        assignments: List<Assignment>,
        modules: List<Module>,
        studySeconds: Long
    ) {
        val completed = assignments.count { it.progress == 100 }
        val active = assignments.count { it.progress < 100 }

        val unlocked = mutableSetOf<String>()

        if (modules.isNotEmpty()) unlocked.add("First Steps")
        if (assignments.isNotEmpty()) unlocked.add("Getting Started")
        if (completed >= 1) unlocked.add("Assignment Complete")
        if (completed >= 3) unlocked.add("Triple Threat")
        if (completed >= 5) unlocked.add("Assignment Master")
        if (studySeconds > 0) unlocked.add("Study Session")
        if (studySeconds >= 3600) unlocked.add("One Hour")
        if (studySeconds >= 5 * 3600) unlocked.add("Study Time")
        if (studySeconds >= 10 * 3600) unlocked.add("Dedicated Student")
        if (completed >= 10) unlocked.add("10 Assignments")
        if (completed >= 15) unlocked.add("15 Assignments")
        if (completed >= 20) unlocked.add("20 Assignments")
        if (modules.size >= 3) unlocked.add("Module Builder")
        if (modules.size >= 5) unlocked.add("Module Collector")
        if (active >= 5) unlocked.add("Busy Student")
        if (assignments.size >= 10) unlocked.add("Organised")
        if (completed >= 12) unlocked.add("Halfway There")
        if (studySeconds >= 20 * 3600) unlocked.add("Study Champion")
        if (completed >= 25) unlocked.add("25 Assignments")
        if (completed >= 30) unlocked.add("30 Assignments")
        if (studySeconds >= 30 * 3600) unlocked.add("Long Study Session")
        if (completed >= 40) unlocked.add("40 Assignments")
        if (completed >= 50) unlocked.add("50 Assignments")
        if (studySeconds >= 50 * 3600) unlocked.add("Study Legend")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(UNLOCKED_KEY)) {
            prefs.edit().putStringSet(UNLOCKED_KEY, unlocked).apply()
            return
        }

        val previouslyUnlocked =
            prefs.getStringSet(UNLOCKED_KEY, emptySet())?.toSet() ?: emptySet()

        val newlyUnlocked = unlocked - previouslyUnlocked

        newlyUnlocked.forEach { title ->
            showNotification(context, title)
        }

        prefs.edit().putStringSet(UNLOCKED_KEY, unlocked).apply()
    }

    private fun showNotification(context: Context, title: String) {
        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Study Better Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context,
                title.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle("Achievement Unlocked!")
            .setContentText("Congratulations! You unlocked: $title")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Congratulations! You unlocked: $title")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        try {
            manager.notify(title.hashCode() and 0x7fffffff, notification)
        } catch (_: SecurityException) {
            // Notifications are unavailable if permission has been denied.
        }
    }
}
