package com.mymoss.learnlist.domain

import java.time.LocalDate

enum class GoalMetric(val storageValue: String) {
    FOCUS_MINUTES("FOCUS_MINUTES"),
    READING_PAGES("READING_PAGES"),
    REVIEW_TASKS("REVIEW_TASKS"),
    TODO_DONE("TODO_DONE"),
    ;

    companion object {
        fun fromStorage(value: String): GoalMetric? = entries.firstOrNull { it.storageValue == value }
    }
}

enum class GoalPeriod(val storageValue: String) {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    CUSTOM("CUSTOM"),
    ;

    companion object {
        fun fromStorage(value: String): GoalPeriod? = entries.firstOrNull { it.storageValue == value }
    }
}

data class GoalDefinition(
    val metric: GoalMetric,
    val period: GoalPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val projectId: String? = null,
)

data class GoalActivity(
    val metric: GoalMetric,
    val date: LocalDate,
    val value: Int,
    val projectId: String? = null,
)

/** Aggregates dated learning activity for a goal without depending on the UI or database. */
class GoalProgressAggregator {
    fun current(
        goal: GoalDefinition,
        today: LocalDate,
        activities: List<GoalActivity>,
    ): Int {
        val periodStart = when (goal.period) {
            GoalPeriod.DAILY -> today
            GoalPeriod.WEEKLY -> today.minusDays((today.dayOfWeek.value - 1).toLong())
            GoalPeriod.MONTHLY -> today.withDayOfMonth(1)
            GoalPeriod.CUSTOM -> goal.startDate
        }
        val start = maxOf(periodStart, goal.startDate)
        val end = minOf(goal.endDate ?: today, today)
        if (end.isBefore(start)) return 0

        return activities.asSequence()
            .filter { activity ->
                activity.metric == goal.metric &&
                    !activity.date.isBefore(start) &&
                    !activity.date.isAfter(end) &&
                    (goal.projectId == null || activity.projectId == goal.projectId)
            }
            .sumOf { it.value.coerceAtLeast(0) }
    }
}
