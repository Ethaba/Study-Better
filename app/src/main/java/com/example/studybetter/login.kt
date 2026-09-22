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

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)

        findViewById<TextView>(R.id.txtRegister).setOnClickListener {
            startActivity(Intent(this, register::class.java))
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (!InputValidator.isValidEmail(email)) {
                emailInput.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Please enter your password"
                return@setOnClickListener
            }

            Log.d("StudyBetter", "Login validation completed")
            // The password is deliberately not logged for security.
            Toast.makeText(this, "The login service is not connected yet", Toast.LENGTH_LONG).show()
        }
    }
}
