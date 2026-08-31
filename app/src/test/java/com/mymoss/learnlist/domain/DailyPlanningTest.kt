package com.mymoss.learnlist.domain

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPlanningTest {
    private val monday = LocalDate.of(2026, 8, 31)

    @Test
    fun `recurring todo follows its selected rule`() {
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.ONCE, monday, monday))
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.ONCE, monday, monday.plusDays(1)))
        assertTrue(
            TodoRecurrence.isDue(
                rule = TodoRepeatRule.ONCE,
                baseDate = monday,
                date = monday.plusDays(1),
                completedDates = setOf(monday.plusDays(1)),
            ),
        )
        assertFalse(
            TodoRecurrence.isDue(
                rule = TodoRepeatRule.ONCE,
                baseDate = monday,
                date = monday.plusDays(1),
                completedDates = setOf(monday),
            ),
        )
        assertFalse(TodoRecurrence.isDue(TodoRepeatRule.ONCE, null, monday))
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.DAILY, monday, monday.plusDays(1)))
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.WEEKLY, monday, monday.plusDays(7)))
        assertFalse(TodoRecurrence.isDue(TodoRepeatRule.WEEKLY, monday, monday.plusDays(1)))
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.WORKDAYS, monday, monday.plusDays(4)))
        assertFalse(TodoRecurrence.isDue(TodoRepeatRule.WORKDAYS, monday, monday.plusDays(5)))
        assertTrue(TodoRecurrence.isDue(TodoRepeatRule.CUSTOM, monday, monday.plusDays(2), setOf(DayOfWeek.WEDNESDAY)))
    }

    @Test
    fun `rest day does not break a streak`() {
        val streak = StreakCalculator.calculate(
            datesWithRequiredCompletion = setOf(monday, monday.minusDays(2)),
            today = monday,
            restDays = setOf(DayOfWeek.SUNDAY),
        )

        assertTrue(streak == 2)
    }

    @Test
    fun `all weekdays configured as rest days do not loop forever`() {
        val streak = StreakCalculator.calculate(
            datesWithRequiredCompletion = setOf(monday),
            today = monday,
            restDays = DayOfWeek.values().toSet(),
        )

        assertTrue(streak == 0)
    }
}
