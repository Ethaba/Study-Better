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
import com.google.android.material.bottomnavigation.BottomNavigationView

class modules : AppCompatActivity() {

    private lateinit var adapter: ModuleAdapter
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modules)

        emptyState = findViewById(R.id.ll_empty_state)

        adapter = ModuleAdapter(emptyList()) { module, view ->
            showModuleOptions(module, view)
        }

        findViewById<RecyclerView>(R.id.rv_modules).apply {
            layoutManager = LinearLayoutManager(this@modules)
            adapter = this@modules.adapter
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener {
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

    private fun refreshModules() {

        val allModules = ModuleStorage.getModules(this)

        adapter.updateModules(allModules)

        emptyState.visibility =
            if (allModules.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun showAddModuleDialog() {

        val layout = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            val padding =
                (16 * resources.displayMetrics.density).toInt()

            setPadding(
                padding,
                padding,
                padding,
                padding
            )
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

                val code =
                    codeInput.text.toString().trim()

                val title =
                    titleInput.text.toString().trim()

                val description =
                    descInput.text.toString().trim()

                if (title.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Title cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                val newModule = Module(
                    id = System.currentTimeMillis(),
                    code = code,
                    title = title,
                    description = description
                )

                ModuleStorage.saveModule(
                    this,
                    newModule
                )

                refreshModules()

                Toast.makeText(
                    this,
                    "Module added",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showModuleOptions(
        module: Module,
        view: View
    ) {

        val popup = PopupMenu(this, view)

        popup.menu.add("Edit")
        popup.menu.add("Delete")

        popup.setOnMenuItemClickListener { item ->

            when (item.title.toString()) {

                "Edit" -> {
                    showEditModuleDialog(module)
                }

                "Delete" -> {
                    confirmDeleteModule(module)
                }
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

            setPadding(
                padding,
                padding,
                padding,
                padding
            )
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

                val code =
                    codeInput.text.toString().trim()

                val title =
                    titleInput.text.toString().trim()

                val description =
                    descInput.text.toString().trim()

                if (title.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Title cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                val updatedModule = Module(
                    id = module.id,
                    code = code,
                    title = title,
                    description = description
                )

                ModuleStorage.saveModule(
                    this,
                    updatedModule
                )

                refreshModules()

                Toast.makeText(
                    this,
                    "Module updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteModule(module: Module) {

        AlertDialog.Builder(this)
            .setTitle("Delete Module")
            .setMessage(
                "Are you sure you want to delete ${module.title}?"
            )
            .setPositiveButton("Delete") { _, _ ->

                ModuleStorage.deleteModule(
                    this,
                    module.id
                )

                refreshModules()

                Toast.makeText(
                    this,
                    "Module deleted",
                    Toast.LENGTH_SHORT
                ).show()
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
                        Intent(
                            this,
                            dashboard::class.java
                        )
                    )
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(
                        Intent(
                            this,
                            calendar::class.java
                        )
                    )
                    true
                }

                R.id.nav_progress -> {
                    startActivity(
                        Intent(
                            this,
                            progress::class.java
                        )
                    )
                    true
                }

                else -> false
            }
        }
    }
}