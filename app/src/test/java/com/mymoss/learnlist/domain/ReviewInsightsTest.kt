package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewInsightsTest {
    private val today = LocalDate.of(2026, 9, 2)

    @Test
    fun `summary counts only the inclusive recent window and ignores snooze`() {
        val records = listOf(
            record("today", RecallRating.REMEMBERED, today),
            record("today", RecallRating.FUZZY, today),
            record("boundary", RecallRating.FORGOT, today.minusDays(27)),
            record("outside", RecallRating.FORGOT, today.minusDays(28)),
            record("future", RecallRating.FORGOT, today.plusDays(1)),
            record("snooze", RecallRating.SNOOZE, today),
        )

        val summary = ReviewInsights.summarize(records, today)

        assertEquals(1, summary.feedback.remembered)
        assertEquals(1, summary.feedback.fuzzy)
        assertEquals(1, summary.feedback.forgot)
        assertEquals(3, summary.feedback.total)
        assertEquals(33, summary.feedback.recallPercent)
        assertEquals(2, summary.reviewedTaskCount)
    }

    @Test
    fun `weak points rank negative feedback before stable tasks`() {
        val records = listOf(
            record("stable", RecallRating.REMEMBERED, today),
            record("fuzzy", RecallRating.FUZZY, today),
            record("forgot", RecallRating.FORGOT, today),
            record("forgot", RecallRating.FORGOT, today.minusDays(1)),
            record("forgot", RecallRating.FUZZY, today.minusDays(2)),
        )

        val weakPoints = ReviewInsights.summarize(records, today).weakPoints

        assertEquals(listOf("forgot", "fuzzy"), weakPoints.map(ReviewWeakPoint::taskId))
        assertEquals(3, weakPoints.first().negativeCount)
        assertEquals(3, weakPoints.first().reviewCount)
    }

    @Test
    fun `weak point limit can hide only the insight list, not source records`() {
        val records = (1..3).map { index ->
            record("task-$index", RecallRating.FUZZY, today)
        }

        val summary = ReviewInsights.summarize(records, today, weakPointLimit = 1)

        assertEquals(1, summary.weakPoints.size)
        assertEquals(3, summary.feedback.total)
        assertEquals(3, summary.reviewedTaskCount)
        assertTrue(summary.weakPoints.all { it.negativeCount > 0 })
    }

    private fun record(taskId: String, rating: RecallRating, date: LocalDate) =
        ReviewInsightRecord(taskId, taskId, "项目", rating, date)
}