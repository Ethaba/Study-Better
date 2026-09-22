package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class assignments : AppCompatActivity() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var emptyState: View
    private var selectedFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        emptyState = findViewById(R.id.ll_empty_state)
        adapter = AssignmentAdapter(emptyList()) { assignment -> confirmDelete(assignment) }

        findViewById<RecyclerView>(R.id.rv_assignments).apply {
            layoutManager = LinearLayoutManager(this@assignments)
            adapter = this@assignments.adapter
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }
        findViewById<View>(R.id.fab_add_assignment).setOnClickListener {
            startActivity(Intent(this, dialog_add_edit_assignment::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_filter).setOnClickListener { button -> 
            showFilterMenu(button as MaterialButton) 
        }
        setUpBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        showAssignments()
    }

    private fun showAssignments() {
        val allAssignments = AssignmentStorage.getAssignments(this)
        val filteredAssignments = when (selectedFilter) {
            "Completed" -> allAssignments.filter { it.progress == 100 }
            "Upcoming" -> allAssignments.filter { it.progress < 100 }
            "Due soon" -> allAssignments.filter { it.priority == "High" && it.progress < 100 }
            else -> allAssignments
        }.sortedBy { it.dueDateMillis }

        adapter.updateAssignments(filteredAssignments)
        emptyState.visibility = if (filteredAssignments.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showFilterMenu(button: MaterialButton) {
        val menu = PopupMenu(this, button)
        val filters = listOf("All", "Upcoming", "Due soon", "Completed")
        filters.forEach { menu.menu.add(it) }
        menu.setOnMenuItemClickListener { item ->
            selectedFilter = item.title.toString()
            button.text = selectedFilter
            showAssignments()
            true
        }
        menu.show()
    }

    private fun confirmDelete(assignment: Assignment) {
        AlertDialog.Builder(this)
            .setTitle("Delete assignment")
            .setMessage("Delete ${assignment.title}?")
            .setPositiveButton("Delete") { _, _ ->
                AssignmentStorage.deleteAssignment(this, assignment.id)
                showAssignments()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setUpBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_nav).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> startActivity(Intent(this, dashboard::class.java))
                R.id.nav_modules -> startActivity(Intent(this, modules::class.java))
                R.id.nav_calendar -> startActivity(Intent(this, calendar::class.java))
                R.id.nav_progress -> startActivity(Intent(this, progress::class.java))
            }
            true
        }
    }
}
