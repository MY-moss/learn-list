package com.mymoss.learnlist.domain

import java.time.DayOfWeek
import java.time.LocalDate

data class DailyProjectProgress(
    val id: String,
    val isArchived: Boolean,
    val isPaused: Boolean,
)

data class DailyTaskProgress(
    val id: String,
    val projectId: String,
    val isRequired: Boolean,
    val isArchived: Boolean,
    val hasLearned: Boolean,
    val initialLearningDate: LocalDate?,
    val nextReviewDate: LocalDate?,
    val snoozedUntil: LocalDate?,
    val createdOn: LocalDate?,
    val reviewedDates: Set<LocalDate>,
)

data class DailyReadingProgress(
    val id: String,
    val projectId: String,
    val totalPages: Int,
    val currentPage: Int,
    val dailyTarget: Int,
    val startDate: LocalDate,
    val isPaused: Boolean,
    val isArchived: Boolean,
    val pagesByDate: Map<LocalDate, Int>,
    val targetsByDate: Map<LocalDate, Int>,
)

data class DailyTodoProgress(
    val id: String,
    val isRequired: Boolean,
    val isArchived: Boolean,
    val repeatRule: TodoRepeatRule,
    val baseDate: LocalDate?,
    val customDays: Set<DayOfWeek>,
    val completedDates: Set<LocalDate>,
)

data class DailyProgressInput(
    val projects: List<DailyProjectProgress>,
    val tasks: List<DailyTaskProgress>,
    val readings: List<DailyReadingProgress>,
    val todos: List<DailyTodoProgress>,
)

object InitialLearningTracker {
    fun isCompletedOn(recordedDate: String?, date: LocalDate): Boolean =
        recordedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() == date } == true
}

