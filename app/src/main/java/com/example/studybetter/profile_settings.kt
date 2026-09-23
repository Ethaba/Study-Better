package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class profile_settings : AppCompatActivity() {

    private lateinit var preferences: android.content.SharedPreferences

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var levelText: TextView
    private lateinit var languageText: TextView
    private lateinit var notificationsText: TextView
    private lateinit var themeText: TextView
    private lateinit var syncStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        preferences = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        nameText = findViewById(R.id.tv_full_name)
        emailText = findViewById(R.id.tv_email)
        levelText = findViewById(R.id.tv_level)
        languageText = findViewById(R.id.tv_current_language)
        notificationsText = findViewById(R.id.tv_notifications_status)
        themeText = findViewById(R.id.tv_current_theme)
        syncStatusText = findViewById(R.id.tv_sync_status)

        loadProfile()

        findViewById<android.view.View>(R.id.btn_back)
            .setOnClickListener {
                finish()
            }

        findViewById<android.view.View>(R.id.row_edit_profile)
            .setOnClickListener {
                editName()
            }

        findViewById<android.view.View>(R.id.row_study_goal)
            .setOnClickListener {
                editStudyGoal()
            }

        findViewById<android.view.View>(R.id.row_language)
            .setOnClickListener {
                showLanguageMessage()
            }

        findViewById<android.view.View>(R.id.row_theme)
            .setOnClickListener {
                showThemeMessage()
            }

        findViewById<android.view.View>(R.id.row_notifications)
            .setOnClickListener {
                toggleNotifications()
            }

        findViewById<android.view.View>(R.id.row_sync)
            .setOnClickListener {
                showSyncStatus()
            }

        findViewById<android.view.View>(R.id.row_logout)
            .setOnClickListener {
                confirmLogout()
            }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {

        val fullName = preferences.getString(
            "full_name",
            "Student"
        ) ?: "Student"

        val email = preferences.getString(
            "email",
            "No account connected"
        ) ?: "No account connected"

        val language = preferences.getString(
            "language",
            "English"
        ) ?: "English"

        val theme = preferences.getString(
            "theme",
            "Light"
        ) ?: "Light"

        val notificationsEnabled =
            preferences.getBoolean(
                "notifications",
                true
            )

        val studyGoal = preferences.getInt(
            "study_goal",
            60
        )

        val assignments =
            AssignmentStorage.getAssignments(this)

        val completedAssignments =
            assignments.count {
                it.progress == 100
            }

        val level =
            1 + (completedAssignments / 3)

        nameText.text = fullName
        emailText.text = email
        levelText.text = "Level $level"

        languageText.text = language

        themeText.text = theme

        notificationsText.text =
            if (notificationsEnabled) {
                "On"
            } else {
                "Off"
            }

        syncStatusText.text = "Connected"
        syncStatusText.setTextColor(
            android.graphics.Color.rgb(67, 160, 71)
        )
    }

    private fun editName() {

        val nameInput = EditText(this)

        nameInput.setText(
            preferences.getString(
                "full_name",
                ""
            )
        )

        nameInput.inputType =
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(this)
            .setTitle("Edit profile")
            .setMessage("Enter your full name")
            .setView(nameInput)
            .setPositiveButton("Save") { _, _ ->

                val fullName =
                    nameInput.text.toString().trim()

                if (fullName.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Please enter your full name",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    preferences.edit()
                        .putString(
                            "full_name",
                            fullName
                        )
                        .apply()

                    nameText.text = fullName

                    Toast.makeText(
                        this,
                        "Profile updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun editStudyGoal() {

        val goalInput = EditText(this)

        goalInput.inputType =
            InputType.TYPE_CLASS_NUMBER

        goalInput.hint = "Minutes per day"

        goalInput.setText(
            preferences.getInt(
                "study_goal",
                60
            ).toString()
        )

        AlertDialog.Builder(this)
            .setTitle("Study goal")
            .setMessage(
                "How many minutes would you like to study each day?"
            )
            .setView(goalInput)
            .setPositiveButton("Save") { _, _ ->

                val minutes =
                    goalInput.text
                        .toString()
                        .toIntOrNull()

                if (
                    minutes == null ||
                    minutes <= 0
                ) {

                    Toast.makeText(
                        this,
                        "Enter a valid number of minutes",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    preferences.edit()
                        .putInt(
                            "study_goal",
                            minutes
                        )
                        .apply()

                    Toast.makeText(
                        this,
                        "Study goal updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun showLanguageMessage() {

        AlertDialog.Builder(this)
            .setTitle("Language")
            .setMessage(
                "Additional languages will be available in a future version."
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun showThemeMessage() {

        AlertDialog.Builder(this)
            .setTitle("Theme")
            .setMessage(
                "Dark mode is coming soon. Study Better currently uses Light mode."
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun toggleNotifications() {

        val currentValue =
            preferences.getBoolean(
                "notifications",
                true
            )

        val newValue = !currentValue

        preferences.edit()
            .putBoolean(
                "notifications",
                newValue
            )
            .apply()

        notificationsText.text =
            if (newValue) {
                "On"
            } else {
                "Off"
            }

        Toast.makeText(
            this,
            if (newValue) {
                "Notifications enabled"
            } else {
                "Notifications disabled"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSyncStatus() {

        AlertDialog.Builder(this)
            .setTitle("Synchronization")
            .setMessage(
                "Your account is connected to the Study Better API. " +
                        "Account authentication is currently active."
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    private fun confirmLogout() {

        AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage(
                "Are you sure you want to log out?"
            )
            .setPositiveButton(
                "Log out"
            ) { _, _ ->

                /*
                 * Clear authentication data so another
                 * user cannot access the previous session.
                 */
                preferences.edit()
                    .remove("full_name")
                    .remove("email")
                    .remove("token")
                    .remove("user_id")
                    .apply()

                val loginIntent =
                    Intent(
                        this,
                        login::class.java
                    )

                loginIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(loginIntent)

                Toast.makeText(
                    this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }
}