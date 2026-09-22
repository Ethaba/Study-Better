package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator

class dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<View>(R.id.menu_icon).setOnClickListener { view -> showMenu(view) }
        findViewById<View>(R.id.btn_add_assignment).setOnClickListener { openScreen(assignments::class.java) }
        findViewById<View>(R.id.btn_start_study).setOnClickListener { openScreen(study_session::class.java) }
        findViewById<View>(R.id.btn_add_module).setOnClickListener { openScreen(modules::class.java) }
        findViewById<View>(R.id.btn_view_calendar).setOnClickListener { openScreen(calendar::class.java) }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        val assignments = AssignmentStorage.getAssignments(this)
        val totalAssignments = assignments.size
        val remainingAssignments = assignments.count { it.progress < 100 }
        val averageProgress = if (totalAssignments == 0) 0 else assignments.sumOf { it.progress } / totalAssignments
        val fullName = getSharedPreferences("study_better_settings", MODE_PRIVATE)
            .getString("full_name", "Student")

        // Dashboard totals are calculated from saved assignments instead of placeholders.
        findViewById<TextView>(R.id.tv_greeting).text = "Hi, $fullName!"
        findViewById<TextView>(R.id.tv_progress_percent).text = "$averageProgress%"
        findViewById<CircularProgressIndicator>(R.id.progress_indicator).progress = averageProgress
        findViewById<TextView>(R.id.tv_streak).text = "0 days"
        findViewById<TextView>(R.id.tv_tasks_total).text = "$totalAssignments Total"
        findViewById<TextView>(R.id.tv_tasks_remaining).text = "$remainingAssignments remaining"
        findViewById<TextView>(R.id.tv_no_achievements).visibility = View.VISIBLE
    }

    private fun showMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menu.add("Profile & Settings")
        popupMenu.menu.add("Modules")
        popupMenu.menu.add("Assignments")
        popupMenu.menu.add("Study Sessions")
        popupMenu.menu.add("Achievements")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Profile & Settings" -> openScreen(profile_settings::class.java)
                "Modules" -> openScreen(modules::class.java)
                "Assignments" -> openScreen(assignments::class.java)
                "Study Sessions" -> openScreen(study_session::class.java)
                "Achievements" -> openScreen(achievements::class.java)
            }
            true
        }
        popupMenu.show()
    }

    private fun openScreen(screen: Class<*>) {
        startActivity(Intent(this, screen))
    }
}
