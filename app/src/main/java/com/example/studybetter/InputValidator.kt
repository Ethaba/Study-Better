package com.example.studybetter

object InputValidator {

    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
