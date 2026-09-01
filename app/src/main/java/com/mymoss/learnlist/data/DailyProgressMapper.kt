package com.mymoss.learnlist.data

import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReadingPlanEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.ReadingAdjustmentEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.DailyProgressInput
import com.mymoss.learnlist.domain.DailyProjectProgress
import com.mymoss.learnlist.domain.DailyReadingProgress
import com.mymoss.learnlist.domain.DailyTaskProgress
import com.mymoss.learnlist.domain.DailyTodoProgress
import com.mymoss.learnlist.domain.TodoCompletion
import com.mymoss.learnlist.domain.TodoRepeatRule
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Converts persisted records into the input shared by all daily-progress consumers. */
object DailyProgressMapper {
    fun from(snapshot: BackupSnapshot, zoneId: ZoneId = ZoneId.systemDefault()): DailyProgressInput = from(
        projects = snapshot.projects,
        tasks = snapshot.tasks,
        reviewLogs = snapshot.reviewLogs,
        readingPlans = snapshot.readingPlans,
        readingTargets = snapshot.readingTargets,
        pageLogs = snapshot.pageLogs,
        readingAdjustments = snapshot.readingAdjustments,
        todos = snapshot.todos,
        zoneId = zoneId,
    )

    fun from(
        projects: List<ProjectEntity>,
        tasks: List<LearningTaskEntity>,
        reviewLogs: List<ReviewLogEntity>,
        readingPlans: List<ReadingPlanEntity>,
        readingTargets: List<ReadingTargetEntity>,
        pageLogs: List<PageLogEntity>,
        readingAdjustments: List<ReadingAdjustmentEntity> = emptyList(),
        todos: List<TodoEntity>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): DailyProgressInput {
        val reviewedDatesByTask = reviewLogs.groupBy(ReviewLogEntity::taskId).mapValues { (_, logs) ->
            logs.mapNotNull { it.reviewedOn.asLocalDate() }.toSet()
        }
        val pagesByPlan = (pageLogs.groupBy(PageLogEntity::planId).keys + readingAdjustments.map(ReadingAdjustmentEntity::planId))
            .distinct()
            .associateWith { planId ->
                val logged = pageLogs.filter { it.planId == planId }.groupingBy { it.localDate.asLocalDate() }
                    .fold(0) { total, log -> total + log.pagesRead.coerceAtLeast(0) }
                val adjustments = readingAdjustments.filter { it.planId == planId }.groupingBy { it.localDate.asLocalDate() }
                    .fold(0) { total, adjustment -> total + adjustment.deltaPages }
                (logged.keys + adjustments.keys).filterNotNull().associateWith { date ->
                    ((logged[date] ?: 0) + (adjustments[date] ?: 0)).coerceAtLeast(0)
                }
            }
        val targetsByPlan = readingTargets.groupBy(ReadingTargetEntity::planId).mapValues { (_, targets) ->
            targets.mapNotNull { target -> target.localDate.asLocalDate()?.let { it to target.targetPages } }.toMap()
        }

        return DailyProgressInput(
            projects = projects.filter { it.deletedAt == null }.map { project ->
                DailyProjectProgress(project.id, project.isArchived, project.isPaused)
            },
            tasks = tasks.filter { it.deletedAt == null }.map { task ->
                DailyTaskProgress(
                    id = task.id,
                    projectId = task.projectId,
                    isRequired = task.isRequired,
                    isArchived = task.isArchived,
                    hasLearned = task.hasLearned,
                    initialLearningDate = task.initialLearningDate.asLocalDate(),
                    nextReviewDate = task.nextReviewDate.asLocalDate(),
                    snoozedUntil = task.snoozedUntil.asLocalDate(),
                    createdOn = task.createdAt.toLocalDate(zoneId),
                    reviewedDates = reviewedDatesByTask[task.id].orEmpty(),
                )
            },
            readings = readingPlans.filter { it.deletedAt == null }.map { plan ->
                DailyReadingProgress(
                    id = plan.id,
                    projectId = plan.projectId,
                    totalPages = plan.totalPages,
                    currentPage = plan.currentPage,
                    dailyTarget = plan.dailyTarget,
                    startDate = plan.startDate.asLocalDate() ?: LocalDate.MIN,
                    isPaused = plan.isPaused,
                    isArchived = plan.isArchived,
                    pagesByDate = pagesByPlan[plan.id].orEmpty(),
                    targetsByDate = targetsByPlan[plan.id].orEmpty(),
                )
            },
            todos = todos.filter { it.deletedAt == null }.map { todo ->
                DailyTodoProgress(
                    id = todo.id,
                    projectId = todo.projectId,
                    isRequired = todo.isRequired,
                    isArchived = todo.isArchived,
                    repeatRule = runCatching { TodoRepeatRule.valueOf(todo.repeatRule) }.getOrDefault(TodoRepeatRule.ONCE),
                    baseDate = todo.dueDate.asLocalDate(),
                    customDays = todo.customRepeatDays.toDayOfWeekSet(),
                    completedDates = TodoCompletion.dates(todo.completedDates),
                )
            },
        )
    }

    private fun String?.asLocalDate(): LocalDate? = this?.let { value ->
        runCatching { LocalDate.parse(value) }.getOrNull()
    }

    private fun String.toDayOfWeekSet(): Set<DayOfWeek> = split(',').mapNotNull { token ->
        token.trim().toIntOrNull()?.takeIf { it in 1..7 }?.let { runCatching { DayOfWeek.of(it) }.getOrNull() }
    }.toSet()

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
