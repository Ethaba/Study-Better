package com.example.studybetter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class activity_splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Keep the splash screen visible briefly before opening the login screen.
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("StudyBetter", "Splash screen finished")
            startActivity(Intent(this, login::class.java))
            finish()
        }, 2000)
    }
}
