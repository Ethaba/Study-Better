package com.example.studybetter

import android.content.Intent
import android.os.Bundle
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

        languageText.text = preferences.getString("language", "English")
        notificationsText.text = if (preferences.getBoolean("notifications", true)) "On" else "Off"

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener { finish() }

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
