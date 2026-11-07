
package com.example.studybetter

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studybetter.api.RetrofitClient
import com.example.studybetter.api.StudySessionRequest
import com.example.studybetter.api.StudySessionResponse
import com.example.studybetter.data.StudySessionRoomRepository
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.studybetter.data.AssignmentRoomRepository
import com.example.studybetter.data.ModuleRoomRepository

class study_session : AppCompatActivity() {

    private val sessionLengthMillis = 60 * 60 * 1000L

    private var remainingMillis = sessionLengthMillis
    private var isTimerRunning = false
    private var sessionWasSaved = false
    private var sessionStartTime = 0L

    private var timer: CountDownTimer? = null

    private lateinit var timerText: TextView
    private lateinit var sessionProgressText: TextView
    private lateinit var timerProgress: CircularProgressIndicator
    private lateinit var progressSlider: Slider
    private lateinit var currentTaskText: TextView
    private lateinit var sessionRepository: StudySessionRoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        sessionRepository = StudySessionRoomRepository(this)

        setContentView(R.layout.activity_study_session)

        timerText = findViewById(R.id.tv_timer)
        sessionProgressText = findViewById(R.id.tv_session_progress)
        timerProgress = findViewById(R.id.progress_timer)
        progressSlider = findViewById(R.id.slider_session_progress)
        currentTaskText = findViewById(R.id.tv_current_task)

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<android.view.View>(R.id.btn_start).setOnClickListener {
            startTimer()
        }

        findViewById<android.view.View>(R.id.btn_pause).setOnClickListener {
            pauseTimer()
        }

        findViewById<android.view.View>(R.id.btn_stop).setOnClickListener {
            stopTimer()
        }

        updateTimerScreen()
    }

    private fun startTimer() {
        if (isTimerRunning) return

        if (remainingMillis == 0L) {
            remainingMillis = sessionLengthMillis
            sessionWasSaved = false
            sessionStartTime = 0L
        }

        if (sessionStartTime == 0L) {
            sessionStartTime = System.currentTimeMillis()
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

                updateTimerScreen()
                saveStudySession()

                Toast.makeText(
                    this@study_session,
                    "Study session completed",
                    Toast.LENGTH_SHORT
                ).show()
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
            resetTimer()
            return
        }

        if (!isTimerRunning && remainingMillis == sessionLengthMillis) {
            return
        }

        timer?.cancel()
        isTimerRunning = false

        val studiedMillis = sessionLengthMillis - remainingMillis

        if (studiedMillis >= 1000L) {
            saveStudySession()

            Toast.makeText(
                this,
                "Study time recorded",
                Toast.LENGTH_SHORT
            ).show()
        }

        resetTimer()
    }

    private fun saveStudySession() {
        if (sessionWasSaved) return

        val studiedSeconds =
            ((sessionLengthMillis - remainingMillis) / 1000).toInt()

        if (studiedSeconds <= 0) return

        sessionWasSaved = true

        val startedAt = formatDate(
            if (sessionStartTime == 0L) {
                System.currentTimeMillis() - studiedSeconds * 1000L
            } else {
                sessionStartTime
            }
        )

        val taskDescription = currentTaskText.text.toString()
            .takeIf {
                it.isNotBlank() && it != "No task selected"
            } ?: "Study session"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Save the session to Room before attempting the upload.
                val localId = withContext(Dispatchers.IO) {
                    sessionRepository.saveSession(
                        startedAt = startedAt,
                        durationSeconds = studiedSeconds,
                        description = taskDescription
                    )
                }

                // Update the existing total study time.
                val preferences = getSharedPreferences(
                    "study_better_settings",
                    MODE_PRIVATE
                )

                val totalSeconds =
                    preferences.getLong("study_seconds", 0L) + studiedSeconds

                preferences.edit()
                    .putLong("study_seconds", totalSeconds)
                    .apply()

                Log.d("StudyBetter", "Study session saved to Room")

                // Check whether this session unlocked a new achievement.
                val assignmentRepository = AssignmentRoomRepository(this@study_session)
                val moduleRepository = ModuleRoomRepository(this@study_session)

                val achievementData = withContext(Dispatchers.IO) {
                    Triple(
                        assignmentRepository.getAssignments(),
                        moduleRepository.getModules(),
                        sessionRepository.getSessions()
                    )
                }

                val totalStudySeconds = achievementData.third.sumOf {
                    it.durationSeconds.toLong()
                }

                AchievementNotificationManager.checkAndNotify(
                    this@study_session,
                    achievementData.first,
                    achievementData.second,
                    totalStudySeconds
                )

                uploadStudySession(
                    localId,
                    startedAt,
                    studiedSeconds,
                    taskDescription
                )

            } catch (e: Exception) {
                Log.e("StudyBetter", "Could not save session to Room", e)

                Toast.makeText(
                    this@study_session,
                    "Could not save study session.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun uploadStudySession(
        localId: Long,
        startedAt: String,
        durationSeconds: Int,
        description: String
    ) {
        val request = StudySessionRequest(
            startedAt = startedAt,
            durationSeconds = durationSeconds,
            description = description
        )

        try {
            RetrofitClient.api.createStudySession(request)
                .enqueue(object : Callback<StudySessionResponse> {

                    override fun onResponse(
                        call: Call<StudySessionResponse>,
                        response: Response<StudySessionResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {

                            val serverId =
                                response.body()!!.studySessionID

                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        sessionRepository.markSynced(
                                            localId,
                                            serverId
                                        )
                                    }

                                    Log.d(
                                        "StudyBetter",
                                        "Study session uploaded successfully"
                                    )

                                } catch (e: Exception) {
                                    Log.e(
                                        "StudyBetter",
                                        "Could not update session sync status",
                                        e
                                    )
                                }
                            }

                        } else {
                            Log.e(
                                "StudyBetter",
                                "Study session upload failed: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<StudySessionResponse>,
                        t: Throwable
                    ) {
                        Log.e(
                            "StudyBetter",
                            "Could not upload study session. It remains pending.",
                            t
                        )
                    }
                })

        } catch (e: Exception) {
            Log.e("StudyBetter", "Study session API error", e)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        )

        formatter.timeZone = TimeZone.getTimeZone("UTC")

        return formatter.format(Date(timestamp))
    }

    private fun resetTimer() {
        timer?.cancel()

        remainingMillis = sessionLengthMillis
        isTimerRunning = false
        sessionWasSaved = false
        sessionStartTime = 0L

        updateTimerScreen()
    }

    private fun updateTimerScreen() {
        val remainingSeconds = remainingMillis / 1000
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60

        val elapsedMinutes =
            (sessionLengthMillis - remainingMillis) / 60_000

        val progress =
            ((sessionLengthMillis - remainingMillis) * 100 /
                    sessionLengthMillis).toInt()

        timerText.text = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )

        sessionProgressText.text = "$elapsedMinutes min / 60 min"

        timerProgress.progress = progress
        progressSlider.value = elapsedMinutes.toFloat()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
