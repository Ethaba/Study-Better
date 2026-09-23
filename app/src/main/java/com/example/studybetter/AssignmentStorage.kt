package com.example.studybetter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AssignmentStorage {

    private const val preferencesName = "study_better_assignments"
    private const val assignmentsKeyPrefix = "assignments_user_"

    private fun getUserKey(context: Context): String {
        val userId = context.getSharedPreferences(
            "study_better_settings",
            Context.MODE_PRIVATE
        ).getInt("user_id", -1)

        return assignmentsKeyPrefix + userId
    }

    fun getAssignments(context: Context): MutableList<Assignment> {

        val savedText = context.getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )
            .getString(getUserKey(context), "[]") ?: "[]"

        val assignments = mutableListOf<Assignment>()
        val savedArray = JSONArray(savedText)

        for (index in 0 until savedArray.length()) {

            val item = savedArray.getJSONObject(index)

            assignments.add(
                Assignment(
                    item.getLong("id"),
                    item.getString("title"),
                    item.getString("description"),
                    item.getString("module"),
                    item.getLong("dueDateMillis"),
                    item.getString("priority"),
                    item.getInt("progress")
                )
            )
        }

        return assignments
    }

    fun saveAssignment(
        context: Context,
        assignment: Assignment
    ) {

        val assignments = getAssignments(context)

        val existingIndex = assignments.indexOfFirst {
            it.id == assignment.id
        }

        if (existingIndex >= 0) {
            assignments[existingIndex] = assignment
        } else {
            assignments.add(assignment)
        }

        saveAll(context, assignments)
    }

    fun deleteAssignment(
        context: Context,
        assignmentId: Long
    ) {

        val assignments = getAssignments(context)

        assignments.removeAll {
            it.id == assignmentId
        }

        saveAll(context, assignments)
    }

    private fun saveAll(
        context: Context,
        assignments: List<Assignment>
    ) {

        val savedArray = JSONArray()

        assignments.forEach { assignment ->

            savedArray.put(
                JSONObject()
                    .put("id", assignment.id)
                    .put("title", assignment.title)
                    .put("description", assignment.description)
                    .put("module", assignment.module)
                    .put("dueDateMillis", assignment.dueDateMillis)
                    .put("priority", assignment.priority)
                    .put("progress", assignment.progress)
            )
        }

        context.getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                getUserKey(context),
                savedArray.toString()
            )
            .apply()
    }
}