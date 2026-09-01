package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanningCalculatorTest {
    @Test
    fun `overall progress only counts required actions`() {
        val summary = DailyProgressCalculator().calculate(
            listOf(
                DailyAction(isRequired = true, isCompleted = true),
                DailyAction(isRequired = true, isCompleted = false),
                DailyAction(isRequired = false, isCompleted = false),
            ),
        )

        assertEquals(1, summary.completedRequired)
        assertEquals(2, summary.totalRequired)
        assertEquals(50, summary.percent)
    }

    @Test
    fun `empty required action list is not presented as a completed day`() {
        val summary = DailyProgressCalculator().calculate(
            listOf(DailyAction(isRequired = false, isCompleted = true)),
        )

        assertEquals(0, summary.totalRequired)
        assertEquals(null, summary.percent)
    }

    @Test
    fun `reading catch up evenly distributes remaining pages through the deadline`() {
        val targets = ReadingPlanCalculator().rebalance(
            currentPage = 40,
            totalPages = 100,
            from = LocalDate.of(2026, 8, 31),
            deadline = LocalDate.of(2026, 9, 5),
        )

        assertEquals(listOf(10, 10, 10, 10, 10, 10), targets.map { it.pages })
    }

    @Test
    fun `reading catch up assigns the remainder to the earliest days`() {
        val targets = ReadingPlanCalculator().rebalance(
            currentPage = 0,
            totalPages = 10,
            from = LocalDate.of(2026, 8, 31),
            deadline = LocalDate.of(2026, 9, 2),
        )

        assertEquals(listOf(4, 3, 3), targets.map { it.pages })
        assertEquals(10, targets.sumOf { it.pages })
    }

    @Test
    fun `completed reading plan does not create zero page targets`() {
        val targets = ReadingPlanCalculator().rebalance(
            currentPage = 100,
            totalPages = 100,
            from = LocalDate.of(2026, 8, 31),
            deadline = LocalDate.of(2026, 9, 2),
        )

        assertEquals(emptyList<PageTarget>(), targets)
    }

    @Test
    fun `reading catch up does not create impossible zero page days`() {
        val targets = ReadingPlanCalculator().rebalance(
            currentPage = 98,
            totalPages = 100,
            from = LocalDate.of(2026, 8, 31),
            deadline = LocalDate.of(2026, 9, 4),
        )

        assertEquals(listOf(1, 1), targets.map { it.pages })
        assertEquals(
            listOf(LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 1)),
            targets.map { it.date },
        )
    }

    @Test
    fun `goal progress is capped at one hundred percent`() {
        val progress = GoalProgressCalculator().calculate(current = 120, target = 100)

        assertEquals(100, progress.percent)
        assertEquals(true, progress.isComplete)
    }
}
