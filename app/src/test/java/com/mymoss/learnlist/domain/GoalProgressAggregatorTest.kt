package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalProgressAggregatorTest {
    private val aggregator = GoalProgressAggregator()
    private val today = LocalDate.of(2026, 9, 2)

    @Test
    fun `daily goal counts only today's matching project activity`() {
        val goal = GoalDefinition(
            metric = GoalMetric.READING_PAGES,
            period = GoalPeriod.DAILY,
            startDate = today.minusDays(10),
            projectId = "book-a",
        )

        val current = aggregator.current(
            goal = goal,
            today = today,
            activities = listOf(
                GoalActivity(GoalMetric.READING_PAGES, today, 12, "book-a"),
                GoalActivity(GoalMetric.READING_PAGES, today, 8, "book-b"),
                GoalActivity(GoalMetric.READING_PAGES, today.minusDays(1), 50, "book-a"),
            ),
        )

        assertEquals(12, current)
    }

    @Test
    fun `weekly goal starts on monday and includes the current sunday boundary`() {
        val goal = GoalDefinition(
            metric = GoalMetric.REVIEW_TASKS,
            period = GoalPeriod.WEEKLY,
            startDate = today.minusDays(30),
        )

        val current = aggregator.current(
            goal,
            today,
            listOf(
                GoalActivity(GoalMetric.REVIEW_TASKS, LocalDate.of(2026, 8, 30), 9),
                GoalActivity(GoalMetric.REVIEW_TASKS, LocalDate.of(2026, 8, 31), 2),
                GoalActivity(GoalMetric.REVIEW_TASKS, today, 3),
            ),
        )

        assertEquals(5, current)
    }

    @Test
    fun `monthly goal respects its configured start date and end date`() {
        val goal = GoalDefinition(
            metric = GoalMetric.FOCUS_MINUTES,
            period = GoalPeriod.MONTHLY,
            startDate = LocalDate.of(2026, 8, 30),
            endDate = LocalDate.of(2026, 9, 1),
        )

        val current = aggregator.current(
            goal,
            today,
            listOf(
                GoalActivity(GoalMetric.FOCUS_MINUTES, LocalDate.of(2026, 8, 31), 25),
                GoalActivity(GoalMetric.FOCUS_MINUTES, LocalDate.of(2026, 9, 1), 30),
                GoalActivity(GoalMetric.FOCUS_MINUTES, today, 60),
            ),
        )

        assertEquals(30, current)
    }

    @Test
    fun `custom goal does not count future or negative activity`() {
        val goal = GoalDefinition(
            metric = GoalMetric.TODO_DONE,
            period = GoalPeriod.CUSTOM,
            startDate = today.minusDays(2),
            endDate = today,
        )

        val current = aggregator.current(
            goal,
            today,
            listOf(
                GoalActivity(GoalMetric.TODO_DONE, today.minusDays(1), -3),
                GoalActivity(GoalMetric.TODO_DONE, today, 2),
                GoalActivity(GoalMetric.TODO_DONE, today.plusDays(1), 100),
            ),
        )

        assertEquals(2, current)
    }
}
