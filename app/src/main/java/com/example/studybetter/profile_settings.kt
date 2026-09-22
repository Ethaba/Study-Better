package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class profile_settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val preferences = getSharedPreferences("study_better_settings", MODE_PRIVATE)
        val languageText = findViewById<TextView>(R.id.tv_current_language)
        val notificationsText = findViewById<TextView>(R.id.tv_notifications_status)
        val nameText = findViewById<TextView>(R.id.tv_full_name)
        val levelText = findViewById<TextView>(R.id.tv_level)

        languageText.text = preferences.getString("language", "English")
        notificationsText.text = if (preferences.getBoolean("notifications", true)) "On" else "Off"
        nameText.text = preferences.getString("full_name", "Student")
        levelText.text = "Study goal: ${preferences.getInt("study_goal", 0)} minutes"

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.row_edit_profile).setOnClickListener {
            editName(nameText)
        }

        findViewById<android.view.View>(R.id.row_study_goal).setOnClickListener {
            editStudyGoal(levelText)
        }

        findViewById<android.view.View>(R.id.row_language).setOnClickListener {
            chooseLanguage(languageText)
        }

        findViewById<android.view.View>(R.id.row_theme).setOnClickListener {
            Toast.makeText(this, "Dark mode is coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.row_notifications).setOnClickListener {
            val notificationsEnabled = !preferences.getBoolean("notifications", true)
            preferences.edit().putBoolean("notifications", notificationsEnabled).apply()
            notificationsText.text = if (notificationsEnabled) "On" else "Off"
        }

        findViewById<android.view.View>(R.id.row_sync).setOnClickListener {
            Toast.makeText(this, "No account is connected yet", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.view.View>(R.id.row_logout).setOnClickListener {
            confirmLogout()
        }
    }

    private fun chooseLanguage(languageText: TextView) {
        val languages = arrayOf("English", "isiZulu", "isiXhosa")
        AlertDialog.Builder(this)
            .setTitle("Choose language")
            .setItems(languages) { _, selectedIndex ->
                val selectedLanguage = languages[selectedIndex]
                getSharedPreferences("study_better_settings", MODE_PRIVATE)
                    .edit()
                    .putString("language", selectedLanguage)
                    .apply()
                languageText.text = selectedLanguage
            }
            .show()
    }

    private fun editName(nameText: TextView) {
        val nameInput = EditText(this)
        nameInput.setText(nameText.text)

        AlertDialog.Builder(this)
            .setTitle("Edit profile")
            .setMessage("Enter your full name")
            .setView(nameInput)
            .setPositiveButton("Save") { _, _ ->
                val fullName = nameInput.text.toString().trim()
                if (fullName.isNotEmpty()) {
                    getSharedPreferences("study_better_settings", MODE_PRIVATE)
                        .edit().putString("full_name", fullName).apply()
                    nameText.text = fullName
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editStudyGoal(levelText: TextView) {
        val goalInput = EditText(this)
        goalInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        goalInput.hint = "Minutes per day"

        AlertDialog.Builder(this)
            .setTitle("Study goal")
            .setMessage("How many minutes would you like to study each day?")
            .setView(goalInput)
            .setPositiveButton("Save") { _, _ ->
                val minutes = goalInput.text.toString().toIntOrNull()
                if (minutes == null || minutes <= 0) {
                    Toast.makeText(this, "Enter a valid number of minutes", Toast.LENGTH_SHORT).show()
                } else {
                    getSharedPreferences("study_better_settings", MODE_PRIVATE)
                        .edit().putInt("study_goal", minutes).apply()
                    levelText.text = "Study goal: $minutes minutes"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log out") { _, _ ->
                val loginIntent = Intent(this, login::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
