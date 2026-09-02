package com.mymoss.learnlist.ui

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissedTodoPromptPolicyTest {
    private val currentDate = LocalDate.of(2026, 9, 2)

    @Test
    fun historicalDateDoesNotPrompt() {
        assertFalse(
            shouldPromptMissedTodo(
                selectedDate = currentDate.minusDays(1),
                currentDate = currentDate,
                isRestDay = false,
                hasEligibleProject = true,
                hasMissedOccurrence = true,
            ),
        )
    }

    @Test
    fun currentDatePromptsForEligibleMissedTodo() {
        assertTrue(
            shouldPromptMissedTodo(
                selectedDate = currentDate,
                currentDate = currentDate,
                isRestDay = false,
                hasEligibleProject = true,
                hasMissedOccurrence = true,
            ),
        )
    }

    @Test
    fun restDayAndUnavailableProjectDoNotPrompt() {
        assertFalse(
            shouldPromptMissedTodo(
                selectedDate = currentDate,
                currentDate = currentDate,
                isRestDay = true,
                hasEligibleProject = true,
                hasMissedOccurrence = true,
            ),
        )
        assertFalse(
            shouldPromptMissedTodo(
                selectedDate = currentDate,
                currentDate = currentDate,
                isRestDay = false,
                hasEligibleProject = false,
                hasMissedOccurrence = true,
            ),
        )
    }

    @Test
    fun noMissedOccurrenceDoesNotPrompt() {
        assertFalse(
            shouldPromptMissedTodo(
                selectedDate = currentDate,
                currentDate = currentDate,
                isRestDay = false,
                hasEligibleProject = true,
                hasMissedOccurrence = false,
            ),
        )
    }
}

