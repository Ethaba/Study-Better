
package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybetter.api.RetrofitClient
import com.example.studybetter.api.AssignmentResponse
import com.example.studybetter.data.AssignmentRoomRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class assignments : AppCompatActivity() {

    private lateinit var adapter: AssignmentAdapter
    private lateinit var emptyState: View
    private lateinit var assignmentRepository: AssignmentRoomRepository

    private var selectedFilter = "All"
    private var isLoading = false
    private var currentAssignments = emptyList<Assignment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        assignmentRepository = AssignmentRoomRepository(this)

        setContentView(R.layout.activity_assignments)

        emptyState = findViewById(R.id.ll_empty_state)

        adapter = AssignmentAdapter(emptyList()) { assignment ->
            confirmDelete(assignment)
        }

        findViewById<RecyclerView>(R.id.rv_assignments).apply {
            layoutManager = LinearLayoutManager(this@assignments)
            adapter = this@assignments.adapter
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbar
        ).setNavigationOnClickListener {
            finish()
        }

        findViewById<View>(R.id.fab_add_assignment)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        dialog_add_edit_assignment::class.java
                    )
                )
            }

        findViewById<MaterialButton>(R.id.btn_filter)
            .setOnClickListener { button ->
                showFilterMenu(button as MaterialButton)
            }

        setUpBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        refreshAssignments()
    }

    private fun displayAssignments() {

        val filteredAssignments = when (selectedFilter) {

            "Completed" -> currentAssignments.filter {
                it.progress == 100
            }

            "Upcoming" -> currentAssignments.filter {
                it.progress < 100
            }

            "Due soon" -> {
                val now = System.currentTimeMillis()
                val sevenDaysFromNow =
                    now + (7L * 24 * 60 * 60 * 1000)

                currentAssignments.filter {
                    it.progress < 100 &&
                            it.dueDateMillis >= now &&
                            it.dueDateMillis <= sevenDaysFromNow
                }
            }

            else -> currentAssignments
        }.sortedBy {
            it.dueDateMillis
        }

        adapter.updateAssignments(filteredAssignments)

        emptyState.visibility =
            if (filteredAssignments.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun refreshAssignments() {

        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.Main).launch {
            try {

                // Load saved assignments from Room first.
                val localAssignments = withContext(Dispatchers.IO) {
                    assignmentRepository.getAssignments()
                }

                currentAssignments = localAssignments
                displayAssignments()

                // Refresh from the server when a connection is available.
                try {
                    val serverAssignments = withContext(Dispatchers.IO) {

                        val response = RetrofitClient.api.getAssignments()

                        if (!response.isSuccessful) {
                            throw Exception(
                                "Server returned ${response.code()}"
                            )
                        }

                        (response.body() ?: emptyList())
                            .map { it.toLocalAssignment() }
                    }

                    withContext(Dispatchers.IO) {
                        assignmentRepository.replaceServerAssignments(
                            serverAssignments
                        )
                    }

                    // Reload Room so pending local changes remain visible.
                    currentAssignments = withContext(Dispatchers.IO) {
                        assignmentRepository.getAssignments()
                    }

                    displayAssignments()

                } catch (serverError: Exception) {
                    // Keep showing locally saved assignments if offline.
                    Toast.makeText(
                        this@assignments,
                        "Showing saved assignments. Server sync is unavailable.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@assignments,
                    "Could not load assignments.",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {
                isLoading = false
            }
        }
    }

    private fun AssignmentResponse.toLocalAssignment(): Assignment {
        return Assignment(
            id = assignmentID.toLong(),
            title = title,
            description = description,
            module = moduleName,
            dueDateMillis = dueDateMillis,
            priority = priority,
            progress = progress
        )
    }

    private fun showFilterMenu(button: MaterialButton) {

        val menu = PopupMenu(this, button)

        val filters = listOf(
            "All",
            "Upcoming",
            "Due soon",
            "Completed"
        )

        filters.forEach {
            menu.menu.add(it)
        }

        menu.setOnMenuItemClickListener { item ->

            selectedFilter = item.title.toString()
            button.text = selectedFilter

            displayAssignments()

            true
        }

        menu.show()
    }


    private fun confirmDelete(assignment: Assignment) {

        AlertDialog.Builder(this)
            .setTitle("Delete assignment")
            .setMessage("Delete ${assignment.title}?")
            .setPositiveButton("Delete") { _, _ ->

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // Record deletion locally first.
                        withContext(Dispatchers.IO) {
                            assignmentRepository.deleteAssignment(
                                assignment.id
                            )
                        }

                        // Refresh the screen from Room.
                        loadAssignmentsFromRoom()

                        Toast.makeText(
                            this@assignments,
                            "Assignment deleted",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@assignments,
                            "Could not delete assignment.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private suspend fun loadAssignmentsFromRoom() {
        currentAssignments = withContext(Dispatchers.IO) {
            assignmentRepository.getAssignments()
        }

        displayAssignments()
    }

    private fun setUpBottomNavigation() {

        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setOnItemSelectedListener { item ->

                when (item.itemId) {

                    R.id.nav_dashboard -> {
                        startActivity(
                            Intent(this, dashboard::class.java)
                        )
                    }

                    R.id.nav_modules -> {
                        startActivity(
                            Intent(this, modules::class.java)
                        )
                    }

                    R.id.nav_calendar -> {
                        startActivity(
                            Intent(this, calendar::class.java)
                        )
                    }

                    R.id.nav_progress -> {
                        startActivity(
                            Intent(this, progress::class.java)
                        )
                    }
                }

                true
            }
    }
}
