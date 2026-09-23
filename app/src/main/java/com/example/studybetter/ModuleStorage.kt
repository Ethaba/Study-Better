package com.example.studybetter

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ModuleStorage {

    private const val preferencesName = "study_better_modules"
    private const val modulesKeyPrefix = "modules_user_"

    private fun getUserKey(context: Context): String {

        val userId = context.getSharedPreferences(
            "study_better_settings",
            Context.MODE_PRIVATE
        ).getInt("user_id", -1)

        return modulesKeyPrefix + userId
    }

    fun getModules(context: Context): MutableList<Module> {

        val savedText = context.getSharedPreferences(
            preferencesName,
            Context.MODE_PRIVATE
        )
            .getString(getUserKey(context), "[]") ?: "[]"

        val modules = mutableListOf<Module>()

        val savedArray = JSONArray(savedText)

        for (index in 0 until savedArray.length()) {

            val item = savedArray.getJSONObject(index)

            modules.add(
                Module(
                    item.getLong("id"),
                    item.getString("code"),
                    item.getString("title"),
                    item.getString("description")
                )
            )
        }

        return modules
    }

    fun saveModule(
        context: Context,
        module: Module
    ) {

        val modules = getModules(context)

        val existingIndex = modules.indexOfFirst {
            it.id == module.id
        }

        if (existingIndex >= 0) {
            modules[existingIndex] = module
        } else {
            modules.add(module)
        }

        saveAll(context, modules)
    }

    fun deleteModule(
        context: Context,
        moduleId: Long
    ) {

        val modules = getModules(context)

        modules.removeAll {
            it.id == moduleId
        }

        saveAll(context, modules)
    }

    private fun saveAll(
        context: Context,
        modules: List<Module>
    ) {

        val savedArray = JSONArray()

        modules.forEach { module ->

            savedArray.put(
                JSONObject()
                    .put("id", module.id)
                    .put("code", module.code)
                    .put("title", module.title)
                    .put("description", module.description)
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