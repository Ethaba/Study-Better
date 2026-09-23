package com.example.studybetter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class achievements : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var summaryText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        summaryText = findViewById(R.id.tv_summary)
        recyclerView = findViewById(R.id.rv_achievements)

        findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbar
        ).setNavigationOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        setUpBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadAchievements()
    }

    private fun loadAchievements() {

        val assignments = AssignmentStorage.getAssignments(this)
        val modules = ModuleStorage.getModules(this)

        val preferences = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        val studySeconds = preferences.getLong(
            "study_seconds",
            0L
        )

        val completedAssignments = assignments.count {
            it.progress == 100
        }

        val achievements = listOf(

            Achievement(
                "First Steps",
                "Add your first module",
                modules.isNotEmpty()
            ),

            Achievement(
                "Getting Started",
                "Add your first assignment",
                assignments.isNotEmpty()
            ),

            Achievement(
                "Assignment Complete",
                "Complete your first assignment",
                completedAssignments >= 1
            ),

            Achievement(
                "Triple Threat",
                "Complete 3 assignments",
                completedAssignments >= 3
            ),

            Achievement(
                "Assignment Master",
                "Complete 5 assignments",
                completedAssignments >= 5
            ),

            Achievement(
                "Study Session",
                "Complete your first study session",
                studySeconds > 0
            ),

            Achievement(
                "One Hour",
                "Study for a total of 60 minutes",
                studySeconds >= 60 * 60
            ),

            Achievement(
                "Study Time",
                "Study for a total of 5 hours",
                studySeconds >= 5 * 60 * 60
            ),

            Achievement(
                "Dedicated Student",
                "Study for a total of 10 hours",
                studySeconds >= 10 * 60 * 60
            ),

            Achievement(
                "10 Assignments",
                "Complete 10 assignments",
                completedAssignments >= 10
            ),

            Achievement(
                "15 Assignments",
                "Complete 15 assignments",
                completedAssignments >= 15
            ),

            Achievement(
                "20 Assignments",
                "Complete 20 assignments",
                completedAssignments >= 20
            ),

            Achievement(
                "Module Builder",
                "Add 3 modules",
                modules.size >= 3
            ),

            Achievement(
                "Module Collector",
                "Add 5 modules",
                modules.size >= 5
            ),

            Achievement(
                "Busy Student",
                "Have 5 active assignments",
                assignments.count { it.progress < 100 } >= 5
            ),

            Achievement(
                "Organised",
                "Have 10 assignments saved",
                assignments.size >= 10
            ),

            Achievement(
                "Halfway There",
                "Complete 12 assignments",
                completedAssignments >= 12
            ),

            Achievement(
                "Study Champion",
                "Study for a total of 20 hours",
                studySeconds >= 20 * 60 * 60
            ),

            Achievement(
                "25 Assignments",
                "Complete 25 assignments",
                completedAssignments >= 25
            ),

            Achievement(
                "30 Assignments",
                "Complete 30 assignments",
                completedAssignments >= 30
            ),

            Achievement(
                "Long Study Session",
                "Study for a total of 30 hours",
                studySeconds >= 30 * 60 * 60
            ),

            Achievement(
                "40 Assignments",
                "Complete 40 assignments",
                completedAssignments >= 40
            ),

            Achievement(
                "50 Assignments",
                "Complete 50 assignments",
                completedAssignments >= 50
            ),

            Achievement(
                "Study Legend",
                "Study for a total of 50 hours",
                studySeconds >= 50 * 60 * 60
            )
        )

        val unlockedCount = achievements.count {
            it.unlocked
        }

        summaryText.text =
            "$unlockedCount of ${achievements.size} achievements unlocked"

        recyclerView.adapter = AchievementAdapter(achievements)
    }

    private fun setUpBottomNavigation() {

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(
                        Intent(this, dashboard::class.java)
                    )
                    true
                }

                R.id.nav_modules -> {
                    startActivity(
                        Intent(this, modules::class.java)
                    )
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(
                        Intent(this, calendar::class.java)
                    )
                    true
                }

                R.id.nav_progress -> {
                    startActivity(
                        Intent(this, progress::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val unlocked: Boolean
)

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val icon: ImageView =
            view.findViewById(android.R.id.icon)

        val title: TextView =
            view.findViewById(android.R.id.text1)

        val description: TextView =
            view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(
        parent: android.view.ViewGroup,
        viewType: Int
    ): AchievementViewHolder {

        val context = parent.context

        val card = MaterialCardView(context).apply {

            layoutParams =
                RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        0,
                        0,
                        0,
                        12
                    )
                }

            radius = 14f
            cardElevation = 0f
            strokeWidth = 1
            strokeColor = Color.LTGRAY
            setCardBackgroundColor(Color.WHITE)
        }

        val row = LinearLayout(context).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                16,
                14,
                16,
                14
            )
        }

        val icon = ImageView(context).apply {

            id = android.R.id.icon

            layoutParams =
                LinearLayout.LayoutParams(
                    48,
                    48
                )

            setPadding(
                10,
                10,
                10,
                10
            )
        }

        val textContainer = LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL

            layoutParams =
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = 14
                }
        }

        val title = TextView(context).apply {

            id = android.R.id.text1

            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(
                null,
                android.graphics.Typeface.BOLD
            )
        }

        val description = TextView(context).apply {

            id = android.R.id.text2

            textSize = 13f
            setTextColor(Color.DKGRAY)

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
        }

        textContainer.addView(title)
        textContainer.addView(description)

        row.addView(icon)
        row.addView(textContainer)

        card.addView(row)

        return AchievementViewHolder(card)
    }

    override fun onBindViewHolder(
        holder: AchievementViewHolder,
        position: Int
    ) {

        val achievement = achievements[position]

        holder.title.text = achievement.title
        holder.description.text = achievement.description

        if (achievement.unlocked) {

            holder.icon.setImageResource(
                android.R.drawable.star_big_on
            )

            holder.icon.setBackgroundColor(
                Color.rgb(255, 193, 7)
            )

            holder.title.setTextColor(Color.BLACK)

            holder.description.setTextColor(
                Color.DKGRAY
            )

        } else {

            holder.icon.setImageResource(
                android.R.drawable.ic_lock_lock
            )

            holder.icon.setBackgroundColor(
                Color.LTGRAY
            )

            holder.title.setTextColor(
                Color.GRAY
            )

            holder.description.setTextColor(
                Color.GRAY
            )
        }
    }

    override fun getItemCount(): Int =
        achievements.size
}