
package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybetter.api.ModuleResponse
import com.example.studybetter.api.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.studybetter.data.ModuleRoomRepository
import android.util.Log

class modules : AppCompatActivity() {

    private lateinit var adapter: ModuleAdapter
    private lateinit var emptyState: View
    private lateinit var moduleRepository: ModuleRoomRepository
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        moduleRepository = ModuleRoomRepository(this)

        setContentView(R.layout.activity_modules)

        emptyState = findViewById(R.id.ll_empty_state)

        adapter = ModuleAdapter(emptyList()) { module, view ->
            showModuleOptions(module, view)
        }

        findViewById<RecyclerView>(R.id.rv_modules).apply {
            layoutManager = LinearLayoutManager(this@modules)
            adapter = this@modules.adapter
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbar
        ).setNavigationOnClickListener {
            finish()
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fab_add_module
        ).setOnClickListener {
            showAddModuleDialog()
        }

        setUpBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        refreshModules()
    }

    private fun displayModules(modules: List<Module>) {
        adapter.updateModules(modules)

        emptyState.visibility =
            if (modules.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun refreshModules() {
        if (isLoading) return
        isLoading = true

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val localModules = withContext(Dispatchers.IO) {
                    moduleRepository.getModules()
                }

                displayModules(localModules)

                try {
                    val serverModules = withContext(Dispatchers.IO) {
                        val response = RetrofitClient.api.getModules()

                        if (!response.isSuccessful) {
                            throw Exception("Server returned ${response.code()}")
                        }

                        response.body()?.map { it.toLocalModule() }
                            ?: emptyList()
                    }

                    withContext(Dispatchers.IO) {
                        moduleRepository.replaceServerModules(serverModules)
                    }

                    // Keep any offline-created or edited modules visible.
                    displayModules(
                        withContext(Dispatchers.IO) {
                            moduleRepository.getModules()
                        }
                    )

                } catch (e: Exception) {
                    Log.e("StudyBetter", "Module server sync failed", e)
                    displayModules(localModules)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@modules,
                    "Could not load modules",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun ModuleResponse.toLocalModule(): Module {
        return Module(
            id = moduleID.toLong(),
            code = code,
            title = title,
            description = description
        )
    }

    private fun showAddModuleDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val padding =
                (16 * resources.displayMetrics.density).toInt()

            setPadding(padding, padding, padding, padding)
        }

        val codeInput = EditText(this).apply {
            hint = "Module Code (e.g., COS101)"
        }

        val titleInput = EditText(this).apply {
            hint = "Module Title"
        }

        val descInput = EditText(this).apply {
            hint = "Description"
        }

        layout.addView(codeInput)
        layout.addView(titleInput)
        layout.addView(descInput)

        AlertDialog.Builder(this)
            .setTitle("Add Module")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->

                val code = codeInput.text.toString().trim()
                val title = titleInput.text.toString().trim()
                val description = descInput.text.toString().trim()

                if (code.isEmpty() || title.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Module code and title are required",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                addModule(code, title, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addModule(
        code: String,
        title: String,
        description: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val localModule = Module(
                    id = System.currentTimeMillis(),
                    code = code,
                    title = title,
                    description = description
                )

                withContext(Dispatchers.IO) {
                    moduleRepository.saveModule(localModule, true)
                }

                val updatedModules = withContext(Dispatchers.IO) {
                    moduleRepository.getModules()
                }

                displayModules(updatedModules)

                Toast.makeText(
                    this@modules,
                    "Module saved offline. It will be synchronised when connected.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("StudyBetter", "Could not save module", e)

                Toast.makeText(
                    this@modules,
                    "Could not save module",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    private fun showModuleOptions(module: Module, view: View) {
        val popup = PopupMenu(this, view)

        popup.menu.add("Edit")
        popup.menu.add("Delete")

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Edit" -> showEditModuleDialog(module)
                "Delete" -> confirmDeleteModule(module)
            }
            true
        }

        popup.show()
    }

    private fun showEditModuleDialog(module: Module) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val padding =
                (16 * resources.displayMetrics.density).toInt()

            setPadding(padding, padding, padding, padding)
        }

        val codeInput = EditText(this).apply {
            hint = "Module Code"
            setText(module.code)
        }

        val titleInput = EditText(this).apply {
            hint = "Module Title"
            setText(module.title)
        }

        val descInput = EditText(this).apply {
            hint = "Description"
            setText(module.description)
        }

        layout.addView(codeInput)
        layout.addView(titleInput)
        layout.addView(descInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Module")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->

                val code = codeInput.text.toString().trim()
                val title = titleInput.text.toString().trim()
                val description = descInput.text.toString().trim()

                if (code.isEmpty() || title.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Module code and title are required",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                updateModule(module, code, title, description)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateModule(
        oldModule: Module,
        code: String,
        title: String,
        description: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val updatedModule = oldModule.copy(
                    code = code,
                    title = title,
                    description = description
                )

                withContext(Dispatchers.IO) {
                    moduleRepository.saveModule(updatedModule, true)
                }

                val updatedModules = withContext(Dispatchers.IO) {
                    moduleRepository.getModules()
                }

                displayModules(updatedModules)

                Toast.makeText(
                    this@modules,
                    "Module updated. Changes will be synchronised.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("StudyBetter", "Could not update module", e)

                Toast.makeText(
                    this@modules,
                    "Could not update module",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmDeleteModule(module: Module) {
        AlertDialog.Builder(this)
            .setTitle("Delete Module")
            .setMessage("Are you sure you want to delete ${module.title}?")
            .setPositiveButton("Delete") { _, _ ->

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        withContext(Dispatchers.IO) {
                            moduleRepository.deleteModule(module.id)
                        }

                        val updatedModules = withContext(Dispatchers.IO) {
                            moduleRepository.getModules()
                        }

                        displayModules(updatedModules)

                        Toast.makeText(
                            this@modules,
                            "Module deleted",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Log.e("StudyBetter", "Could not delete module", e)

                        Toast.makeText(
                            this@modules,
                            "Could not delete module",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun setUpBottomNavigation() {
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.selectedItemId = R.id.nav_modules

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(
                        Intent(this, dashboard::class.java)
                    )
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(
                        Intent(this, calendar::class.java)
                    )
                    true
                }

                R.id.nav_progress -> {
                    startActivity(
                        Intent(this, progress::class.java)
                    )
                    true
                }

                else -> false
            }
        }
    }
}
