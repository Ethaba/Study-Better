package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class calendar : AppCompatActivity() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var selectedDateText: TextView
    private lateinit var emptyStateText: TextView

    private var selectedTimeMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        selectedDateText = findViewById(R.id.tv_selected_date)
        emptyStateText = findViewById(R.id.tv_empty_state)

        findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbar
        ).setNavigationOnClickListener {
            finish()
        }

        adapter = AssignmentAdapter(emptyList()) { assignment ->
            confirmDelete(assignment)
        }

        findViewById<RecyclerView>(R.id.rv_day_assignments).apply {

            layoutManager = LinearLayoutManager(this@calendar)

            adapter = this@calendar.adapter
        }

        val calendarView =
            findViewById<CalendarView>(R.id.calendar_view)

        selectedTimeMillis =
            savedInstanceState?.getLong(
                "selected_date",
                calendarView.date
            ) ?: calendarView.date

        calendarView.date = selectedTimeMillis

        updateSelectedDateDisplay(selectedTimeMillis)
        showAssignmentsForDate()

        calendarView.setOnDateChangeListener {
                _,
                year,
                month,
                dayOfMonth ->

            val selectedCalendar = Calendar.getInstance().apply {

                set(
                    year,
                    month,
                    dayOfMonth,
                    0,
                    0,
                    0
                )

                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

            selectedTimeMillis =
                selectedCalendar.timeInMillis

            updateSelectedDateDisplay(
                selectedTimeMillis
            )

            showAssignmentsForDate()
        }

        setUpBottomNavigation()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putLong(
            "selected_date",
            selectedTimeMillis
        )

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        showAssignmentsForDate()
    }

    private fun updateSelectedDateDisplay(
        timeMillis: Long
    ) {

        val dateFormat = SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale.getDefault()
        )

        selectedDateText.text =
            dateFormat.format(
                Date(timeMillis)
            )
    }

    private fun showAssignmentsForDate() {

        val allAssignments =
            AssignmentStorage.getAssignments(this)

        val targetCalendar =
            Calendar.getInstance().apply {
                timeInMillis = selectedTimeMillis
            }

        val targetYear =
            targetCalendar.get(Calendar.YEAR)

        val targetMonth =
            targetCalendar.get(Calendar.MONTH)

        val targetDay =
            targetCalendar.get(Calendar.DAY_OF_MONTH)

        val filteredAssignments =
            allAssignments.filter { assignment ->

                val assignmentCalendar =
                    Calendar.getInstance().apply {
                        timeInMillis =
                            assignment.dueDateMillis
                    }

                assignmentCalendar.get(Calendar.YEAR) ==
                        targetYear &&
                        assignmentCalendar.get(Calendar.MONTH) ==
                        targetMonth &&
                        assignmentCalendar.get(Calendar.DAY_OF_MONTH) ==
                        targetDay
            }.sortedBy {
                it.dueDateMillis
            }

        adapter.updateAssignments(
            filteredAssignments
        )

        emptyStateText.visibility =
            if (filteredAssignments.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun confirmDelete(
        assignment: Assignment
    ) {

        AlertDialog.Builder(this)
            .setTitle("Delete assignment")
            .setMessage(
                "Delete ${assignment.title}?"
            )
            .setPositiveButton("Delete") { _, _ ->

                AssignmentStorage.deleteAssignment(
                    this,
                    assignment.id
                )

                showAssignmentsForDate()

            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun setUpBottomNavigation() {

        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottom_nav
            )

        bottomNav.selectedItemId =
            R.id.nav_calendar

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> {

                    startActivity(
                        Intent(
                            this,
                            dashboard::class.java
                        )
                    )

                    true
                }

                R.id.nav_modules -> {

                    startActivity(
                        Intent(
                            this,
                            modules::class.java
                        )
                    )

                    true
                }

                R.id.nav_progress -> {

                    startActivity(
                        Intent(
                            this,
                            progress::class.java
                        )
                    )

                    true
                }

                else -> false
            }
        }
    }
}