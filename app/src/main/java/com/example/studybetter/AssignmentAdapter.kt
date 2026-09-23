package com.example.studybetter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentAdapter(
    private var assignments: List<Assignment>,
    private val onAssignmentLongPress: (Assignment) -> Unit
) : RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    class AssignmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.tv_title)
        val module: TextView = view.findViewById(R.id.tv_module)
        val dueDate: TextView = view.findViewById(R.id.tv_due)
        val priority: TextView = view.findViewById(R.id.tv_priority)
        val progress: TextView = view.findViewById(R.id.tv_progress)
        val progressIndicator: CircularProgressIndicator =
            view.findViewById(R.id.progress_indicator)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssignmentViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.activity_item_assignment,
                parent,
                false
            )

        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AssignmentViewHolder,
        position: Int
    ) {

        val assignment = assignments[position]

        holder.title.text = assignment.title
        holder.module.text = assignment.module

        holder.dueDate.text =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(
                Date(assignment.dueDateMillis)
            )

        holder.priority.text = assignment.priority
        holder.progress.text = "${assignment.progress}%"

        holder.progressIndicator.progress = assignment.progress

        holder.priority.setTextColor(
            when (assignment.priority) {
                "High" -> Color.rgb(229, 57, 53)
                "Medium" -> Color.rgb(251, 140, 0)
                else -> Color.rgb(67, 160, 71)
            }
        )

        // Tap an assignment to edit it.
        holder.itemView.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                dialog_add_edit_assignment::class.java
            )

            intent.putExtra(
                "assignment_id",
                assignment.id
            )

            holder.itemView.context.startActivity(intent)
        }

        // Long press still deletes the assignment.
        holder.itemView.setOnLongClickListener {

            onAssignmentLongPress(assignment)

            true
        }
    }

    override fun getItemCount(): Int {
        return assignments.size
    }

    fun updateAssignments(
        updatedAssignments: List<Assignment>
    ) {

        assignments = updatedAssignments

        notifyDataSetChanged()
    }
}