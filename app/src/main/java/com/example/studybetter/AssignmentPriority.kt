package com.example.studybetter

object AssignmentPriority {

    // The priority rules were chosen in the approved Study Better design.
    fun fromDaysUntilDue(daysUntilDue: Long): String {
        return when {
            daysUntilDue <= 5 -> "High"
            daysUntilDue <= 14 -> "Medium"
            else -> "Low"
        }
    }
}
