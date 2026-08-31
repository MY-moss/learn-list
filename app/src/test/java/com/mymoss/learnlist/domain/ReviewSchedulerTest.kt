package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewSchedulerTest {
    private val scheduler = DefaultReviewScheduler()
    private val day = LocalDate.of(2026, 8, 31)

    @Test
    fun `initial learning schedules first review for tomorrow`() {
        val state = scheduler.completeInitialLearning(day)

        assertEquals(0, state.stage)
        assertEquals(day.plusDays(1), state.nextReviewDate)
    }

    @Test
    fun `remember advances through the configured intervals`() {
        val state = ReviewState(stage = 1, nextReviewDate = day)

        val next = scheduler.review(state, RecallRating.REMEMBERED, day).state

        assertEquals(2, next.stage)
        assertEquals(day.plusDays(4), next.nextReviewDate)
    }

    @Test
    fun `remembering the final stage keeps the ninety day interval`() {
        val state = ReviewState(stage = ReviewIntervals.days.lastIndex, nextReviewDate = day)

        val next = scheduler.review(state, RecallRating.REMEMBERED, day).state

        assertEquals(ReviewIntervals.days.lastIndex, next.stage)
        assertEquals(day.plusDays(90), next.nextReviewDate)
    }

    @Test
    fun `fuzzy keeps the stage and uses half the current interval rounded up`() {
        val state = ReviewState(stage = 4, nextReviewDate = day)

        val next = scheduler.review(state, RecallRating.FUZZY, day).state

        assertEquals(4, next.stage)
        assertEquals(day.plusDays(8), next.nextReviewDate)
    }

    @Test
    fun `forgotten content returns to the first interval`() {
        val state = ReviewState(stage = 7, nextReviewDate = day)

        val next = scheduler.review(state, RecallRating.FORGOT, day).state

        assertEquals(0, next.stage)
        assertEquals(day.plusDays(1), next.nextReviewDate)
    }

    @Test
    fun `snooze changes only the reminder time`() {
        val state = ReviewState(stage = 3, nextReviewDate = day)

        val decision = scheduler.review(
            state = state,
            rating = RecallRating.SNOOZE,
            completedDate = day,
            snoozeUntil = day.plusDays(1),
        )

        assertEquals(state.stage, decision.state.stage)
        assertEquals(day.plusDays(1), decision.state.nextReviewDate)
        assertNull(decision.log)
    }

    @Test
    fun `snooze without an explicit date postpones from the completion date`() {
        val state = ReviewState(stage = 3, nextReviewDate = day.minusDays(10))

        val decision = scheduler.review(state, RecallRating.SNOOZE, day)

        assertEquals(state.stage, decision.state.stage)
        assertEquals(day.plusDays(1), decision.state.nextReviewDate)
        assertNull(decision.log)
    }
}
