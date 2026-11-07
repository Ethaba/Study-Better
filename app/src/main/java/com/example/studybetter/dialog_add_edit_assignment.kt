
package com.example.studybetter

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studybetter.api.AssignmentRequest
import com.example.studybetter.api.RetrofitClient
import com.example.studybetter.data.AssignmentRoomRepository
import com.example.studybetter.data.ModuleRoomRepository
import com.example.studybetter.data.StudySessionRoomRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class dialog_add_edit_assignment : AppCompatActivity() {

    private val dueDate = Calendar.getInstance()

    private lateinit var dueDateInput: TextInputEditText
    private lateinit var progressText: TextView
    private lateinit var assignmentRepository: AssignmentRoomRepository
    private lateinit var moduleRepository: ModuleRoomRepository

    private var selectedPriority = "Low"
    private var editingAssignmentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)

        assignmentRepository = AssignmentRoomRepository(this)
        moduleRepository = ModuleRoomRepository(this)

        setContentView(R.layout.activity_dialog_add_edit_assignment)

        dueDateInput = findViewById(R.id.et_due_date)
        progressText = findViewById(R.id.tv_progress_value)

        editingAssignmentId = intent.getLongExtra(
            "assignment_id",
            -1L
        ).takeIf { it != -1L }

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        setUpModuleMenu()
        setUpDatePicker()
        setUpProgressSlider()
        setUpPriorityButtons()

        if (editingAssignmentId != null) {
            loadAssignmentForEditing()
        }

        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {
            saveAssignment()
        }
    }

    private fun setUpModuleMenu() {
        val moduleInput =
            findViewById<AutoCompleteTextView>(R.id.act_module)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val savedModules = withContext(Dispatchers.IO) {
                    moduleRepository.getModules()
                }

                val moduleNames = mutableListOf("General")
                savedModules.forEach { module ->
                    moduleNames.add(module.title)
                }

                val adapter = ArrayAdapter(
                    this@dialog_add_edit_assignment,
                    android.R.layout.simple_dropdown_item_1line,
                    moduleNames
                )

                moduleInput.setAdapter(adapter)

            } catch (e: Exception) {
                Toast.makeText(
                    this@dialog_add_edit_assignment,
                    "Could not load modules.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpDatePicker() {
        dueDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dueDate.set(year, month, day, 0, 0, 0)
                    dueDate.set(Calendar.MILLISECOND, 0)

                    dueDateInput.setText(
                        SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                        ).format(dueDate.time)
                    )

                    selectedPriority =
                        AssignmentPriority.fromDaysUntilDue(daysUntilDue())
                },
                dueDate.get(Calendar.YEAR),
                dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setUpProgressSlider() {
        findViewById<Slider>(R.id.slider_progress)
            .addOnChangeListener { _, value, _ ->
                progressText.text = "${value.toInt()}%"
            }
    }

    private fun setUpPriorityButtons() {
        findViewById<MaterialButton>(R.id.btn_priority_low)
            .setOnClickListener { selectedPriority = "Low" }

        findViewById<MaterialButton>(R.id.btn_priority_medium)
            .setOnClickListener { selectedPriority = "Medium" }

        findViewById<MaterialButton>(R.id.btn_priority_high)
            .setOnClickListener { selectedPriority = "High" }
    }

    private fun loadAssignmentForEditing() {
        val assignmentId = editingAssignmentId ?: return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val assignment = withContext(Dispatchers.IO) {
                    assignmentRepository.getAssignments()
                        .firstOrNull { it.id == assignmentId }
                } ?: return@launch

                findViewById<TextInputEditText>(R.id.et_title)
                    .setText(assignment.title)

                findViewById<TextInputEditText>(R.id.et_description)
                    .setText(assignment.description)

                findViewById<AutoCompleteTextView>(R.id.act_module)
                    .setText(assignment.module, false)

                dueDate.timeInMillis = assignment.dueDateMillis

                dueDateInput.setText(
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(dueDate.time)
                )

                selectedPriority = assignment.priority

                findViewById<Slider>(R.id.slider_progress).value =
                    assignment.progress.toFloat()

                progressText.text = "${assignment.progress}%"

                findViewById<MaterialButton>(R.id.btn_save).text =
                    "Update Assignment"

            } catch (e: Exception) {
                Toast.makeText(
                    this@dialog_add_edit_assignment,
                    "Could not load assignment.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveAssignment() {
        val titleLayout = findViewById<TextInputLayout>(R.id.til_title)
        val moduleLayout = findViewById<TextInputLayout>(R.id.til_module)
        val dueDateLayout = findViewById<TextInputLayout>(R.id.til_due_date)

        val title = findViewById<TextInputEditText>(R.id.et_title)
            .text.toString().trim()

        val description =
            findViewById<TextInputEditText>(R.id.et_description)
                .text.toString().trim()

        val module =
            findViewById<AutoCompleteTextView>(R.id.act_module)
                .text.toString().trim()

        val progress =
            findViewById<Slider>(R.id.slider_progress).value.toInt()

        titleLayout.error = null
        moduleLayout.error = null
        dueDateLayout.error = null

        if (title.isEmpty()) {
            titleLayout.error = "Please enter an assignment title"
            return
        }

        if (module.isEmpty()) {
            moduleLayout.error = "Please select a module"
            return
        }

        if (dueDateInput.text.isNullOrBlank()) {
            dueDateLayout.error = "Please select a due date"
            return
        }

        selectedPriority =
            AssignmentPriority.fromDaysUntilDue(daysUntilDue())

        val assignmentId =
            editingAssignmentId ?: System.currentTimeMillis()

        val assignment = Assignment(
            id = assignmentId,
            title = title,
            description = description,
            module = module,
            dueDateMillis = dueDate.timeInMillis,
            priority = selectedPriority,
            progress = progress
        )

        saveAssignmentToServer(assignment)
    }

    private fun saveAssignmentToServer(assignment: Assignment) {
        CoroutineScope(Dispatchers.Main).launch {
            val saveButton = findViewById<MaterialButton>(R.id.btn_save)
            saveButton.isEnabled = false

            try {
                val selectedModule = withContext(Dispatchers.IO) {
                    moduleRepository.getModules()
                        .firstOrNull { it.title == assignment.module }
                }

                val moduleId = selectedModule
                    ?.id
                    ?.takeIf { it <= Int.MAX_VALUE }
                    ?.toInt()

                val request = AssignmentRequest(
                    title = assignment.title,
                    description = assignment.description,
                    moduleID = moduleId,
                    moduleName = assignment.module,
                    dueDateMillis = assignment.dueDateMillis,
                    priority = assignment.priority,
                    progress = assignment.progress
                )

                val response = withContext(Dispatchers.IO) {
                    if (assignment.id > Int.MAX_VALUE) {
                        RetrofitClient.api.createAssignment(request)
                    } else {
                        RetrofitClient.api.updateAssignment(
                            assignment.id.toInt(),
                            request
                        )
                    }
                }

                if (response.isSuccessful && response.body() != null) {
                    val saved = response.body()!!

                    val serverAssignment = Assignment(
                        id = saved.assignmentID.toLong(),
                        title = saved.title,
                        description = saved.description,
                        module = saved.moduleName,
                        dueDateMillis = saved.dueDateMillis,
                        priority = saved.priority,
                        progress = saved.progress
                    )

                    withContext(Dispatchers.IO) {
                        if (assignment.id != serverAssignment.id) {
                            assignmentRepository.permanentlyDeleteAssignment(
                                assignment.id
                            )
                        }

                        assignmentRepository.saveAssignment(
                            serverAssignment,
                            pending = false
                        )
                    }

                    checkForNewAchievements()

                    Toast.makeText(
                        this@dialog_add_edit_assignment,
                        if (editingAssignmentId != null)
                            "Assignment updated"
                        else
                            "Assignment saved",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else {
                    Toast.makeText(
                        this@dialog_add_edit_assignment,
                        "Could not save assignment. Server error: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("StudyBetter", "Assignment server save failed", e)

                withContext(Dispatchers.IO) {
                    assignmentRepository.saveAssignment(
                        assignment,
                        pending = true
                    )
                }

                checkForNewAchievements()

                Toast.makeText(
                    this@dialog_add_edit_assignment,
                    if (editingAssignmentId != null)
                        "No server connection. Changes saved locally."
                    else
                        "No server connection. Assignment saved locally.",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } finally {
                saveButton.isEnabled = true
            }
        }
    }

    private suspend fun checkForNewAchievements() {
        try {
            val moduleRepo = ModuleRoomRepository(this)
            val sessionRepo = StudySessionRoomRepository(this)

            val data = withContext(Dispatchers.IO) {
                Triple(
                    assignmentRepository.getAssignments(),
                    moduleRepo.getModules(),
                    sessionRepo.getSessions()
                )
            }

            val totalStudySeconds = data.third.sumOf {
                it.durationSeconds.toLong()
            }

            AchievementNotificationManager.checkAndNotify(
                this,
                data.first,
                data.second,
                totalStudySeconds
            )

        } catch (e: Exception) {
            Log.e(
                "StudyBetter",
                "Achievement check after assignment save failed",
                e
            )
        }
    }

    private fun daysUntilDue(): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (
                dueDate.timeInMillis - today.timeInMillis
                ) / (24 * 60 * 60 * 1000)
    }
}
