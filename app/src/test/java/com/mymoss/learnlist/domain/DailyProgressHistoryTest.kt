package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyProgressHistoryTest {
    private val day = LocalDate.of(2026, 9, 1)

    @Test
    fun `todo completion can be toggled off without losing other dates`() {
        val encoded = "2026-08-30,${day}"

        val undone = TodoCompletion.toggle(encoded, day)

        assertEquals("2026-08-30", undone)
        assertEquals(encoded, TodoCompletion.toggle(undone, day))
    }

    @Test
    fun `initial learning stays attached to its original date after later edits`() {
        assertTrue(InitialLearningTracker.isCompletedOn(day.toString(), day))
        assertFalse(InitialLearningTracker.isCompletedOn(day.toString(), day.plusDays(1)))
        assertFalse(InitialLearningTracker.isCompletedOn(null, day))
    }

    @Test
    fun `daily progress uses the same required-action rules for tasks reading and todos`() {
        val project = DailyProjectProgress("book", isArchived = false, isPaused = false)
        val input = DailyProgressInput(
            projects = listOf(project),
            tasks = listOf(
                DailyTaskProgress(
                    id = "reviewed",
                    projectId = project.id,
                    isRequired = true,
                    isArchived = false,
                    hasLearned = true,
                    initialLearningDate = null,
                    nextReviewDate = day,
                    snoozedUntil = null,
                    createdOn = day.minusDays(1),
                    reviewedDates = setOf(day),
                ),
                DailyTaskProgress(
                    id = "overdue",
                    projectId = project.id,
                    isRequired = true,
                    isArchived = false,
                    hasLearned = true,
                    initialLearningDate = null,
                    nextReviewDate = day.minusDays(1),
                    snoozedUntil = null,
                    createdOn = day.minusDays(2),
                    reviewedDates = emptySet(),
                ),
            ),
            readings = listOf(
                DailyReadingProgress(
                    id = "reading",
                    projectId = project.id,
                    totalPages = 100,
                    currentPage = 20,
                    dailyTarget = 10,
                    startDate = day.minusDays(1),
                    isPaused = false,
                    isArchived = false,
                    pagesByDate = mapOf(day to 10),
                    targetsByDate = emptyMap(),
                ),
            ),
            todos = listOf(
                DailyTodoProgress(
                    id = "todo",
                    isRequired = true,
                    isArchived = false,
                    repeatRule = TodoRepeatRule.DAILY,
                    baseDate = day.minusDays(1),
                    customDays = emptySet(),
                    completedDates = setOf(day),
                ),
            ),
        )

        val summary = DailyProgressCalculator().calculate(input, day)

        assertEquals(3, summary.completedRequired)
        assertEquals(4, summary.totalRequired)
        assertEquals(75, summary.percent)
    }
}

