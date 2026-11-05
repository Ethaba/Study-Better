package com.example.studybetter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object StudySessionStorage {

    private const val PREFS_NAME = "study_better_sessions"
    private const val SESSIONS_KEY = "sessions"

    fun saveSession(
        context: Context,
        startedAt: String,
        durationSeconds: Int,
        description: String
    ) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val sessions = JSONArray(
            preferences.getString(SESSIONS_KEY, "[]")
        )

        val session = JSONObject().apply {
            put("startedAt", startedAt)
            put("durationSeconds", durationSeconds)
            put("description", description)
            put("synced", false)
        }

        sessions.put(session)

        preferences.edit()
            .putString(SESSIONS_KEY, sessions.toString())
            .apply()
    }

    fun getSessions(context: Context): JSONArray {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return JSONArray(
            preferences.getString(SESSIONS_KEY, "[]")
        )
    }

    fun markSessionSynced(context: Context, startedAt: String) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val sessions = getSessions(context)

        for (i in 0 until sessions.length()) {
            val session = sessions.getJSONObject(i)

            if (session.optString("startedAt") == startedAt) {
                session.put("synced", true)
                break
            }
        }

        preferences.edit()
            .putString(SESSIONS_KEY, sessions.toString())
            .apply()
    }
}