package com.example.studybetter

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.slider.Slider

class study_session : AppCompatActivity() {

    private val sessionLengthMillis = 60 * 60 * 1000L
    private var remainingMillis = sessionLengthMillis
    private var isTimerRunning = false
    private var sessionWasSaved = false
    private var timer: CountDownTimer? = null

    private lateinit var timerText: TextView
    private lateinit var sessionProgressText: TextView
    private lateinit var timerProgress: CircularProgressIndicator
    private lateinit var progressSlider: Slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_session)

        timerText = findViewById(R.id.tv_timer)
        sessionProgressText = findViewById(R.id.tv_session_progress)
        timerProgress = findViewById(R.id.progress_timer)
        progressSlider = findViewById(R.id.slider_session_progress)

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btn_start).setOnClickListener { startTimer() }
        findViewById<android.view.View>(R.id.btn_pause).setOnClickListener { pauseTimer() }
        findViewById<android.view.View>(R.id.btn_stop).setOnClickListener { stopTimer() }
        updateTimerScreen()
    }

    private fun startTimer() {
        if (isTimerRunning) return
        if (remainingMillis == 0L) {
            remainingMillis = sessionLengthMillis
            sessionWasSaved = false
        }

        isTimerRunning = true
        Log.d("StudyBetter", "Study session started")
        timer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerScreen()
            }

            override fun onFinish() {
                remainingMillis = 0L
                isTimerRunning = false
                sessionWasSaved = true
                updateTimerScreen()
                saveStudyTime(sessionLengthMillis)
                Toast.makeText(this@study_session, "Study session completed", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun pauseTimer() {
        if (!isTimerRunning) return
        timer?.cancel()
        isTimerRunning = false
        Log.d("StudyBetter", "Study session paused")
    }

    private fun stopTimer() {
        if (sessionWasSaved) {
            remainingMillis = sessionLengthMillis
            sessionWasSaved = false
            updateTimerScreen()
            return
        }

        if (!isTimerRunning && remainingMillis == sessionLengthMillis) return

        timer?.cancel()
        val studiedMillis = sessionLengthMillis - remainingMillis
        isTimerRunning = false
        if (studiedMillis > 0) {
            saveStudyTime(studiedMillis)
            Toast.makeText(this, "Study time recorded", Toast.LENGTH_SHORT).show()
        }

        remainingMillis = sessionLengthMillis
        updateTimerScreen()
    }

    private fun saveStudyTime(studiedMillis: Long) {
        val preferences = getSharedPreferences("study_better_settings", MODE_PRIVATE)
        val totalSeconds = preferences.getLong("study_seconds", 0L) + (studiedMillis / 1000)
        preferences.edit().putLong("study_seconds", totalSeconds).apply()
        Log.d("StudyBetter", "Study session time saved")
    }

    private fun updateTimerScreen() {
        val remainingSeconds = remainingMillis / 1000
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val elapsedMinutes = (sessionLengthMillis - remainingMillis) / 60_000
        val progress = ((sessionLengthMillis - remainingMillis) * 100 / sessionLengthMillis).toInt()

        timerText.text = String.format("%02d:%02d", minutes, seconds)
        sessionProgressText.text = "$elapsedMinutes min / 60 min"
        timerProgress.progress = progress
        progressSlider.value = elapsedMinutes.toFloat()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
