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
import com.mymoss.learnlist.data.local.ReadingAdjustmentEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.ReviewCorrectionEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.DefaultReviewScheduler
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.ReviewState
import com.mymoss.learnlist.domain.ReadingPlanCalculator
import com.mymoss.learnlist.domain.ReadingPlanService
import com.mymoss.learnlist.domain.TodoCompletion
import com.mymoss.learnlist.domain.TodoRepeatRule
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class LearnListRepository(
    private val database: LearnListDatabase,
    private val scheduler: DefaultReviewScheduler = DefaultReviewScheduler(),
    private val readingPlanService: ReadingPlanService = ReadingPlanCalculator(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val dao = database.dao()

    private fun today(): LocalDate = LocalDate.now(clock)
    private fun nowMillis(): Long = clock.millis()

    fun observeProjects(): Flow<List<ProjectEntity>> = dao.observeProjects()
    fun observeArchivedProjects(): Flow<List<ProjectEntity>> = dao.observeArchivedProjects()
    fun observeDeletedProjects(): Flow<List<ProjectEntity>> = dao.observeDeletedProjects()
    fun observeTasks(): Flow<List<LearningTaskEntity>> = dao.observeTasks()
    fun observeDeletedTasks(): Flow<List<LearningTaskEntity>> = dao.observeDeletedTasks()
    fun observeReviewLogs(): Flow<List<ReviewLogEntity>> = dao.observeReviewLogs()
    fun observeReviewCorrections(): Flow<List<ReviewCorrectionEntity>> = dao.observeReviewCorrections()
    fun observeReadingPlans(): Flow<List<ReadingPlanEntity>> = dao.observeReadingPlans()
    fun observeDeletedReadingPlans(): Flow<List<ReadingPlanEntity>> = dao.observeDeletedReadingPlans()
    fun observeReadingTargets(): Flow<List<ReadingTargetEntity>> = dao.observeReadingTargets()
    fun observePageLogs(): Flow<List<PageLogEntity>> = dao.observePageLogs()
    fun observeReadingAdjustments(): Flow<List<ReadingAdjustmentEntity>> = dao.observeReadingAdjustments()
    fun observeTodos(): Flow<List<TodoEntity>> = dao.observeTodos()
    fun observeDeletedTodos(): Flow<List<TodoEntity>> = dao.observeDeletedTodos()
    fun observeFocusSessions(): Flow<List<FocusSessionEntity>> = dao.observeFocusSessions()
    fun observeGoals(): Flow<List<GoalEntity>> = dao.observeGoals()
    fun observeDeletedGoals(): Flow<List<GoalEntity>> = dao.observeDeletedGoals()
    fun observeCountdowns(): Flow<List<CountdownEntity>> = dao.observeCountdowns()
    fun observeDeletedCountdowns(): Flow<List<CountdownEntity>> = dao.observeDeletedCountdowns()
    fun observeReminders(): Flow<List<ReminderEntity>> = dao.observeReminders()

    suspend fun addProject(
        title: String,
        type: String,
        description: String = "",
        tags: String = "",
    ): ProjectEntity {
        require(title.isNotBlank()) { "项目名称不能为空" }
        val now = nowMillis()
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
        val now = nowMillis()
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

    suspend fun completeInitialLearning(taskId: String, completedDate: LocalDate? = null) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            if (task.hasLearned) return@withTransaction
            val date = completedDate ?: today()
            val state = scheduler.completeInitialLearning(date)
            dao.updateTask(
                task.copy(
                    hasLearned = true,
                    initialLearningDate = date.toString(),
                    stage = state.stage,
                    nextReviewDate = state.nextReviewDate?.toString(),
                    snoozedUntil = null,
                    updatedAt = nowMillis(),
                ),
            )
        }
    }

    suspend fun reviewTask(
        taskId: String,
        rating: RecallRating,
        completedDate: LocalDate? = null,
        snoozeUntil: LocalDate? = null,
    ) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            val date = completedDate ?: today()
            val decision = scheduler.review(
                state = ReviewState(task.stage, task.nextReviewDate?.let(LocalDate::parse)),
                rating = rating,
                completedDate = date,
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
                    updatedAt = nowMillis(),
                ),
            )
            decision.log?.let { log ->
                dao.insertReviewLog(
                    ReviewLogEntity(
                        id = newId(), taskId = task.id, rating = log.rating.name,
                        reviewedOn = log.reviewedOn.toString(),
                        previousStage = log.previousStage, nextStage = log.nextStage,
                        nextReviewDate = log.nextReviewDate.toString(),
                        createdAt = nowMillis(),
                    ),
                )
            }
        }
    }

    /** Adds a correction event and moves only the current projection; old review logs stay immutable. */
    suspend fun correctReview(
        taskId: String,
        correctedStage: Int,
        correctedNextReviewDate: LocalDate,
        reason: String = "",
    ) = database.withTransaction {
        require(correctedStage in 0..7) { "复习阶段无效" }
        val task = dao.getTask(taskId) ?: error("学习任务不存在")
        val now = nowMillis()
        dao.updateTask(
            task.copy(
                hasLearned = true,
                stage = correctedStage,
                nextReviewDate = correctedNextReviewDate.toString(),
                snoozedUntil = null,
                updatedAt = now,
            ),
        )
        dao.insertReviewCorrection(
            ReviewCorrectionEntity(
                id = newId(), taskId = taskId, correctedStage = correctedStage,
                correctedNextReviewDate = correctedNextReviewDate.toString(), reason = reason.trim(), createdAt = now,
            ),
        )
    }

    suspend fun snoozeTask(taskId: String, until: LocalDate? = null) {
        database.withTransaction {
            val task = dao.getTask(taskId) ?: return@withTransaction
            val date = until ?: today().plusDays(1)
            dao.updateTask(task.copy(snoozedUntil = date.toString(), updatedAt = nowMillis()))
        }
    }

    suspend fun addReadingPlan(
        projectId: String,
        title: String,
        totalPages: Int,
        dailyTarget: Int,
        startDate: LocalDate? = null,
        deadline: LocalDate? = null,
    ): ReadingPlanEntity {
        require(title.isNotBlank()) { "阅读计划名称不能为空" }
        require(dao.getProject(projectId) != null) { "学习项目不存在" }
        require(totalPages > 0)
        require(dailyTarget > 0)
        val effectiveStartDate = startDate ?: today()
        require(deadline == null || !deadline.isBefore(effectiveStartDate)) { "阅读截止日不能早于开始日" }
        val now = nowMillis()
        val plan = ReadingPlanEntity(
            id = newId(), projectId = projectId, title = title.trim(),
            totalPages = totalPages, dailyTarget = dailyTarget, currentPage = 0,
            startDate = effectiveStartDate.toString(), deadline = deadline?.toString(),
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
                    createdAt = nowMillis(),
                ),
            )
            dao.updateReadingPlan(plan.copy(currentPage = nextPage, updatedAt = nowMillis()))
        }
    }

    /** Stores a signed adjustment instead of rewriting any original page log. */
    suspend fun adjustReading(
        planId: String,
        date: LocalDate,
        deltaPages: Int,
        reason: String = "",
    ) = database.withTransaction {
        require(deltaPages != 0) { "阅读调整不能为 0 页" }
        val plan = dao.getReadingPlan(planId) ?: error("阅读计划不存在")
        val nextPage = (plan.currentPage + deltaPages).coerceIn(0, plan.totalPages)
        val actualDelta = nextPage - plan.currentPage
        require(actualDelta != 0) { "调整超出阅读页数范围" }
        val now = nowMillis()
        dao.insertReadingAdjustment(
            ReadingAdjustmentEntity(newId(), planId, date.toString(), actualDelta, reason.trim(), now),
        )
        dao.updateReadingPlan(plan.copy(currentPage = nextPage, updatedAt = now))
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
                updatedAt = nowMillis(),
            ),
        )
    }

    /** Generates dated targets for the remaining pages through the plan deadline. */
    suspend fun rebalanceReadingPlan(planId: String, from: LocalDate? = null) {
        database.withTransaction {
            val plan = dao.getReadingPlan(planId) ?: return@withTransaction
            val deadline = plan.deadline?.let(LocalDate::parse) ?: error("请先为阅读计划设置截止日")
            val start = maxOf(from ?: today(), LocalDate.parse(plan.startDate))
            val targets = readingPlanService.rebalance(plan.currentPage, plan.totalPages, start, deadline)
            dao.insertReadingTargets(
                targets.map { target ->
                    ReadingTargetEntity(
                        id = "$planId:${target.date}", planId = planId,
                        localDate = target.date.toString(), targetPages = target.pages,
                        updatedAt = nowMillis(),
                    )
                },
            )
            targets.firstOrNull()?.let { first ->
                dao.updateReadingPlan(plan.copy(dailyTarget = first.pages.coerceAtLeast(1), updatedAt = nowMillis()))
            }
        }
    }

    suspend fun addTodo(
        title: String,
        notes: String = "",
        isRequired: Boolean = true,
        repeatRule: String = "ONCE",
        customRepeatDays: String = "",
        dueDate: LocalDate? = null,
        projectId: String? = null,
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
        val effectiveDueDate = dueDate ?: today()
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        val now = nowMillis()
        val todo = TodoEntity(
            id = newId(), title = title.trim(), notes = notes.trim(),
            isRequired = isRequired, repeatRule = normalizedRule,
            customRepeatDays = normalizedCustomDays, dueDate = effectiveDueDate.toString(),
            completedDates = "", isArchived = false, createdAt = now, updatedAt = now,
            projectId = projectId,
        )
        dao.insertTodo(todo)
        return todo
    }

    /** Creates a one-time occurrence for today while keeping the recurring template unchanged. */
    suspend fun createTodoInstanceForToday(todoId: String, date: LocalDate? = null): TodoEntity = database.withTransaction {
        val source = dao.getTodo(todoId) ?: error("待办不存在")
        require(source.repeatRule != TodoRepeatRule.ONCE.name) { "一次性待办不需要补做实例" }
        val occurrenceDate = date ?: today()
        val now = nowMillis()
        TodoEntity(
            id = newId(),
            title = source.title,
            notes = source.notes,
            isRequired = source.isRequired,
            repeatRule = TodoRepeatRule.ONCE.name,
            customRepeatDays = "",
            dueDate = occurrenceDate.toString(),
            completedDates = "",
            isArchived = false,
            createdAt = now,
            updatedAt = now,
            projectId = source.projectId,
            recurrenceSourceId = source.id,
            missedPromptPolicy = "NEVER",
        ).also { dao.insertTodo(it) }
    }

    suspend fun setTodoMissedPromptPolicy(todoId: String, policy: String): TodoEntity = database.withTransaction {
        require(policy in setOf("ASK", "NEVER")) { "漏做提示策略无效" }
        val current = dao.getTodo(todoId) ?: error("待办不存在")
        current.copy(missedPromptPolicy = policy, updatedAt = nowMillis()).also { dao.updateTodo(it) }
    }

    suspend fun completeTodo(todoId: String, date: LocalDate? = null) {
        setTodoCompleted(todoId, date, completed = true)
    }

    suspend fun setTodoCompleted(todoId: String, date: LocalDate? = null, completed: Boolean) {
        val todo = dao.getTodo(todoId) ?: return
        dao.updateTodo(
            todo.copy(
                completedDates = TodoCompletion.setCompleted(todo.completedDates, date ?: today(), completed),
                updatedAt = nowMillis(),
            ),
        )
    }

    suspend fun addFocusSession(
        plannedMinutes: Int,
        actualMinutes: Int = plannedMinutes,
        projectId: String? = null,
        taskId: String? = null,
        startedAt: Long? = null,
        actualSeconds: Int = actualMinutes * 60,
        phase: String = "WORK",
        round: Int = 1,
    ): FocusSessionEntity = database.withTransaction {
        require(plannedMinutes > 0)
        val effectiveStartedAt = startedAt ?: (nowMillis() - actualSeconds * 1000L)
        val session = FocusSessionEntity(
            id = newId(), projectId = projectId, taskId = taskId,
            startedAt = effectiveStartedAt, endedAt = nowMillis(),
            plannedMinutes = plannedMinutes, actualMinutes = (actualSeconds / 60).coerceAtLeast(0),
            status = "COMPLETED", actualSeconds = actualSeconds.coerceAtLeast(0), phase = phase, round = round.coerceIn(1, 4),
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
        actualSeconds: Int = actualMinutes * 60,
        phase: String = "WORK",
        round: Int = 1,
    ): FocusSessionEntity = database.withTransaction {
        dao.getFocusSessionByStartedAt(startedAt) ?: run {
            require(plannedMinutes > 0)
            FocusSessionEntity(
                id = newId(), projectId = projectId, taskId = taskId,
                startedAt = startedAt, endedAt = nowMillis(),
                plannedMinutes = plannedMinutes, actualMinutes = (actualSeconds / 60).coerceAtLeast(0),
                status = "COMPLETED", actualSeconds = actualSeconds.coerceAtLeast(0), phase = phase, round = round.coerceIn(1, 4),
            ).also { dao.insertFocusSession(it) }
        }
    }

    suspend fun addGoal(
        title: String,
        metric: String,
        targetValue: Int,
        period: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        projectId: String? = null,
    ): GoalEntity {
        require(title.isNotBlank()) { "目标名称不能为空" }
        require(metric in setOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE")) { "目标指标无效" }
        require(period in setOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM")) { "目标周期无效" }
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        require(targetValue > 0)
        val effectiveStartDate = startDate ?: today()
        require(endDate == null || !endDate.isBefore(effectiveStartDate)) { "目标截止日不能早于开始日" }
        val now = nowMillis()
        val goal = GoalEntity(
            id = newId(), title = title.trim(), metric = metric,
            targetValue = targetValue, period = period, startDate = effectiveStartDate.toString(),
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
        val now = nowMillis()
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
            dao.updateCountdown(it.copy(isCompleted = true, updatedAt = nowMillis()))
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
        val now = nowMillis()
        val reminder = ReminderEntity(
            id = newId(), projectId = projectId, kind = kind,
            timeMinutes = timeMinutes, repeatDays = normalizedDays, enabled = true,
            quietStartMinutes = quietStartMinutes, quietEndMinutes = quietEndMinutes,
            createdAt = now, updatedAt = now,
        )
        dao.insertReminder(reminder)
        return reminder
    }

    suspend fun updateProject(
        projectId: String,
        title: String,
        type: String,
        description: String = "",
        tags: String = "",
        colorHex: String? = null,
    ): ProjectEntity = database.withTransaction {
        require(title.isNotBlank()) { "项目名称不能为空" }
        val current = dao.getProject(projectId) ?: error("学习项目不存在")
        current.copy(
            title = title.trim(), type = type.trim().ifBlank { current.type },
            description = description.trim(), tagCsv = tags.trim(),
            colorHex = colorHex?.trim()?.takeIf(String::isNotBlank) ?: current.colorHex,
            updatedAt = nowMillis(),
        ).also { dao.updateProject(it) }
    }

    /** Edit only content fields so review stage and schedule remain unchanged. */
    suspend fun updateTask(
        taskId: String,
        title: String,
        prompt: String = "",
        notes: String = "",
        source: String = "",
        isRequired: Boolean,
    ): LearningTaskEntity = database.withTransaction {
        require(title.isNotBlank()) { "学习任务名称不能为空" }
        val current = dao.getTask(taskId) ?: error("学习任务不存在")
        current.copy(
            title = title.trim(), prompt = prompt.trim(), notes = notes.trim(), source = source.trim(),
            isRequired = isRequired, updatedAt = nowMillis(),
        ).also { dao.updateTask(it) }
    }

    suspend fun updateReadingPlan(
        planId: String,
        title: String,
        totalPages: Int,
        dailyTarget: Int,
        deadline: LocalDate?,
    ): ReadingPlanEntity = database.withTransaction {
        require(title.isNotBlank()) { "阅读计划名称不能为空" }
        require(totalPages > 0 && dailyTarget > 0) { "页数和每日目标需要大于 0" }
        val current = dao.getReadingPlan(planId) ?: error("阅读计划不存在")
        require(totalPages >= current.currentPage) { "总页数不能小于已读页数" }
        val startDate = LocalDate.parse(current.startDate)
        require(deadline == null || !deadline.isBefore(startDate)) { "阅读截止日不能早于开始日" }
        current.copy(
            title = title.trim(), totalPages = totalPages, dailyTarget = dailyTarget,
            deadline = deadline?.toString(), updatedAt = nowMillis(),
        ).also { dao.updateReadingPlan(it) }
    }

    suspend fun updateTodo(
        todoId: String,
        title: String,
        notes: String,
        isRequired: Boolean,
        repeatRule: String,
        customRepeatDays: String,
        dueDate: LocalDate?,
        projectId: String?,
    ): TodoEntity = database.withTransaction {
        require(title.isNotBlank()) { "待办内容不能为空" }
        val normalizedRule = runCatching { TodoRepeatRule.valueOf(repeatRule) }.getOrNull()?.name
            ?: error("重复规则无效")
        val normalizedDays = customRepeatDays.split(',').mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }
            .distinct().sorted().joinToString(",")
        require(normalizedRule != TodoRepeatRule.CUSTOM.name || normalizedDays.isNotBlank()) { "自定义待办至少选择一天" }
        require(normalizedRule != TodoRepeatRule.ONCE.name || dueDate != null) { "一次性待办需要设置日期" }
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        val current = dao.getTodo(todoId) ?: error("待办不存在")
        current.copy(
            title = title.trim(), notes = notes.trim(), isRequired = isRequired,
            repeatRule = normalizedRule, customRepeatDays = normalizedDays, dueDate = dueDate?.toString(),
            projectId = projectId, updatedAt = nowMillis(),
        ).also { dao.updateTodo(it) }
    }

    suspend fun updateGoal(
        goalId: String,
        title: String,
        metric: String,
        targetValue: Int,
        period: String,
        endDate: LocalDate?,
        projectId: String?,
    ): GoalEntity = database.withTransaction {
        require(title.isNotBlank()) { "目标名称不能为空" }
        require(metric in setOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE")) { "目标指标无效" }
        require(period in setOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM")) { "目标周期无效" }
        require(targetValue > 0) { "目标值需要大于 0" }
        require(projectId == null || dao.getProject(projectId) != null) { "学习项目不存在" }
        val current = dao.getAllGoals().firstOrNull { it.id == goalId } ?: error("目标不存在")
        val startDate = LocalDate.parse(current.startDate)
        require(endDate == null || !endDate.isBefore(startDate)) { "目标截止日不能早于开始日" }
        current.copy(
            title = title.trim(), metric = metric, targetValue = targetValue, period = period,
            endDate = endDate?.toString(), projectId = projectId, updatedAt = nowMillis(),
        ).also { dao.updateGoal(it) }
    }

    suspend fun updateCountdown(
        id: String,
        title: String,
        eventAtEpochMillis: Long,
        note: String,
        reminderMinutesBefore: Int?,
    ): CountdownEntity = database.withTransaction {
        require(title.isNotBlank()) { "倒计时名称不能为空" }
        require(eventAtEpochMillis > 0) { "事件时间无效" }
        require(reminderMinutesBefore == null || reminderMinutesBefore in 0..43_200) { "提前提醒时间无效" }
        val current = dao.getAllCountdowns().firstOrNull { it.id == id } ?: error("倒计时不存在")
        current.copy(
            title = title.trim(), eventAtEpochMillis = eventAtEpochMillis, note = note.trim(),
            reminderMinutesBefore = reminderMinutesBefore, updatedAt = nowMillis(),
        ).also { dao.updateCountdown(it) }
    }

    suspend fun softDeleteProject(id: String) = database.withTransaction {
        val now = nowMillis()
        dao.getProject(id)?.let { dao.updateProject(it.copy(deletedAt = now, updatedAt = now)) }
    }

    suspend fun softDeleteTask(id: String) = markTaskDeleted(id, true)
    suspend fun softDeleteReadingPlan(id: String) = markReadingPlanDeleted(id, true)
    suspend fun softDeleteTodo(id: String) = markTodoDeleted(id, true)
    suspend fun softDeleteGoal(id: String) = markGoalDeleted(id, true)
    suspend fun softDeleteCountdown(id: String) = markCountdownDeleted(id, true)

    suspend fun restoreProject(id: String) = markProjectDeleted(id, false)
    suspend fun restoreTask(id: String) = markTaskDeleted(id, false)
    suspend fun restoreReadingPlan(id: String) = markReadingPlanDeleted(id, false)
    suspend fun restoreTodo(id: String) = markTodoDeleted(id, false)
    suspend fun restoreGoal(id: String) = markGoalDeleted(id, false)
    suspend fun restoreCountdown(id: String) = markCountdownDeleted(id, false)

    suspend fun permanentlyDeleteProject(id: String) = database.withTransaction {
        dao.getAllTasks().filter { it.projectId == id }.forEach { task ->
            dao.deleteReviewLogsForTask(task.id)
            dao.deleteReviewCorrectionsForTask(task.id)
            dao.deleteFocusSessionsForTask(task.id)
            dao.deleteTask(task.id)
        }
        dao.getAllReadingPlans().filter { it.projectId == id }.forEach { plan ->
            dao.deleteReadingTargetsForPlan(plan.id)
            dao.deletePageLogsForPlan(plan.id)
            dao.deleteReadingAdjustmentsForPlan(plan.id)
            dao.deleteReadingPlan(plan.id)
        }
        dao.deleteFocusSessionsForProject(id)
        dao.deleteTodosForProject(id)
        dao.deleteGoalsForProject(id)
        dao.deleteRemindersForProject(id)
        dao.deleteProject(id)
    }

    suspend fun permanentlyDeleteTask(id: String) = database.withTransaction { dao.deleteReviewLogsForTask(id); dao.deleteReviewCorrectionsForTask(id); dao.deleteFocusSessionsForTask(id); dao.deleteTask(id) }
    suspend fun permanentlyDeleteReadingPlan(id: String) = database.withTransaction { dao.deleteReadingTargetsForPlan(id); dao.deletePageLogsForPlan(id); dao.deleteReadingAdjustmentsForPlan(id); dao.deleteReadingPlan(id) }
    suspend fun permanentlyDeleteTodo(id: String) = database.withTransaction { dao.deleteTodoInstancesForSource(id); dao.deleteTodo(id) }
    suspend fun permanentlyDeleteGoal(id: String) = dao.deleteGoal(id)
    suspend fun permanentlyDeleteCountdown(id: String) = dao.deleteCountdown(id)

    private suspend fun markProjectDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getProject(id)?.let { dao.updateProject(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }
    private suspend fun markTaskDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getTask(id)?.let { dao.updateTask(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }
    private suspend fun markReadingPlanDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getReadingPlan(id)?.let { dao.updateReadingPlan(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }
    private suspend fun markTodoDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getTodo(id)?.let { dao.updateTodo(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }
    private suspend fun markGoalDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getAllGoals().firstOrNull { it.id == id }?.let { dao.updateGoal(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }
    private suspend fun markCountdownDeleted(id: String, deleted: Boolean) {
        val now = nowMillis()
        dao.getAllCountdowns().firstOrNull { it.id == id }?.let { dao.updateCountdown(it.copy(deletedAt = if (deleted) now else null, updatedAt = now)) }
    }

    suspend fun archiveProject(projectId: String, archived: Boolean = true) {
        dao.getProject(projectId)?.let { dao.updateProject(it.copy(isArchived = archived, updatedAt = nowMillis())) }
    }

    suspend fun setProjectPaused(projectId: String, paused: Boolean) {
        dao.getProject(projectId)?.let { dao.updateProject(it.copy(isPaused = paused, updatedAt = nowMillis())) }
    }

    suspend fun archiveTask(taskId: String, archived: Boolean = true) {
        dao.getTask(taskId)?.let { dao.updateTask(it.copy(isArchived = archived, updatedAt = nowMillis())) }
    }

    suspend fun setReminderEnabled(reminderId: String, enabled: Boolean) {
        dao.getAllReminders().firstOrNull { it.id == reminderId }?.let {
            dao.updateReminder(it.copy(enabled = enabled, updatedAt = nowMillis()))
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        dao.deleteReminder(reminderId)
    }

    suspend fun snapshot(): BackupSnapshot = BackupSnapshot(
        projects = dao.getAllProjects(), tasks = dao.getAllTasks(),
        reviewLogs = dao.getAllReviewLogs(), reviewCorrections = dao.getAllReviewCorrections(), readingPlans = dao.getAllReadingPlans(),
        readingTargets = dao.getAllReadingTargets(),
        pageLogs = dao.getAllPageLogs(), readingAdjustments = dao.getAllReadingAdjustments(), todos = dao.getAllTodos(),
        focusSessions = dao.getAllFocusSessions(), goals = dao.getAllGoals(),
        countdowns = dao.getAllCountdowns(), reminders = dao.getAllReminders(),
    )

    suspend fun replaceAll(snapshot: BackupSnapshot) {
        database.withTransaction {
            dao.clearReminders(); dao.clearCountdowns(); dao.clearGoals();
            dao.clearFocusSessions(); dao.clearTodos(); dao.clearPageLogs();
            dao.clearReadingAdjustments(); dao.clearReadingTargets(); dao.clearReadingPlans(); dao.clearReviewCorrections(); dao.clearReviewLogs(); dao.clearTasks(); dao.clearProjects()
            dao.insertProjects(snapshot.projects); dao.insertTasks(snapshot.tasks)
            dao.insertReviewLogs(snapshot.reviewLogs); dao.insertReviewCorrections(snapshot.reviewCorrections); dao.insertReadingPlans(snapshot.readingPlans); dao.insertReadingTargets(snapshot.readingTargets)
            dao.insertPageLogs(snapshot.pageLogs); dao.insertReadingAdjustments(snapshot.readingAdjustments); dao.insertTodos(snapshot.todos)
            dao.insertFocusSessions(snapshot.focusSessions); dao.insertGoals(snapshot.goals)
            dao.insertCountdowns(snapshot.countdowns); dao.insertReminders(snapshot.reminders)
        }
    }

    suspend fun merge(snapshot: BackupSnapshot) {
        val local = snapshot()
        val projects = mergeLatest(local.projects, snapshot.projects, ProjectEntity::id, ProjectEntity::updatedAt)
        val tasks = mergeLatest(local.tasks, snapshot.tasks, LearningTaskEntity::id, LearningTaskEntity::updatedAt)
        val plans = mergeLatest(local.readingPlans, snapshot.readingPlans, ReadingPlanEntity::id, ReadingPlanEntity::updatedAt)
        val targets = mergeLatest(local.readingTargets, snapshot.readingTargets, ReadingTargetEntity::id, ReadingTargetEntity::updatedAt)
        val todos = mergeLatest(local.todos, snapshot.todos, TodoEntity::id, TodoEntity::updatedAt)
        val goals = mergeLatest(local.goals, snapshot.goals, GoalEntity::id, GoalEntity::updatedAt)
        val countdowns = mergeLatest(local.countdowns, snapshot.countdowns, CountdownEntity::id, CountdownEntity::updatedAt)
        val reminders = mergeLatest(local.reminders, snapshot.reminders, ReminderEntity::id, ReminderEntity::updatedAt)
        val reviewLogs = mergeUnique(local.reviewLogs, snapshot.reviewLogs, ReviewLogEntity::id)
        val reviewCorrections = mergeUnique(local.reviewCorrections, snapshot.reviewCorrections, ReviewCorrectionEntity::id)
        val pageLogs = mergeUnique(local.pageLogs, snapshot.pageLogs, PageLogEntity::id)
        val readingAdjustments = mergeUnique(local.readingAdjustments, snapshot.readingAdjustments, ReadingAdjustmentEntity::id)
        val focusSessions = mergeUnique(local.focusSessions, snapshot.focusSessions, FocusSessionEntity::id)
        database.withTransaction {
            dao.insertProjects(projects); dao.insertTasks(tasks)
            dao.insertReviewLogs(reviewLogs); dao.insertReviewCorrections(reviewCorrections); dao.insertReadingPlans(plans); dao.insertReadingTargets(targets)
            dao.insertPageLogs(pageLogs); dao.insertReadingAdjustments(readingAdjustments); dao.insertTodos(todos)
            dao.insertFocusSessions(focusSessions); dao.insertGoals(goals)
            dao.insertCountdowns(countdowns); dao.insertReminders(reminders)
        }
    }

    private fun <T> mergeLatest(
        local: List<T>,
        incoming: List<T>,
        id: (T) -> String,
        updatedAt: (T) -> Long,
    ): List<T> {
        val result = local.associateBy(id).toMutableMap()
        incoming.forEach { item ->
            val existing = result[id(item)]
            if (existing == null || updatedAt(item) >= updatedAt(existing)) result[id(item)] = item
        }
        return result.values.toList()
    }

    private fun <T> mergeUnique(local: List<T>, incoming: List<T>, id: (T) -> String): List<T> =
        (local + incoming).distinctBy(id)

    private fun newId(): String = UUID.randomUUID().toString()
}

data class BackupSnapshot(
    val projects: List<ProjectEntity>,
    val tasks: List<LearningTaskEntity>,
    val reviewLogs: List<ReviewLogEntity>,
    val reviewCorrections: List<ReviewCorrectionEntity> = emptyList(),
    val readingPlans: List<ReadingPlanEntity>,
    val readingTargets: List<ReadingTargetEntity> = emptyList(),
    val pageLogs: List<PageLogEntity>,
    val readingAdjustments: List<ReadingAdjustmentEntity> = emptyList(),
    val todos: List<TodoEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val goals: List<GoalEntity>,
    val countdowns: List<CountdownEntity>,
    val reminders: List<ReminderEntity>,
)
