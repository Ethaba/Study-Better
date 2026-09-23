package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studybetter.api.LoginRequest
import com.example.studybetter.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        findViewById<TextView>(R.id.txtRegister).setOnClickListener {
            startActivity(Intent(this, register::class.java))
        }

        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // Validate email
            if (!InputValidator.isValidEmail(email)) {
                emailInput.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            // Validate password
            if (password.isEmpty()) {
                passwordInput.error = "Please enter your password"
                return@setOnClickListener
            }

            // Prevent multiple login requests
            loginButton.isEnabled = false

            val request = LoginRequest(
                email = email,
                password = password
            )

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    val response = RetrofitClient.api.login(request)

                    withContext(Dispatchers.Main) {

                        if (response.isSuccessful && response.body() != null) {

                            val authResponse = response.body()!!

                            // Save logged-in user's information
                            val prefs = getSharedPreferences(
                                "study_better_settings",
                                MODE_PRIVATE
                            )

                            prefs.edit()
                                .putString("full_name", authResponse.fullName)
                                .putString("email", authResponse.email)
                                .putString("token", authResponse.token)
                                .putInt("user_id", authResponse.userID)
                                .apply()

                            Toast.makeText(
                                this@login,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Go to dashboard
                            val intent = Intent(
                                this@login,
                                dashboard::class.java
                            )

                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()

                        } else {

                            when (response.code()) {

                                401 -> {
                                    Toast.makeText(
                                        this@login,
                                        "Invalid email or password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        this@login,
                                        "Login failed. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            loginButton.isEnabled = true
                        }
                    }

                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {

                        Toast.makeText(
                            this@login,
                            "Could not connect to the server.",
                            Toast.LENGTH_LONG
                        ).show()

                        loginButton.isEnabled = true
                    }
                }
            }
        }
    }
}