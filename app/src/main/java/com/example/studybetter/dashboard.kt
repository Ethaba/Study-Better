
package com.example.studybetter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.studybetter.data.AssignmentRoomRepository
import com.example.studybetter.data.ModuleRoomRepository
import com.example.studybetter.data.StudySessionRoomRepository
import com.example.studybetter.sync.SyncScheduler
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class dashboard : AppCompatActivity() {

    private lateinit var assignmentRepository: AssignmentRoomRepository
    private lateinit var moduleRepository: ModuleRoomRepository
    private lateinit var studySessionRepository: StudySessionRoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getSharedPreferences("study_better_settings", MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", task.result)
                    .apply()

                Log.d("StudyBetterFCM", "FCM token received")
            } else {
                Log.e("StudyBetterFCM", "Could not retrieve FCM token", task.exception)
            }
        }

        setContentView(R.layout.activity_dashboard)

        assignmentRepository = AssignmentRoomRepository(this)
        moduleRepository = ModuleRoomRepository(this)
        studySessionRepository = StudySessionRoomRepository(this)

        SyncScheduler.schedulePeriodicSync(this)
        SyncScheduler.requestImmediateSync(this)

        findViewById<View>(R.id.menu_icon).setOnClickListener { showMenu(it) }

        findViewById<View>(R.id.btn_add_assignment).setOnClickListener {
            openScreen(assignments::class.java)
        }

        findViewById<View>(R.id.btn_start_study).setOnClickListener {
            openScreen(study_session::class.java)
        }

        findViewById<View>(R.id.btn_add_module).setOnClickListener {
            openScreen(modules::class.java)
        }

        findViewById<View>(R.id.btn_view_calendar).setOnClickListener {
            openScreen(calendar::class.java)
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    R.string.notifications_disabled,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        lifecycleScope.launch {
            try {
                val dashboardData = withContext(Dispatchers.IO) {
                    Triple(
                        assignmentRepository.getAssignments(),
                        moduleRepository.getModules(),
                        studySessionRepository.getSessions()
                    )
                }

                val assignments = dashboardData.first
                val modules = dashboardData.second
                val sessions = dashboardData.third

                val prefs = getSharedPreferences(
                    "study_better_settings",
                    MODE_PRIVATE
                )

                val fullName = prefs.getString("full_name", "Student") ?: "Student"

                val totalAssignments = assignments.size
                val remainingAssignments = assignments.count { it.progress < 100 }

                val averageProgress = if (totalAssignments == 0) {
                    0
                } else {
                    assignments.sumOf { it.progress } / totalAssignments
                }

                val studySeconds = sessions.sumOf {
                    it.durationSeconds.toLong()
                }

                findViewById<TextView>(R.id.tv_greeting).text =
                    getString(R.string.dashboard_greeting, fullName)

                findViewById<TextView>(R.id.tv_progress_percent).text =
                    "$averageProgress%"

                findViewById<CircularProgressIndicator>(
                    R.id.progress_indicator
                ).progress = averageProgress

                findViewById<TextView>(R.id.tv_tasks_total).text =
                    getString(R.string.tasks_total, totalAssignments)

                findViewById<TextView>(R.id.tv_tasks_remaining).text =
                    getString(R.string.tasks_remaining, remainingAssignments)

                showDueSoonAssignments(assignments)

                showLatestAchievements(assignments, modules, studySeconds)

                AchievementNotificationManager.checkAndNotify(
                    this@dashboard,
                    assignments,
                    modules,
                    studySeconds
                )

                findViewById<TextView>(R.id.tv_streak).text =
                    getString(R.string.days_count, 0)

            } catch (e: Exception) {
                Log.e("StudyBetter", "Dashboard loading failed", e)
            }
        }
    }

    private fun showDueSoonAssignments(assignments: List<Assignment>) {
        val dueSoonContainer = findViewById<LinearLayout>(R.id.ll_due_soon)
        val dueSoonAssignments = getDueSoonAssignments(assignments)

        dueSoonContainer.removeAllViews()

        if (dueSoonAssignments.isEmpty()) {
            val noAssignmentsText = TextView(this).apply {
                text = getString(R.string.no_assignments_due)
                setTextColor(android.graphics.Color.rgb(51, 51, 51))
                textSize = 15f
            }

            val messageText = TextView(this).apply {
                text = getString(R.string.add_assignment_start)
                setTextColor(android.graphics.Color.rgb(229, 57, 53))
                textSize = 14f
                setPadding(0, 4, 0, 0)
            }

            dueSoonContainer.addView(noAssignmentsText)
            dueSoonContainer.addView(messageText)
            return
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        dueSoonAssignments.forEach { assignment ->
            val assignmentTitle = TextView(this).apply {
                text = assignment.title
                setTextColor(android.graphics.Color.rgb(51, 51, 51))
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val assignmentDetails = TextView(this).apply {
                text = "${assignment.module} • ${dateFormat.format(Date(assignment.dueDateMillis))}"
                setTextColor(android.graphics.Color.rgb(102, 102, 102))
                textSize = 14f
                setPadding(0, 4, 0, 12)
            }

            dueSoonContainer.addView(assignmentTitle)
            dueSoonContainer.addView(assignmentDetails)
        }
    }

    private fun getDueSoonAssignments(
        assignments: List<Assignment>
    ): List<Assignment> {
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7L * 24 * 60 * 60 * 1000)

        return assignments
            .filter {
                it.progress < 100 &&
                        it.dueDateMillis >= now &&
                        it.dueDateMillis <= sevenDaysFromNow
            }
            .sortedBy { it.dueDateMillis }
    }

    private fun showLatestAchievements(
        assignments: List<Assignment>,
        modules: List<Module>,
        studySeconds: Long
    ) {
        val achievementsContainer =
            findViewById<LinearLayout>(R.id.ll_latest_achievements)

        achievementsContainer.removeAllViews()

        val completedAssignments = assignments.count { it.progress == 100 }

        val unlockedAchievements = mutableListOf<String>()

        if (modules.isNotEmpty()) {
            unlockedAchievements.add(getString(R.string.achievement_first_steps))
        }

        if (assignments.isNotEmpty()) {
            unlockedAchievements.add(getString(R.string.achievement_getting_started))
        }

        if (completedAssignments >= 1) {
            unlockedAchievements.add(getString(R.string.achievement_assignment_complete))
        }

        if (completedAssignments >= 3) {
            unlockedAchievements.add(getString(R.string.achievement_triple_threat))
        }

        if (studySeconds >= 3600) {
            unlockedAchievements.add(getString(R.string.achievement_one_hour))
        }

        if (unlockedAchievements.isEmpty()) {
            val noAchievementsText = TextView(this).apply {
                text = getString(R.string.no_achievements)
                setTextColor(android.graphics.Color.rgb(136, 136, 136))
                textSize = 14f
            }

            achievementsContainer.addView(noAchievementsText)
            return
        }

        unlockedAchievements.take(3).forEach { achievement ->
            val achievementText = TextView(this).apply {
                text = "✓ $achievement"
                setTextColor(android.graphics.Color.rgb(51, 51, 51))
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }

            achievementsContainer.addView(achievementText)
        }
    }

    private fun showMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)

        popupMenu.menu.add(getString(R.string.menu_profile))
        popupMenu.menu.add(getString(R.string.menu_modules))
        popupMenu.menu.add(getString(R.string.menu_assignments))
        popupMenu.menu.add(getString(R.string.menu_study_sessions))
        popupMenu.menu.add(getString(R.string.menu_academic_resources))
        popupMenu.menu.add(getString(R.string.menu_achievements))

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                getString(R.string.menu_profile) ->
                    openScreen(profile_settings::class.java)

                getString(R.string.menu_modules) ->
                    openScreen(modules::class.java)

                getString(R.string.menu_assignments) ->
                    openScreen(assignments::class.java)

                getString(R.string.menu_study_sessions) ->
                    openScreen(study_session_history::class.java)

                getString(R.string.menu_academic_resources) ->
                    openScreen(academic_resources::class.java)

                getString(R.string.menu_achievements) ->
                    openScreen(achievements::class.java)
            }
            true
        }

        popupMenu.show()
    }

    private fun openScreen(screen: Class<*>) {
        startActivity(Intent(this, screen))
    }
}
