package com.example.studybetter

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybetter.data.StudySessionRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class study_session_history : AppCompatActivity() {

    private lateinit var repository: StudySessionRoomRepository

    private lateinit var totalStudyTime: TextView
    private lateinit var sessionCount: TextView
    private lateinit var emptyHistory: TextView
    private lateinit var historyContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_session_history)

        repository = StudySessionRoomRepository(this)

        totalStudyTime = findViewById(R.id.tv_total_study_time)
        sessionCount = findViewById(R.id.tv_session_count)
        emptyHistory = findViewById(R.id.tv_empty_history)
        historyContainer = findViewById(R.id.ll_session_history)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val sessions = withContext(Dispatchers.IO) {
                    repository.getSessions()
                }

                displayHistory(sessions)

            } catch (e: Exception) {
                totalStudyTime.text = "Unable to load study history"
                emptyHistory.visibility = View.VISIBLE
                emptyHistory.text = "Please try again later."
            }
        }
    }

    private fun displayHistory(
        sessions: List<com.example.studybetter.data.StudySessionEntity>
    ) {
        historyContainer.removeAllViews()

        val totalSeconds = sessions.sumOf {
            it.durationSeconds.toLong()
        }

        totalStudyTime.text =
            "Total study time: ${formatDuration(totalSeconds)}"

        sessionCount.text =
            "${sessions.size} sessions completed"

        if (sessions.isEmpty()) {
            emptyHistory.visibility = View.VISIBLE
            return
        }

        emptyHistory.visibility = View.GONE

        sessions.forEach { session ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(
                    android.graphics.Color.rgb(245, 247, 250)
                )

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
            }

            val description = TextView(this).apply {
                text = session.description
                textSize = 16f
                setTextColor(
                    android.graphics.Color.rgb(17, 17, 17)
                )
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val date = TextView(this).apply {
                text = formatDate(session.startedAt)
                textSize = 13f
                setTextColor(
                    android.graphics.Color.rgb(102, 102, 102)
                )
                setPadding(0, 6, 0, 4)
            }

            val duration = TextView(this).apply {
                text = "Duration: ${formatDuration(session.durationSeconds.toLong())}"
                textSize = 14f
                setTextColor(
                    android.graphics.Color.rgb(66, 165, 245)
                )
            }

            val deleteButton = TextView(this).apply {
                text = "Delete session"
                textSize = 14f
                gravity = Gravity.END
                setTextColor(
                    android.graphics.Color.rgb(229, 57, 53)
                )
                setPadding(0, 12, 0, 0)

                setOnClickListener {
                    confirmDelete(session.localId)
                }
            }

            card.addView(description)
            card.addView(date)
            card.addView(duration)
            card.addView(deleteButton)

            historyContainer.addView(card)
        }
    }

    private fun confirmDelete(localId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Delete study session")
            .setMessage(
                "Are you sure you want to delete this study session?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            repository.deleteSession(localId)
                        }

                        loadHistory()

                    } catch (e: Exception) {
                        AlertDialog.Builder(this@study_session_history)
                            .setMessage("Could not delete this session.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes} min"
            else -> "$seconds sec"
        }
    }

    private fun formatDate(value: String): String {
        return try {
            val input = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.US
            )

            val date = input.parse(value) ?: return value

            SimpleDateFormat(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault()
            ).format(date)

        } catch (e: Exception) {
            value
        }
    }
}
