package com.mymoss.learnlist.domain

import java.time.DayOfWeek
import java.time.LocalDate

enum class TodoRepeatRule { ONCE, DAILY, WEEKLY, WORKDAYS, CUSTOM }

object TodoRecurrence {
    fun isDue(
        rule: TodoRepeatRule,
        baseDate: LocalDate?,
        date: LocalDate,
        customDays: Set<DayOfWeek> = emptySet(),
    ): Boolean {
        if (baseDate != null && date.isBefore(baseDate)) return false
        return when (rule) {
            TodoRepeatRule.ONCE -> baseDate != null && date == baseDate
            TodoRepeatRule.DAILY -> true
            TodoRepeatRule.WEEKLY -> baseDate == null || date.dayOfWeek == baseDate.dayOfWeek
            TodoRepeatRule.WORKDAYS -> date.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY
            TodoRepeatRule.CUSTOM -> date.dayOfWeek in customDays
        }
    }

    fun isCompleted(completedDates: String, date: LocalDate): Boolean =
        completedDates.split(',').any { it == date.toString() }
}

object StreakCalculator {
    fun calculate(
        datesWithRequiredCompletion: Set<LocalDate>,
        today: LocalDate,
        restDays: Set<DayOfWeek> = emptySet(),
        pausedDates: Set<LocalDate> = emptySet(),
    ): Int {
        var cursor = today
        var streak = 0
        var inspectedDays = 0
        while (inspectedDays < 10_000) {
            inspectedDays += 1
            if (cursor.dayOfWeek in restDays || cursor in pausedDates) {
                cursor = cursor.minusDays(1)
                continue
            }
            if (cursor !in datesWithRequiredCompletion) break
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
