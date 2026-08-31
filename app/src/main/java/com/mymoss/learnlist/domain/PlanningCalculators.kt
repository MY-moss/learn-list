package com.mymoss.learnlist.domain

import java.time.LocalDate

data class DailyAction(
    val isRequired: Boolean,
    val isCompleted: Boolean,
)

data class DailyProgressSummary(
    val completedRequired: Int,
    val totalRequired: Int,
    val percent: Int?,
)

class DailyProgressCalculator {
    fun calculate(actions: List<DailyAction>): DailyProgressSummary {
        val required = actions.filter(DailyAction::isRequired)
        val completed = required.count(DailyAction::isCompleted)
        val percent = if (required.isEmpty()) null else completed * 100 / required.size
        return DailyProgressSummary(
            completedRequired = completed,
            totalRequired = required.size,
            percent = percent,
        )
    }
}

data class PageTarget(
    val date: LocalDate,
    val pages: Int,
)

interface ReadingPlanService {
    fun rebalance(
        currentPage: Int,
        totalPages: Int,
        from: LocalDate,
        deadline: LocalDate,
    ): List<PageTarget>
}

class ReadingPlanCalculator : ReadingPlanService {
    /**
     * Reallocates the unread pages over the inclusive date range. Earlier dates
     * receive the remainder so that every page is accounted for exactly once.
     */
    override fun rebalance(
        currentPage: Int,
        totalPages: Int,
        from: LocalDate,
        deadline: LocalDate,
    ): List<PageTarget> {
        require(totalPages >= 0) { "totalPages must not be negative" }
        require(currentPage in 0..totalPages) { "currentPage must be within the book" }
        if (deadline.isBefore(from)) return emptyList()

        val days = (deadline.toEpochDay() - from.toEpochDay() + 1).toInt()
        val remaining = totalPages - currentPage
        if (remaining == 0) return emptyList()
        val base = remaining / days
        val remainder = remaining % days
        return (0 until days).map { offset ->
            PageTarget(
                date = from.plusDays(offset.toLong()),
                pages = base + if (offset < remainder) 1 else 0,
            )
        }
    }
}

data class GoalProgress(
    val percent: Int,
    val isComplete: Boolean,
)

class GoalProgressCalculator {
    fun calculate(current: Int, target: Int): GoalProgress {
        require(target > 0) { "target must be positive" }
        val percent = (current.coerceAtLeast(0) * 100 / target).coerceIn(0, 100)
        return GoalProgress(percent = percent, isComplete = current >= target)
    }
}
