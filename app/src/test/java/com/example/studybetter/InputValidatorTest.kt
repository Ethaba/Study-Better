package com.example.studybetter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun validEmail_returnsTrue() {
        assertTrue(InputValidator.isValidEmail("student@example.com"))
    }

    @Test
    fun invalidEmail_returnsFalse() {
        assertFalse(InputValidator.isValidEmail("student-at-example.com"))
    }

    @Test
    fun passwordWithSixCharacters_returnsTrue() {
        assertTrue(InputValidator.isValidPassword("secret"))
    }

    @Test
    fun passwordWithLessThanSixCharacters_returnsFalse() {
        assertFalse(InputValidator.isValidPassword("short"))
    }
}
