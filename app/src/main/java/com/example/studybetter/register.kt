package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fullNameInput = findViewById<EditText>(R.id.etFullName)
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (fullName.isEmpty()) {
                fullNameInput.error = "Please enter your full name"
                return@setOnClickListener
            }

            if (!InputValidator.isValidEmail(email)) {
                emailInput.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if (!InputValidator.isValidPassword(password)) {
                passwordInput.error = "Password must contain at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            Log.d("StudyBetter", "Registration validation completed")
            // Account creation will only happen after the REST API is connected.
            Toast.makeText(this, "The registration service is not connected yet", Toast.LENGTH_LONG).show()
        }
    }
}
