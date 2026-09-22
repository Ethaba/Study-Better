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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_add_edit_assignment)

        dueDateInput = findViewById(R.id.et_due_date)
        progressText = findViewById(R.id.tv_progress_value)

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener { finish() }
        setUpModuleMenu()
        setUpDatePicker()
        setUpProgressSlider()
        setUpPriorityButtons()
        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener { saveAssignment() }
    }

    private fun setUpModuleMenu() {
        val modules = arrayOf("General")
        val moduleInput = findViewById<AutoCompleteTextView>(R.id.act_module)
        moduleInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, modules))
    }

    private fun setUpDatePicker() {
        dueDateInput.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                dueDate.set(year, month, day, 0, 0, 0)
                dueDate.set(Calendar.MILLISECOND, 0)
                dueDateInput.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dueDate.time))
                selectedPriority = AssignmentPriority.fromDaysUntilDue(daysUntilDue())
                Toast.makeText(this, "Priority set to $selectedPriority", Toast.LENGTH_SHORT).show()
            }, dueDate.get(Calendar.YEAR), dueDate.get(Calendar.MONTH), dueDate.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setUpProgressSlider() {
        findViewById<Slider>(R.id.slider_progress).addOnChangeListener { _, value, _ ->
            progressText.text = "${value.toInt()}%"
        }
    }

    private fun setUpPriorityButtons() {
        findViewById<MaterialButton>(R.id.btn_priority_low).setOnClickListener { selectedPriority = "Low" }
        findViewById<MaterialButton>(R.id.btn_priority_medium).setOnClickListener { selectedPriority = "Medium" }
        findViewById<MaterialButton>(R.id.btn_priority_high).setOnClickListener { selectedPriority = "High" }
    }

    private fun saveAssignment() {
        val titleLayout = findViewById<TextInputLayout>(R.id.til_title)
        val moduleLayout = findViewById<TextInputLayout>(R.id.til_module)
        val dueDateLayout = findViewById<TextInputLayout>(R.id.til_due_date)
        val title = findViewById<TextInputEditText>(R.id.et_title).text.toString().trim()
        val description = findViewById<TextInputEditText>(R.id.et_description).text.toString().trim()
        val module = findViewById<AutoCompleteTextView>(R.id.act_module).text.toString().trim()
        val progress = findViewById<Slider>(R.id.slider_progress).value.toInt()

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

        // Due date priority is calculated again when the assignment is saved.
        selectedPriority = AssignmentPriority.fromDaysUntilDue(daysUntilDue())
        val assignment = Assignment(
            System.currentTimeMillis(),
            title,
            description,
            module,
            dueDate.timeInMillis,
            selectedPriority,
            progress
        )
        AssignmentStorage.saveAssignment(this, assignment)
        Toast.makeText(this, "Assignment saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun daysUntilDue(): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (dueDate.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)
    }
}
