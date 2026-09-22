package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // New users begin with no completed work or study streak.
        val fullName = getSharedPreferences("study_better_settings", MODE_PRIVATE)
            .getString("full_name", "Student")
        findViewById<TextView>(R.id.tv_greeting).text = "Hi, $fullName!"
        findViewById<TextView>(R.id.tv_progress_percent).text = "0%"
        findViewById<TextView>(R.id.tv_streak).text = "0 days"
        findViewById<TextView>(R.id.tv_tasks_total).text = "0 Total"
        findViewById<TextView>(R.id.tv_tasks_remaining).text = "0 remaining"
        findViewById<TextView>(R.id.tv_no_achievements).visibility = View.VISIBLE

        findViewById<View>(R.id.menu_icon).setOnClickListener { view -> showMenu(view) }
        findViewById<View>(R.id.btn_add_assignment).setOnClickListener { openScreen(assignments::class.java) }
        findViewById<View>(R.id.btn_start_study).setOnClickListener { openScreen(study_session::class.java) }
        findViewById<View>(R.id.btn_add_module).setOnClickListener { openScreen(modules::class.java) }
        findViewById<View>(R.id.btn_view_calendar).setOnClickListener { openScreen(calendar::class.java) }
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
