package com.example.studybetter

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class dialog_add_edit_assignment : AppCompatActivity() {

    private val dueDate = Calendar.getInstance()

    private lateinit var dueDateInput: TextInputEditText
    private lateinit var progressText: TextView

    private var selectedPriority = "Low"

    // Used when editing an existing assignment
    private var editingAssignmentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dialog_add_edit_assignment)

        dueDateInput = findViewById(R.id.et_due_date)
        progressText = findViewById(R.id.tv_progress_value)

        editingAssignmentId = intent.getLongExtra(
            "assignment_id",
            -1L
        ).takeIf { it != -1L }

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
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

        val savedModules = ModuleStorage.getModules(this)

        val moduleNames = mutableListOf<String>()

        moduleNames.add("General")

        savedModules.forEach { module ->
            moduleNames.add(module.title)
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            moduleNames
        )

        moduleInput.setAdapter(adapter)
    }

    private fun setUpDatePicker() {

        dueDateInput.setOnClickListener {

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    dueDate.set(
                        year,
                        month,
                        day,
                        0,
                        0,
                        0
                    )

                    dueDate.set(
                        Calendar.MILLISECOND,
                        0
                    )

                    dueDateInput.setText(
                        SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                        ).format(dueDate.time)
                    )

                    selectedPriority =
                        AssignmentPriority.fromDaysUntilDue(
                            daysUntilDue()
                        )
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

                progressText.text =
                    "${value.toInt()}%"
            }
    }

    private fun setUpPriorityButtons() {

        findViewById<MaterialButton>(R.id.btn_priority_low)
            .setOnClickListener {
                selectedPriority = "Low"
            }

        findViewById<MaterialButton>(R.id.btn_priority_medium)
            .setOnClickListener {
                selectedPriority = "Medium"
            }

        findViewById<MaterialButton>(R.id.btn_priority_high)
            .setOnClickListener {
                selectedPriority = "High"
            }
    }

    private fun loadAssignmentForEditing() {

        val assignmentId = editingAssignmentId ?: return

        val assignment = AssignmentStorage
            .getAssignments(this)
            .firstOrNull { it.id == assignmentId }
            ?: return

        findViewById<TextInputEditText>(R.id.et_title)
            .setText(assignment.title)

        findViewById<TextInputEditText>(R.id.et_description)
            .setText(assignment.description)

        findViewById<AutoCompleteTextView>(R.id.act_module)
            .setText(assignment.module, false)

        dueDate.timeInMillis =
            assignment.dueDateMillis

        dueDateInput.setText(
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(dueDate.time)
        )

        selectedPriority = assignment.priority

        findViewById<Slider>(R.id.slider_progress).value = assignment.progress.toFloat()

        progressText.text =
            "${assignment.progress}%"

        findViewById<MaterialButton>(R.id.btn_save)
            .text = "Update Assignment"
    }

    private fun saveAssignment() {

        val titleLayout =
            findViewById<TextInputLayout>(R.id.til_title)

        val moduleLayout =
            findViewById<TextInputLayout>(R.id.til_module)

        val dueDateLayout =
            findViewById<TextInputLayout>(R.id.til_due_date)

        val title =
            findViewById<TextInputEditText>(R.id.et_title)
                .text
                .toString()
                .trim()

        val description =
            findViewById<TextInputEditText>(R.id.et_description)
                .text
                .toString()
                .trim()

        val module =
            findViewById<AutoCompleteTextView>(R.id.act_module)
                .text
                .toString()
                .trim()

        val progress =
            findViewById<Slider>(R.id.slider_progress)
                .value
                .toInt()

        titleLayout.error = null
        moduleLayout.error = null
        dueDateLayout.error = null

        if (title.isEmpty()) {
            titleLayout.error =
                "Please enter an assignment title"
            return
        }

        if (module.isEmpty()) {
            moduleLayout.error =
                "Please select a module"
            return
        }

        if (dueDateInput.text.isNullOrBlank()) {
            dueDateLayout.error =
                "Please select a due date"
            return
        }

        // Automatically calculate priority based on how
        // close the assignment deadline is.
        selectedPriority =
            AssignmentPriority.fromDaysUntilDue(
                daysUntilDue()
            )

        val assignmentId =
            editingAssignmentId
                ?: System.currentTimeMillis()

        val assignment = Assignment(
            id = assignmentId,
            title = title,
            description = description,
            module = module,
            dueDateMillis = dueDate.timeInMillis,
            priority = selectedPriority,
            progress = progress
        )

        AssignmentStorage.saveAssignment(
            this,
            assignment
        )

        Toast.makeText(
            this,
            if (editingAssignmentId != null)
                "Assignment updated"
            else
                "Assignment saved",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun daysUntilDue(): Long {

        val today = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (
                dueDate.timeInMillis -
                        today.timeInMillis
                ) / (24 * 60 * 60 * 1000)
    }
}