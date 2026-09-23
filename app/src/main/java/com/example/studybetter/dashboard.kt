package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<View>(R.id.menu_icon).setOnClickListener { view ->
            showMenu(view)
        }

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

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {

        val prefs = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        // Get logged-in user's name
        val fullName = prefs.getString(
            "full_name",
            "Student"
        ) ?: "Student"

        // Get saved assignments
        val assignments = AssignmentStorage.getAssignments(this)

        val totalAssignments = assignments.size

        val remainingAssignments = assignments.count {
            it.progress < 100
        }

        val averageProgress =
            if (totalAssignments == 0) {
                0
            } else {
                assignments.sumOf { it.progress } / totalAssignments
            }

        // Greeting
        findViewById<TextView>(R.id.tv_greeting).text =
            "Hi, $fullName!"

        // Overall assignment progress
        findViewById<TextView>(R.id.tv_progress_percent).text =
            "$averageProgress%"

        findViewById<CircularProgressIndicator>(
            R.id.progress_indicator
        ).progress = averageProgress

        // Assignment statistics
        findViewById<TextView>(R.id.tv_tasks_total).text =
            "$totalAssignments Total"

        findViewById<TextView>(R.id.tv_tasks_remaining).text =
            "$remainingAssignments remaining"

        // Show assignments due soon
        showDueSoonAssignments(assignments)

        // Show latest achievements
        showLatestAchievements(assignments)

        // Study streak
        findViewById<TextView>(R.id.tv_streak).text =
            "0 days"
    }

    private fun showDueSoonAssignments(
        assignments: List<Assignment>
    ) {

        val dueSoonContainer =
            findViewById<LinearLayout>(R.id.ll_due_soon)

        val dueSoonAssignments =
            getDueSoonAssignments(assignments)

        // Remove existing content
        dueSoonContainer.removeAllViews()

        // No assignments due soon
        if (dueSoonAssignments.isEmpty()) {

            val noAssignmentsText =
                TextView(this).apply {
                    text = "No assignments due soon"
                    setTextColor(
                        android.graphics.Color.rgb(51, 51, 51)
                    )
                    textSize = 15f
                }

            val messageText =
                TextView(this).apply {
                    text = "Add an assignment to get started"
                    setTextColor(
                        android.graphics.Color.rgb(229, 57, 53)
                    )
                    textSize = 14f
                    setPadding(
                        0,
                        4,
                        0,
                        0
                    )
                }

            dueSoonContainer.addView(noAssignmentsText)
            dueSoonContainer.addView(messageText)

            return
        }

        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        )

        // Display each due-soon assignment
        dueSoonAssignments.forEach { assignment ->

            val assignmentTitle =
                TextView(this).apply {
                    text = assignment.title
                    setTextColor(
                        android.graphics.Color.rgb(51, 51, 51)
                    )
                    textSize = 15f
                    setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )
                }

            val assignmentDetails =
                TextView(this).apply {

                    text =
                        "${assignment.module} • Due ${
                            dateFormat.format(
                                Date(assignment.dueDateMillis)
                            )
                        }"

                    setTextColor(
                        android.graphics.Color.rgb(102, 102, 102)
                    )

                    textSize = 14f

                    setPadding(
                        0,
                        4,
                        0,
                        12
                    )
                }

            dueSoonContainer.addView(
                assignmentTitle
            )

            dueSoonContainer.addView(
                assignmentDetails
            )
        }
    }

    private fun getDueSoonAssignments(
        assignments: List<Assignment>
    ): List<Assignment> {

        val now = System.currentTimeMillis()

        // Seven days from now
        val sevenDaysFromNow =
            now + (7L * 24 * 60 * 60 * 1000)

        return assignments
            .filter {

                // Assignment is not completed
                it.progress < 100 &&

                        // Assignment has not already passed
                        it.dueDateMillis >= now &&

                        // Assignment is due within 7 days
                        it.dueDateMillis <= sevenDaysFromNow
            }
            .sortedBy {
                it.dueDateMillis
            }
    }

    private fun showLatestAchievements(
        assignments: List<Assignment>
    ) {

        val achievementsContainer =
            findViewById<LinearLayout>(
                R.id.ll_latest_achievements
            )

        // Clear old achievement views
        achievementsContainer.removeAllViews()

        val completedAssignments =
            assignments.count {
                it.progress == 100
            }

        val modules =
            ModuleStorage.getModules(this)

        val studySeconds =
            getSharedPreferences(
                "study_better_settings",
                MODE_PRIVATE
            ).getLong(
                "study_seconds",
                0L
            )

        val unlockedAchievements =
            mutableListOf<String>()

        // First module
        if (modules.isNotEmpty()) {
            unlockedAchievements.add(
                "First Steps - Added your first module"
            )
        }

        // First assignment
        if (assignments.isNotEmpty()) {
            unlockedAchievements.add(
                "Getting Started - Added your first assignment"
            )
        }

        // First completed assignment
        if (completedAssignments >= 1) {
            unlockedAchievements.add(
                "Assignment Complete - Completed an assignment"
            )
        }

        // Three completed assignments
        if (completedAssignments >= 3) {
            unlockedAchievements.add(
                "Triple Threat - Completed 3 assignments"
            )
        }

        // One hour of study time
        if (studySeconds >= 3600) {
            unlockedAchievements.add(
                "One Hour - Studied for one hour"
            )
        }

        // No achievements yet
        if (unlockedAchievements.isEmpty()) {

            val noAchievementsText =
                TextView(this).apply {

                    text =
                        "No achievements yet. Keep studying!"

                    setTextColor(
                        android.graphics.Color.rgb(
                            136,
                            136,
                            136
                        )
                    )

                    textSize = 14f
                }

            achievementsContainer.addView(
                noAchievementsText
            )

            return
        }

        // Display up to 3 achievements
        unlockedAchievements
            .take(3)
            .forEach { achievement ->

                val achievementText =
                    TextView(this).apply {

                        text = "✓ $achievement"

                        setTextColor(
                            android.graphics.Color.rgb(
                                51,
                                51,
                                51
                            )
                        )

                        textSize = 14f

                        setPadding(
                            0,
                            8,
                            0,
                            8
                        )
                    }

                achievementsContainer.addView(
                    achievementText
                )
            }
    }

    private fun showMenu(anchor: View) {

        val popupMenu = PopupMenu(
            this,
            anchor
        )

        popupMenu.menu.add(
            "Profile & Settings"
        )

        popupMenu.menu.add(
            "Modules"
        )

        popupMenu.menu.add(
            "Assignments"
        )

        popupMenu.menu.add(
            "Study Sessions"
        )

        popupMenu.menu.add(
            "Achievements"
        )

        popupMenu.setOnMenuItemClickListener { item ->

            when (item.title) {

                "Profile & Settings" ->
                    openScreen(
                        profile_settings::class.java
                    )

                "Modules" ->
                    openScreen(
                        modules::class.java
                    )

                "Assignments" ->
                    openScreen(
                        assignments::class.java
                    )

                "Study Sessions" ->
                    openScreen(
                        study_session::class.java
                    )

                "Achievements" ->
                    openScreen(
                        achievements::class.java
                    )
            }

            true
        }

        popupMenu.show()
    }

    private fun openScreen(
        screen: Class<*>
    ) {
        startActivity(
            Intent(this, screen)
        )
    }
}