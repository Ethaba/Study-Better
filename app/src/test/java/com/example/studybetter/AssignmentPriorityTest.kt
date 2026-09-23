package com.example.studybetter

import org.junit.Assert.assertEquals
import org.junit.Test

class AssignmentPriorityTest {

    @Test
    fun assignmentDueInFiveDays_isHighPriority() {
        assertEquals("High", AssignmentPriority.fromDaysUntilDue(5))
    }

    @Test
    fun assignmentDueInSixDays_isMediumPriority() {
        assertEquals("Medium", AssignmentPriority.fromDaysUntilDue(6))
    }

    @Test
    fun assignmentDueInTenDays_isMediumPriority() {
        assertEquals("Medium", AssignmentPriority.fromDaysUntilDue(10))
    }

    @Test
    fun assignmentDueInFourteenDays_isMediumPriority() {
        assertEquals("Medium", AssignmentPriority.fromDaysUntilDue(14))
    }

    @Test
    fun assignmentDueInFifteenDays_isLowPriority() {
        assertEquals("Low", AssignmentPriority.fromDaysUntilDue(15))
    }
}