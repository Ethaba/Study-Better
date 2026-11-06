
package com.example.studybetter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studybetter.data.AcademicResourceEntity
import com.example.studybetter.data.AcademicResourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class academic_resources : AppCompatActivity() {

    private lateinit var repository: AcademicResourceRepository
    private lateinit var userId: String
    private lateinit var resourceContainer: LinearLayout
    private lateinit var searchInput: EditText

    private val categories = arrayOf(
        "Notes",
        "Past Papers",
        "Study Guides",
        "Videos",
        "Websites",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = AcademicResourceRepository(this)

        val prefs = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        userId = prefs.all["user_id"]?.toString() ?: "local_user"

        buildScreen()
        loadResources()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 247, 252))
            setPadding(dp(18), dp(16), dp(18), dp(16))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = TextView(this).apply {
            text = "‹"
            textSize = 34f
            setTextColor(Color.rgb(65, 55, 100))
            setPadding(0, 0, dp(16), 0)
            setOnClickListener { finish() }
        }

        val heading = TextView(this).apply {
            text = "Academic Resources"
            textSize = 22f
            setTextColor(Color.rgb(45, 40, 65))
            setTypeface(null, Typeface.BOLD)
        }

        toolbar.addView(backButton)
        toolbar.addView(heading)
        root.addView(toolbar)

        val description = TextView(this).apply {
            text = "Keep your learning materials organised in one place."
            textSize = 14f
            setTextColor(Color.rgb(110, 105, 120))
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(description)

        searchInput = EditText(this).apply {
            hint = "Search resources..."
            textSize = 15f
            isSingleLine = true
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundedBackground(Color.WHITE, 12)
        }

        root.addView(
            searchInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        searchInput.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    loadResources(s?.toString().orEmpty())
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        )

        val addButton = Button(this).apply {
            text = "+ Add Resource"
            setTextColor(Color.WHITE)
            background = roundedBackground(
                Color.rgb(105, 85, 170),
                12
            )
            setOnClickListener { showAddResourceDialog() }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(50)
        )
        buttonParams.topMargin = dp(14)
        root.addView(addButton, buttonParams)

        val resourcesHeading = TextView(this).apply {
            text = "Your Resources"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(45, 40, 65))
            setPadding(0, dp(22), 0, dp(12))
        }
        root.addView(resourcesHeading)

        val scrollView = ScrollView(this)

        resourceContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(resourceContainer)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun loadResources(query: String = "") {
        lifecycleScope.launch {
            try {
                val resources = withContext(Dispatchers.IO) {
                    if (query.isBlank()) {
                        repository.getResources(userId)
                    } else {
                        repository.searchResources(userId, query.trim())
                    }
                }

                displayResources(resources)

            } catch (e: Exception) {
                Toast.makeText(
                    this@academic_resources,
                    "Could not load resources.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayResources(
        resources: List<AcademicResourceEntity>
    ) {
        resourceContainer.removeAllViews()

        if (resources.isEmpty()) {
            val emptyMessage = TextView(this).apply {
                text = if (searchInput.text.isNotBlank()) {
                    "No resources match your search."
                } else {
                    "No resources saved yet. Tap Add Resource to get started."
                }
                textSize = 14f
                setTextColor(Color.rgb(120, 115, 130))
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(30), dp(16), dp(30))
            }

            resourceContainer.addView(emptyMessage)
            return
        }

        resources.forEach { resource ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                background = roundedBackground(Color.WHITE, 14)
            }

            val title = TextView(this).apply {
                text = resource.title
                textSize = 17f
                setTextColor(Color.rgb(45, 40, 65))
                setTypeface(null, Typeface.BOLD)
            }

            val category = TextView(this).apply {
                text = resource.category
                textSize = 12f
                setTextColor(Color.rgb(105, 85, 170))
                setPadding(0, dp(5), 0, dp(5))
            }

            val details = TextView(this).apply {
                text = resource.description.ifBlank {
                    "No description provided."
                }
                textSize = 14f
                setTextColor(Color.rgb(95, 90, 105))
            }

            card.addView(title)
            card.addView(category)
            card.addView(details)

            if (resource.resourceUrl.isNotBlank()) {
                val openButton = Button(this).apply {
                    text = "Open Resource"
                    setOnClickListener {
                        openResource(resource.resourceUrl)
                    }
                }
                card.addView(openButton)
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setTextColor(Color.rgb(190, 55, 55))
                setOnClickListener {
                    confirmDelete(resource)
                }
            }
            card.addView(deleteButton)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dp(12)

            resourceContainer.addView(card, params)
        }
    }

    private fun showAddResourceDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        val titleInput = EditText(this).apply {
            hint = "Resource title"
            isSingleLine = true
        }

        val descriptionInput = EditText(this).apply {
            hint = "Description"
            minLines = 2
            gravity = Gravity.TOP
        }

        val categorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@academic_resources,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
        }

        val urlInput = EditText(this).apply {
            hint = "Resource link (optional)"
            isSingleLine = true
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        form.addView(titleInput)
        form.addView(descriptionInput)
        form.addView(categorySpinner)
        form.addView(urlInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Academic Resource")
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val title = titleInput.text.toString().trim()

                    if (title.isBlank()) {
                        titleInput.error = "Enter a resource title"
                        return@setOnClickListener
                    }

                    val resource = AcademicResourceEntity(
                        userId = userId,
                        title = title,
                        description = descriptionInput.text.toString().trim(),
                        category = categorySpinner.selectedItem.toString(),
                        resourceUrl = urlInput.text.toString().trim(),
                        createdAt = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.US
                        ).format(Date())
                    )

                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                repository.saveResource(resource)
                            }

                            dialog.dismiss()
                            loadResources(searchInput.text.toString())

                            Toast.makeText(
                                this@academic_resources,
                                "Resource saved successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@academic_resources,
                                "Could not save resource.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        dialog.show()
    }

    private fun confirmDelete(resource: AcademicResourceEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Resource")
            .setMessage("Delete \"${resource.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            repository.deleteResource(resource.id, userId)
                        }

                        loadResources(searchInput.text.toString())

                        Toast.makeText(
                            this@academic_resources,
                            "Resource deleted.",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@academic_resources,
                            "Could not delete resource.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openResource(url: String) {
        try {
            val formattedUrl = if (
                url.startsWith("http://", true) ||
                url.startsWith("https://", true)
            ) {
                url
            } else {
                "https://$url"
            }

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(formattedUrl)
            )

            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open this resource link.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
