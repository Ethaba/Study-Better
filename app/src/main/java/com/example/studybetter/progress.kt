
package com.example.studybetter

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybetter.data.AssignmentRoomRepository
import com.example.studybetter.data.ModuleRoomRepository
import com.example.studybetter.data.StudySessionRoomRepository
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class progress : AppCompatActivity() {

    private lateinit var assignmentRepository: AssignmentRoomRepository
    private lateinit var moduleRepository: ModuleRoomRepository
    private lateinit var studySessionRepository: StudySessionRoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        assignmentRepository = AssignmentRoomRepository(this)
        moduleRepository = ModuleRoomRepository(this)
        studySessionRepository = StudySessionRoomRepository(this)

        findViewById<android.view.View>(
            R.id.btn_back
        ).setOnClickListener {
            finish()
        }

        refreshProgress()
    }

    override fun onResume() {
        super.onResume()
        refreshProgress()
    }

    private fun refreshProgress() {

        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    Triple(
                        assignmentRepository.getAssignments(),
                        moduleRepository.getModules(),
                        studySessionRepository.getSessions()
                    )
                }

                val assignments = data.first
                val modules = data.second
                val sessions = data.third

                val studySeconds = sessions.sumOf {
                    it.durationSeconds.toLong()
                }

                val hours = studySeconds / 3600
                val minutes = (studySeconds % 3600) / 60

                val completedCount = assignments.count {
                    it.progress == 100
                }

                val xp = completedCount * 100 +
                        (studySeconds / 60).toInt() * 10

                val level = 1 + (completedCount / 3)

                val unlockedAchievements = mutableListOf<String>()

                if (modules.isNotEmpty()) {
                    unlockedAchievements.add("First Steps")
                }

                if (assignments.isNotEmpty()) {
                    unlockedAchievements.add("Getting Started")
                }

                if (completedCount >= 1) {
                    unlockedAchievements.add("Assignment Complete")
                }

                if (completedCount >= 3) {
                    unlockedAchievements.add("Triple Threat")
                }

                if (studySeconds >= 3600) {
                    unlockedAchievements.add("One Hour")
                }

                findViewById<TextView>(
                    R.id.tv_study_time
                ).text = "${hours}h ${minutes}m"

                findViewById<TextView>(
                    R.id.tv_assignments_done
                ).text = completedCount.toString()

                findViewById<TextView>(
                    R.id.tv_study_streak
                ).text = if (studySeconds > 0) {
                    "Active"
                } else {
                    "0 days"
                }

                findViewById<TextView>(
                    R.id.tv_xp
                ).text = xp.toString()

                findViewById<TextView>(
                    R.id.tv_level
                ).text = level.toString()

                findViewById<TextView>(
                    R.id.tv_achievements
                ).text = "${unlockedAchievements.size}/24"

                showModuleProgress(modules, assignments)

            } catch (e: Exception) {
                Log.e("StudyBetter", "Could not load progress", e)
            }
        }
    }

    private fun showModuleProgress(
        modules: List<Module>,
        assignments: List<Assignment>
    ) {

        val container = findViewById<LinearLayout>(
            R.id.ll_module_progress
        )

        container.removeAllViews()

        if (modules.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No modules added yet."
                textSize = 14f
                setTextColor(
                    resources.getColor(
                        android.R.color.darker_gray,
                        theme
                    )
                )
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 16)
            }

            container.addView(emptyText)
            return
        }

        modules.forEach { module ->

            val moduleAssignments = assignments.filter {
                it.module.equals(
                    module.title,
                    ignoreCase = true
                )
            }

            val moduleProgress =
                if (moduleAssignments.isEmpty()) {
                    0
                } else {
                    moduleAssignments.sumOf {
                        it.progress
                    } / moduleAssignments.size
                }

            val moduleName = TextView(this).apply {
                text = module.title

                setTextColor(
                    resources.getColor(
                        android.R.color.black,
                        theme
                    )
                )

                textSize = 15f

                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                setPadding(0, 0, 0, 6)
            }

            container.addView(moduleName)

            val progressBar =
                LinearProgressIndicator(this).apply {

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 20
                    }

                    progress = moduleProgress
                    trackThickness = 8
                    trackCornerRadius = 4

                    setIndicatorColor(
                        resources.getColor(
                            android.R.color.black,
                            theme
                        )
                    )

                    setTrackColor(
                        resources.getColor(
                            android.R.color.darker_gray,
                            theme
                        )
                    )
                }

            container.addView(progressBar)
        }
    }
}
