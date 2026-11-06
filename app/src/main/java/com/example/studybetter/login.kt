
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.studybetter.api.LoginRequest
import com.example.studybetter.api.GoogleLoginRequest
import com.example.studybetter.api.RetrofitClient
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class login : AppCompatActivity() {

    private val webClientId =
        "743179742104-l7gbpteos1n377qlhi574imatn1r9kif.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)

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
        val googleButton = findViewById<Button>(R.id.btnGoogle)

        findViewById<TextView>(R.id.txtRegister).setOnClickListener {
            startActivity(Intent(this, register::class.java))
        }

        // Normal email and password login
        loginButton.setOnClickListener {

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

                            saveUserAndOpenDashboard(
                                response.body()!!.fullName,
                                response.body()!!.email,
                                response.body()!!.token,
                                response.body()!!.userID
                            )

                        } else {
                            when (response.code()) {
                                401 -> Toast.makeText(
                                    this@login,
                                    "Invalid email or password",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> Toast.makeText(
                                    this@login,
                                    "Login failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            loginButton.isEnabled = true
                        }
                    }

                } catch (e: Exception) {
                    android.util.Log.e(
                        "StudyBetterLogin",
                        "Email login request failed",
                        e
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@login,
                            "Login error: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()

                        loginButton.isEnabled = true
                    }
                }
            }
        }

        // Google Sign-In
        googleButton.setOnClickListener {
            googleButton.isEnabled = false
            signInWithGoogle(googleButton)
        }
    }

    private fun signInWithGoogle(googleButton: Button) {

        CoroutineScope(Dispatchers.Main).launch {

            try {
                val credentialManager = CredentialManager.create(this@login)

                val googleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = webClientId
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = this@login,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    sendGoogleTokenToBackend(
                        googleCredential.idToken,
                        googleButton
                    )

                } else {
                    Toast.makeText(
                        this@login,
                        "Unexpected Google credential type.",
                        Toast.LENGTH_LONG
                    ).show()

                    googleButton.isEnabled = true
                }

            } catch (e: GetCredentialException) {

                Toast.makeText(
                    this@login,
                    "Google sign-in was cancelled or could not be completed.",
                    Toast.LENGTH_LONG
                ).show()

                googleButton.isEnabled = true

            } catch (e: Exception) {

                Toast.makeText(
                    this@login,
                    "Google sign-in failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()

                googleButton.isEnabled = true
            }
        }
    }

    private suspend fun sendGoogleTokenToBackend(
        idToken: String,
        googleButton: Button
    ) {

        withContext(Dispatchers.IO) {

            try {
                val request = GoogleLoginRequest(
                    idToken = idToken
                )

                val response = RetrofitClient.api.googleLogin(request)

                withContext(Dispatchers.Main) {

                    if (response.isSuccessful && response.body() != null) {

                        val authResponse = response.body()!!

                        saveUserAndOpenDashboard(
                            authResponse.fullName,
                            authResponse.email,
                            authResponse.token,
                            authResponse.userID
                        )

                    } else {

                        Toast.makeText(
                            this@login,
                            "Google authentication failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()

                        googleButton.isEnabled = true
                    }
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@login,
                        "Could not connect to the server.",
                        Toast.LENGTH_LONG
                    ).show()

                    googleButton.isEnabled = true
                }
            }
        }
    }

    private fun saveUserAndOpenDashboard(
        fullName: String,
        email: String,
        token: String,
        userID: Int
    ) {

        val prefs = getSharedPreferences(
            "study_better_settings",
            MODE_PRIVATE
        )

        prefs.edit()
            .putString("full_name", fullName)
            .putString("email", email)
            .putString("token", token)
            .putInt("user_id", userID)
            .apply()

        Toast.makeText(
            this,
            "Login successful!",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this, dashboard::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
