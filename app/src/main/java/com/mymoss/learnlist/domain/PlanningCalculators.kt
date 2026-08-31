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

    fun calculate(
        input: DailyProgressInput,
        date: java.time.LocalDate,
        projectId: String? = null,
    ): DailyProgressSummary {
        val activeProjectIds = input.projects.asSequence()
            .filter { !it.isArchived && !it.isPaused }
            .filter { projectId == null || it.id == projectId }
            .map(DailyProjectProgress::id)
            .toSet()
        val actions = buildList {
            input.tasks
                .filter { it.isRequired && !it.isArchived && it.projectId in activeProjectIds }
                .filter { task ->
                    task.createdOn?.isAfter(date) != true &&
                        (isDue(task, date) || date in task.reviewedDates || task.initialLearningDate == date)
                }
                .forEach { task ->
                    add(DailyAction(isRequired = true, isCompleted = date in task.reviewedDates || task.initialLearningDate == date))
                }

            input.readings
                .filter { reading ->
                    !reading.isPaused && !reading.isArchived && reading.projectId in activeProjectIds &&
                        !date.isBefore(reading.startDate) &&
                        (reading.currentPage < reading.totalPages || date in reading.pagesByDate)
                }
                .forEach { reading ->
                    val target = reading.targetsByDate[date] ?: reading.dailyTarget
                    val pages = reading.pagesByDate[date] ?: 0
                    add(DailyAction(isRequired = true, isCompleted = pages >= target.coerceAtLeast(1)))
                }

            if (projectId == null) {
                input.todos
                    .filter { it.isRequired && !it.isArchived }
                    .filter { todo ->
                        TodoRecurrence.isDue(
                            rule = todo.repeatRule,
                            baseDate = todo.baseDate,
                            date = date,
                            customDays = todo.customDays,
                            completedDates = todo.completedDates,
                        )
                    }
                    .forEach { todo -> add(DailyAction(isRequired = true, isCompleted = date in todo.completedDates)) }
            }
        }
        return calculate(actions)
    }

    private fun isDue(task: DailyTaskProgress, date: java.time.LocalDate): Boolean {
        if (task.snoozedUntil?.isAfter(date) == true) return false
        if (!task.hasLearned) return true
        return task.nextReviewDate == null || !task.nextReviewDate.isAfter(date)
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
        val allocationDays = minOf(days, remaining)
        val base = remaining / allocationDays
        val remainder = remaining % allocationDays
        return (0 until allocationDays).map { offset ->
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

