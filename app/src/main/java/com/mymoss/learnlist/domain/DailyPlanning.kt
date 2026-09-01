package com.mymoss.learnlist.domain

import java.time.DayOfWeek
import java.time.LocalDate

enum class TodoRepeatRule { ONCE, DAILY, WEEKLY, WORKDAYS, CUSTOM }

object TodoCompletion {
    fun dates(completedDates: String): Set<LocalDate> = completedDates.split(',')
        .mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }
        .toSet()

    fun isCompleted(completedDates: String, date: LocalDate): Boolean = date in dates(completedDates)

    fun setCompleted(completedDates: String, date: LocalDate, completed: Boolean): String {
        val next = dates(completedDates).toMutableSet()
        if (completed) next += date else next -= date
        return next.sorted().joinToString(",")
    }

    fun toggle(completedDates: String, date: LocalDate): String =
        setCompleted(completedDates, date, !isCompleted(completedDates, date))
}

object TodoRecurrence {
    fun isDue(
        rule: TodoRepeatRule,
        baseDate: LocalDate?,
        date: LocalDate,
        customDays: Set<DayOfWeek> = emptySet(),
        completedDates: Set<LocalDate> = emptySet(),
    ): Boolean {
        if (baseDate != null && date.isBefore(baseDate)) return false
        return when (rule) {
            TodoRepeatRule.ONCE -> {
                val completedOnOrBeforeDate = completedDates.any { !it.isAfter(date) }
                baseDate != null && !date.isBefore(baseDate) &&
                    (!completedOnOrBeforeDate || date in completedDates)
            }
           TodoRepeatRule.DAILY -> true
           TodoRepeatRule.WEEKLY -> baseDate == null || date.dayOfWeek == baseDate.dayOfWeek
           TodoRepeatRule.WORKDAYS -> date.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY
           TodoRepeatRule.CUSTOM -> date.dayOfWeek in customDays
       }
   }

    fun isCompleted(completedDates: String, date: LocalDate): Boolean = TodoCompletion.isCompleted(completedDates, date)
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

