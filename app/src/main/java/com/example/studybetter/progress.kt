package com.example.studybetter

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator

class progress : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

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

        val preferences = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        val studySeconds = preferences.getLong(
            "study_seconds",
            0L
        )

        val hours = studySeconds / 3600

        val minutes =
            (studySeconds % 3600) / 60

        val assignments =
            AssignmentStorage.getAssignments(this)

        val completedCount =
            assignments.count {
                it.progress == 100
            }

        val xp =
            completedCount * 100 +
                    (studySeconds / 60).toInt() * 10

        val level =
            1 + (completedCount / 3)

        val achievementCount =
            if (completedCount > 0) {
                1
            } else {
                0
            }

        findViewById<TextView>(
            R.id.tv_study_time
        ).text =
            "${hours}h ${minutes}m"

        findViewById<TextView>(
            R.id.tv_assignments_done
        ).text =
            completedCount.toString()

        findViewById<TextView>(
            R.id.tv_study_streak
        ).text =
            if (studySeconds > 0) {
                "Active"
            } else {
                "0 days"
            }

        findViewById<TextView>(
            R.id.tv_xp
        ).text =
            xp.toString()

        findViewById<TextView>(
            R.id.tv_level
        ).text =
            level.toString()

        findViewById<TextView>(
            R.id.tv_achievements
        ).text =
            "$achievementCount/24"

        showModuleProgress()
    }

    private fun showModuleProgress() {

        val container =
            findViewById<LinearLayout>(
                R.id.ll_module_progress
            )

        container.removeAllViews()

        val modules =
            ModuleStorage.getModules(this)

        val assignments =
            AssignmentStorage.getAssignments(this)

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

                setPadding(
                    0,
                    8,
                    0,
                    16
                )
            }

            container.addView(emptyText)

            return
        }

        modules.forEach { module ->

            val moduleAssignments =
                assignments.filter {
                    it.module.equals(
                        module.title,
                        ignoreCase = true
                    )
                }

            val moduleProgress =
                if (moduleAssignments.isEmpty()) {
                    0
                } else {
                    moduleAssignments
                        .sumOf { it.progress } /
                            moduleAssignments.size
                }

            val moduleName =
                TextView(this).apply {

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

                    setPadding(
                        0,
                        0,
                        0,
                        6
                    )
                }

            container.addView(moduleName)

            val progressBar =
                LinearProgressIndicator(this).apply {

                    layoutParams =
                        LinearLayout.LayoutParams(
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