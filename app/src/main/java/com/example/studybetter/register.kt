package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studybetter.api.RegisterRequest
import com.example.studybetter.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val fullNameInput =
            findViewById<EditText>(R.id.etFullName)

        val emailInput =
            findViewById<EditText>(R.id.etEmail)

        val passwordInput =
            findViewById<EditText>(R.id.etPassword)

        val confirmPasswordInput =
            findViewById<EditText>(R.id.etConfirmPassword)

        val registerButton =
            findViewById<Button>(R.id.btnRegister)

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {

            val fullName =
                fullNameInput.text.toString().trim()

            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString()

            val confirmPassword =
                confirmPasswordInput.text.toString()

            // Validate full name
            if (fullName.isEmpty()) {
                fullNameInput.error =
                    "Please enter your full name"
                return@setOnClickListener
            }

            // Validate email
            if (!InputValidator.isValidEmail(email)) {
                emailInput.error =
                    "Please enter a valid email address"
                return@setOnClickListener
            }

            // Validate password
            if (!InputValidator.isValidPassword(password)) {
                passwordInput.error =
                    "Password must contain at least 6 characters"
                return@setOnClickListener
            }

            // Confirm password
            if (password != confirmPassword) {
                confirmPasswordInput.error =
                    "Passwords do not match"
                return@setOnClickListener
            }

            registerButton.isEnabled = false

            val request = RegisterRequest(
                fullName = fullName,
                email = email,
                password = password
            )

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    val response =
                        RetrofitClient.api.register(request)

                    withContext(Dispatchers.Main) {

                        if (
                            response.isSuccessful &&
                            response.body() != null
                        ) {

                            val authResponse =
                                response.body()!!

                            /*
                             * Save the newly registered user's
                             * information.
                             */
                            getSharedPreferences(
                                "study_better_settings",
                                MODE_PRIVATE
                            )
                                .edit()
                                .putString(
                                    "full_name",
                                    authResponse.fullName
                                )
                                .putString(
                                    "email",
                                    authResponse.email
                                )
                                .putString(
                                    "token",
                                    authResponse.token
                                )
                                .putInt(
                                    "user_id",
                                    authResponse.userID
                                )
                                .apply()

                            Toast.makeText(
                                this@register,
                                "Registration successful! Please log in.",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(
                                this@register,
                                login::class.java
                            )

                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()

                        } else {

                            val errorMessage =
                                when (response.code()) {

                                    409 ->
                                        "An account with this email already exists."

                                    400 ->
                                        "Please check your registration details."

                                    else ->
                                        "Registration failed. Please try again."
                                }

                            Toast.makeText(
                                this@register,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()

                            registerButton.isEnabled = true
                        }
                    }

                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {

                        Toast.makeText(
                            this@register,
                            "Could not connect to the server.",
                            Toast.LENGTH_LONG
                        ).show()

                        registerButton.isEnabled = true
                    }
                }
            }
        }
    }
}