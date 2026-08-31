package com.mymoss.learnlist.data

import androidx.room.withTransaction
import com.mymoss.learnlist.data.local.CountdownEntity
import com.mymoss.learnlist.data.local.FocusSessionEntity
import com.mymoss.learnlist.data.local.GoalEntity
import com.mymoss.learnlist.data.local.LearnListDatabase
import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReadingPlanEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.DefaultReviewScheduler
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.ReviewState
import com.mymoss.learnlist.domain.ReadingPlanCalculator
import com.mymoss.learnlist.domain.ReadingPlanService
import com.mymoss.learnlist.domain.TodoCompletion
import com.mymoss.learnlist.domain.TodoRepeatRule
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class LearnListRepository(
    private val database: LearnListDatabase,
    private val scheduler: DefaultReviewScheduler = DefaultReviewScheduler(),
    private val readingPlanService: ReadingPlanService = ReadingPlanCalculator(),
) {
    private val dao = database.dao()

    fun observeProjects(): Flow<List<ProjectEntity>> = dao.observeProjects()
    fun observeArchivedProjects(): Flow<List<ProjectEntity>> = dao.observeArchivedProjects()
    fun observeTasks(): Flow<List<LearningTaskEntity>> = dao.observeTasks()
    fun observeReviewLogs(): Flow<List<ReviewLogEntity>> = dao.observeReviewLogs()
    fun observeReadingPlans(): Flow<List<ReadingPlanEntity>> = dao.observeReadingPlans()
    fun observeReadingTargets(): Flow<List<ReadingTargetEntity>> = dao.observeReadingTargets()
    fun observePageLogs(): Flow<List<PageLogEntity>> = dao.observePageLogs()
    fun observeTodos(): Flow<List<TodoEntity>> = dao.observeTodos()
    fun observeFocusSessions(): Flow<List<FocusSessionEntity>> = dao.observeFocusSessions()
    fun observeGoals(): Flow<List<GoalEntity>> = dao.observeGoals()
    fun observeCountdowns(): Flow<List<CountdownEntity>> = dao.observeCountdowns()
    fun observeReminders(): Flow<List<ReminderEntity>> = dao.observeReminders()

    suspend fun addProject(
        title: String,
        type: String,
        description: String = "",
        tags: String = "",
    ): ProjectEntity {
        require(title.isNotBlank()) { "项目名称不能为空" }
        val now = System.currentTimeMillis()
        val project = ProjectEntity(
            id = newId(), title = title.trim(), type = type,
            description = description.trim(), tagCsv = tags.trim(),
            colorHex = "#64D8CB", isArchived = false, isPaused = false,
            createdAt = now, updatedAt = now,
        )
        dao.insertProject(project)
        return project
    }

    suspend fun addTask(
        projectId: String,
        title: String,
        prompt: String = "",
        notes: String = "",
        source: String = "",
        isRequired: Boolean = true,
    ): LearningTaskEntity {
        require(title.isNotBlank()) { "学习任务名称不能为空" }
        require(dao.getProject(projectId) != null) { "学习项目不存在" }
        val now = System.currentTimeMillis()
        val task = LearningTaskEntity(
            id = newId(), projectId = projectId, title = title.trim(),
            prompt = prompt.trim(), notes = notes.trim(), source = source.trim(),
            isRequired = isRequired, isArchived = false, hasLearned = false,
            initialLearningDate = null,
            stage = 0, nextReviewDate = null, snoozedUntil = null,
            createdAt = now, updatedAt = now,
        )
        dao.insertTask(task)
        return task
    }

    suspend fun completeInitialLearning(taskId: String, completedDate: LocalDate = LocalDate.now()) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            if (task.hasLearned) return@withTransaction
            val state = scheduler.completeInitialLearning(completedDate)
            dao.updateTask(
                task.copy(
                    hasLearned = true,
                    initialLearningDate = completedDate.toString(),
                    stage = state.stage,
                    nextReviewDate = state.nextReviewDate?.toString(),
                    snoozedUntil = null,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun reviewTask(
        taskId: String,
        rating: RecallRating,
        completedDate: LocalDate = LocalDate.now(),
        snoozeUntil: LocalDate? = null,
    ) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            val decision = scheduler.review(
                state = ReviewState(task.stage, task.nextReviewDate?.let(LocalDate::parse)),
                rating = rating,
                completedDate = completedDate,
                snoozeUntil = snoozeUntil,
            )
            dao.updateTask(
                task.copy(
                    hasLearned = true,
                    stage = decision.state.stage,
                    nextReviewDate = decision.state.nextReviewDate?.toString(),
                    snoozedUntil = if (rating == RecallRating.SNOOZE) {
                        snoozeUntil?.toString()
                    } else {
                        null
                    },
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            decision.log?.let { log ->
                dao.insertReviewLog(
                    ReviewLogEntity(
                        id = newId(), taskId = task.id, rating = log.rating.name,
                        reviewedOn = log.reviewedOn.toString(),
                        previousStage = log.previousStage, nextStage = log.nextStage,
                        nextReviewDate = log.nextReviewDate.toString(),
                        createdAt = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    suspend fun snoozeTask(taskId: String, until: LocalDate = LocalDate.now().plusDays(1)) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            dao.updateTask(task.copy(snoozedUntil = until.toString(), updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun addReadingPlan(
        projectId: String,
        title: String,
        totalPages: Int,
        dailyTarget: Int,
        startDate: LocalDate = LocalDate.now(),
        deadline: LocalDate? = null,
    ): ReadingPlanEntity {
        require(title.isNotBlank()) { "阅读计划名称不能为空" }
        require(dao.getProject(projectId) != null) { "学习项目不存在" }
        require(totalPages > 0)
        require(dailyTarget > 0)
        require(deadline == null || !deadline.isBefore(startDate)) { "阅读截止日不能早于开始日" }
        val now = System.currentTimeMillis()
        val plan = ReadingPlanEntity(
            id = newId(), projectId = projectId, title = title.trim(),
            totalPages = totalPages, dailyTarget = dailyTarget, currentPage = 0,
            startDate = startDate.toString(), deadline = deadline?.toString(),
            isPaused = false, isArchived = false, createdAt = now, updatedAt = now,
        )
        dao.insertReadingPlan(plan)
        return plan
    }

    suspend fun logReading(
        planId: String,
        date: LocalDate,
        pages: Int,
        startPage: Int? = null,
        endPage: Int? = null,
    ) {
        require(pages > 0)
        database.withTransaction {
            val plan = dao.getReadingPlan(planId) ?: return@withTransaction
            val remainingPages = (plan.totalPages - plan.currentPage).coerceAtLeast(0)
            val actualPages = pages.coerceAtMost(remainingPages)
            if (actualPages == 0) return@withTransaction
            val nextPage = plan.currentPage + actualPages
            dao.insertPageLog(
                PageLogEntity(
                    id = newId(), planId = planId, localDate = date.toString(),
                    pagesRead = actualPages,
                    startPage = startPage ?: plan.currentPage + 1,
                    endPage = endPage ?: nextPage,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            dao.updateReadingPlan(plan.copy(currentPage = nextPage, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun setReadingTarget(
        planId: String,
        date: LocalDate,
        targetPages: Int,
    ) {
        require(targetPages > 0) { "每日页数需要大于 0" }
        require(dao.getReadingPlan(planId) != null) { "阅读计划不存在" }
        dao.insertReadingTarget(
            ReadingTargetEntity(
                id = "$planId:$date",
                planId = planId,
                localDate = date.toString(),
                targetPages = targetPages,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    /** Generates dated targets for the remaining pages through the plan deadline. */
    suspend fun rebalanceReadingPlan(planId: String, from: LocalDate = LocalDate.now()) {
        database.withTransaction {
            val plan = dao.getReadingPlan(planId) ?: return@withTransaction
            val deadline = plan.deadline?.let(LocalDate::parse) ?: error("请先为阅读计划设置截止日")
            val start = maxOf(from, LocalDate.parse(plan.startDate))
            val targets = readingPlanService.rebalance(plan.currentPage, plan.totalPages, start, deadline)
            dao.insertReadingTargets(
                targets.map { target ->
                    ReadingTargetEntity(
                        id = "$planId:${target.date}", planId = planId,
                        localDate = target.date.toString(), targetPages = target.pages,
                        updatedAt = System.currentTimeMillis(),
                    )
                },
            )
            targets.firstOrNull()?.let { first ->
                dao.updateReadingPlan(plan.copy(dailyTarget = first.pages.coerceAtLeast(1), updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun addTodo(
        title: String,
        notes: String = "",
        isRequired: Boolean = true,
        repeatRule: String = "ONCE",
        customRepeatDays: String = "",
        dueDate: LocalDate? = LocalDate.now(),
    ): TodoEntity {
        require(title.isNotBlank()) { "待办内容不能为空" }
        val normalizedRule = runCatching { TodoRepeatRule.valueOf(repeatRule) }.getOrNull()?.name
            ?: error("重复规则无效")
        val normalizedCustomDays = customRepeatDays.split(',')
            .mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }
            .distinct()
            .sorted()
            .joinToString(",")
        require(normalizedRule != TodoRepeatRule.CUSTOM.name || normalizedCustomDays.isNotBlank()) { "自定义待办至少选择一天" }
        require(normalizedRule != TodoRepeatRule.ONCE.name || dueDate != null) { "一次性待办需要设置日期" }
        val now = System.currentTimeMillis()
        val todo = TodoEntity(
            id = newId(), title = title.trim(), notes = notes.trim(),
            isRequired = isRequired, repeatRule = normalizedRule,
            customRepeatDays = normalizedCustomDays, dueDate = dueDate?.toString(),
            completedDates = "", isArchived = false, createdAt = now, updatedAt = now,
        )
        dao.insertTodo(todo)
        return todo
    }

    suspend fun completeTodo(todoId: String, date: LocalDate = LocalDate.now()) {
        setTodoCompleted(todoId, date, completed = true)
    }

    suspend fun setTodoCompleted(todoId: String, date: LocalDate = LocalDate.now(), completed: Boolean) {
        val todo = dao.getTodo(todoId) ?: return
        dao.updateTodo(
            todo.copy(
                completedDates = TodoCompletion.setCompleted(todo.completedDates, date, completed),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun addFocusSession(
        plannedMinutes: Int,
        actualMinutes: Int = plannedMinutes,
        projectId: String? = null,
        taskId: String? = null,
        startedAt: Long = System.currentTimeMillis() - actualMinutes * 60_000L,
    ): FocusSessionEntity = database.withTransaction {
        require(plannedMinutes > 0)
        val session = FocusSessionEntity(
            id = newId(), projectId = projectId, taskId = taskId,
            startedAt = startedAt, endedAt = System.currentTimeMillis(),
            plannedMinutes = plannedMinutes, actualMinutes = actualMinutes.coerceAtLeast(0),
            status = "COMPLETED",
        )
        dao.insertFocusSession(session)
        session
    }

    /** Records a recovered/completed timer at most once across process restarts. */
    suspend fun recordFocusSessionIfNeeded(
        plannedMinutes: Int,
        actualMinutes: Int = plannedMinutes,
        projectId: String? = null,
        taskId: String? = null,
        startedAt: Long,
    ): FocusSessionEntity = database.withTransaction {
        dao.getFocusSessionByStartedAt(startedAt) ?: run {
            require(plannedMinutes > 0)
            FocusSessionEntity(
                id = newId(), projectId = projectId, taskId = taskId,
                startedAt = startedAt, endedAt = System.currentTimeMillis(),
                plannedMinutes = plannedMinutes, actualMinutes = actualMinutes.coerceAtLeast(0),
                status = "COMPLETED",
            ).also { dao.insertFocusSession(it) }
        }
    }

    suspend fun addGoal(
        title: String,
        metric: String,
        targetValue: Int,
        period: String,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        projectId: String? = null,
    ): GoalEntity {
        require(title.isNotBlank()) { "目标名称不能为空" }
        require(metric in setOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE")) { "目标指标无效" }
        require(period in setOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM")) { "目标周期无效" }
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        require(targetValue > 0)
        require(endDate == null || !endDate.isBefore(startDate)) { "目标截止日不能早于开始日" }
        val now = System.currentTimeMillis()
        val goal = GoalEntity(
            id = newId(), title = title.trim(), metric = metric,
            targetValue = targetValue, period = period, startDate = startDate.toString(),
            endDate = endDate?.toString(), projectId = projectId, isArchived = false,
            createdAt = now, updatedAt = now,
        )
        dao.insertGoal(goal)
        return goal
    }

    suspend fun addCountdown(
        title: String,
        eventAtEpochMillis: Long,
        note: String = "",
        reminderMinutesBefore: Int? = null,
    ): CountdownEntity {
        require(title.isNotBlank()) { "倒计时名称不能为空" }
        require(eventAtEpochMillis > 0) { "事件时间无效" }
        require(reminderMinutesBefore == null || reminderMinutesBefore in 0..43_200) { "提前提醒时间无效" }
        val now = System.currentTimeMillis()
        val countdown = CountdownEntity(
            id = newId(), title = title.trim(), note = note.trim(),
            eventAtEpochMillis = eventAtEpochMillis,
            reminderMinutesBefore = reminderMinutesBefore,
            isCompleted = false, isArchived = false, createdAt = now, updatedAt = now,
        )
        dao.insertCountdown(countdown)
        return countdown
    }

    suspend fun completeCountdown(id: String) {
        val items = dao.getAllCountdowns()
        items.firstOrNull { it.id == id }?.let {
            dao.updateCountdown(it.copy(isCompleted = true, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun addReminder(
        projectId: String?,
        kind: String,
        timeMinutes: Int,
        repeatDays: String = "1,2,3,4,5,6,7",
        quietStartMinutes: Int? = null,
        quietEndMinutes: Int? = null,
    ): ReminderEntity {
        require(timeMinutes in 0..1439)
        require(kind == "SUMMARY" || kind == "PROJECT") { "提醒类型无效" }
        require(kind != "PROJECT" || !projectId.isNullOrBlank()) { "项目提醒必须选择项目" }
        require(kind != "SUMMARY" || projectId == null) { "每日进度提醒不能绑定项目" }
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        val normalizedDays = repeatDays.split(',')
            .mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }
            .distinct()
            .sorted()
            .joinToString(",")
        require(normalizedDays.isNotBlank()) { "至少选择一天提醒" }
        require(quietStartMinutes == null || quietStartMinutes in 0..1439) { "安静开始时间无效" }
        require(quietEndMinutes == null || quietEndMinutes in 0..1439) { "安静结束时间无效" }
        val now = System.currentTimeMillis()
        val reminder = ReminderEntity(
            id = newId(), projectId = projectId, kind = kind,
            timeMinutes = timeMinutes, repeatDays = normalizedDays, enabled = true,
            quietStartMinutes = quietStartMinutes, quietEndMinutes = quietEndMinutes,
            createdAt = now, updatedAt = now,
        )
        dao.insertReminder(reminder)
        return reminder
    }

    suspend fun archiveProject(projectId: String, archived: Boolean = true) {
        dao.getProject(projectId)?.let { dao.updateProject(it.copy(isArchived = archived, updatedAt = System.currentTimeMillis())) }
    }

    suspend fun setProjectPaused(projectId: String, paused: Boolean) {
        dao.getProject(projectId)?.let { dao.updateProject(it.copy(isPaused = paused, updatedAt = System.currentTimeMillis())) }
    }

    suspend fun archiveTask(taskId: String, archived: Boolean = true) {
        dao.getTask(taskId)?.let { dao.updateTask(it.copy(isArchived = archived, updatedAt = System.currentTimeMillis())) }
    }

    suspend fun setReminderEnabled(reminderId: String, enabled: Boolean) {
        dao.getAllReminders().firstOrNull { it.id == reminderId }?.let {
            dao.updateReminder(it.copy(enabled = enabled, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun snapshot(): BackupSnapshot = BackupSnapshot(
        projects = dao.getAllProjects(), tasks = dao.getAllTasks(),
        reviewLogs = dao.getAllReviewLogs(), readingPlans = dao.getAllReadingPlans(),
        readingTargets = dao.getAllReadingTargets(),
        pageLogs = dao.getAllPageLogs(), todos = dao.getAllTodos(),
        focusSessions = dao.getAllFocusSessions(), goals = dao.getAllGoals(),
        countdowns = dao.getAllCountdowns(), reminders = dao.getAllReminders(),
    )

    suspend fun replaceAll(snapshot: BackupSnapshot) {
        database.withTransaction {
            dao.clearReminders(); dao.clearCountdowns(); dao.clearGoals();
            dao.clearFocusSessions(); dao.clearTodos(); dao.clearPageLogs();
            dao.clearReadingTargets(); dao.clearReadingPlans(); dao.clearReviewLogs(); dao.clearTasks(); dao.clearProjects()
            dao.insertProjects(snapshot.projects); dao.insertTasks(snapshot.tasks)
            dao.insertReviewLogs(snapshot.reviewLogs); dao.insertReadingPlans(snapshot.readingPlans); dao.insertReadingTargets(snapshot.readingTargets)
            dao.insertPageLogs(snapshot.pageLogs); dao.insertTodos(snapshot.todos)
            dao.insertFocusSessions(snapshot.focusSessions); dao.insertGoals(snapshot.goals)
            dao.insertCountdowns(snapshot.countdowns); dao.insertReminders(snapshot.reminders)
        }
    }

    suspend fun merge(snapshot: BackupSnapshot) {
        database.withTransaction {
            dao.insertProjects(snapshot.projects); dao.insertTasks(snapshot.tasks)
            dao.insertReviewLogs(snapshot.reviewLogs); dao.insertReadingPlans(snapshot.readingPlans); dao.insertReadingTargets(snapshot.readingTargets)
            dao.insertPageLogs(snapshot.pageLogs); dao.insertTodos(snapshot.todos)
            dao.insertFocusSessions(snapshot.focusSessions); dao.insertGoals(snapshot.goals)
            dao.insertCountdowns(snapshot.countdowns); dao.insertReminders(snapshot.reminders)
        }
    }

    private fun newId(): String = UUID.randomUUID().toString()
}

data class BackupSnapshot(
    val projects: List<ProjectEntity>,
    val tasks: List<LearningTaskEntity>,
    val reviewLogs: List<ReviewLogEntity>,
    val readingPlans: List<ReadingPlanEntity>,
    val readingTargets: List<ReadingTargetEntity> = emptyList(),
    val pageLogs: List<PageLogEntity>,
    val todos: List<TodoEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val goals: List<GoalEntity>,
    val countdowns: List<CountdownEntity>,
    val reminders: List<ReminderEntity>,
)

