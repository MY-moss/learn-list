package com.mymoss.learnlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mymoss.learnlist.data.AppSettings
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.data.local.CountdownEntity
import com.mymoss.learnlist.data.local.FocusSessionEntity
import com.mymoss.learnlist.data.local.GoalEntity
import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReadingPlanEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.ReadingAdjustmentEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.FocusPhaseType
import com.mymoss.learnlist.domain.PomodoroCycle
import com.mymoss.learnlist.domain.PomodoroPhase
import com.mymoss.learnlist.system.FocusTimerScheduler
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LearnListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val archivedProjects: List<ProjectEntity> = emptyList(),
    val deletedProjects: List<ProjectEntity> = emptyList(),
    val tasks: List<LearningTaskEntity> = emptyList(),
    val deletedTasks: List<LearningTaskEntity> = emptyList(),
    val reviewLogs: List<ReviewLogEntity> = emptyList(),
    val readingPlans: List<ReadingPlanEntity> = emptyList(),
    val deletedReadingPlans: List<ReadingPlanEntity> = emptyList(),
    val readingTargets: List<ReadingTargetEntity> = emptyList(),
    val pageLogs: List<PageLogEntity> = emptyList(),
    val readingAdjustments: List<ReadingAdjustmentEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val deletedTodos: List<TodoEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val deletedGoals: List<GoalEntity> = emptyList(),
    val countdowns: List<CountdownEntity> = emptyList(),
    val deletedCountdowns: List<CountdownEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val restDays: Set<DayOfWeek> = emptySet(),
    val focusRunning: Boolean = false,
    val focusPaused: Boolean = false,
    val focusRemainingSeconds: Int = 0,
    val focusPlannedMinutes: Int = 25,
    val focusPhase: String = "WORK",
    val focusRound: Int = 1,
    val focusProjectId: String? = null,
    val focusTaskId: String? = null,
    val reviewBatchSize: Int = 20,
    val message: String? = null,
)

class LearnListViewModel(
    private val repository: LearnListRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val focusTimerScheduler: FocusTimerScheduler? = null,
    private val onFocusStarted: (startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String, round: Int) -> Unit = { _, _, _, _, _ -> },
    private val onFocusStopped: () -> Unit = {},
    private val onFocusCompleted: (AppSettings) -> Unit = {},
    private val onFocusPaused: () -> Unit = {},
    private val onFocusSkipped: () -> Unit = {},
    private val clock: java.time.Clock = java.time.Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(LearnListUiState())
    val state: StateFlow<LearnListUiState> = _state.asStateFlow()
    private var focusJob: Job? = null
    private var focusStartedAt: Long = 0L
    private var focusEndAt: Long = 0L
    private var focusSessionStartedAt: Long = 0L
    private var focusAccumulatedSeconds: Int = 0
    private var lastArchivedProjectId: String? = null

    private fun today(): LocalDate = LocalDate.now(clock)

    init {
        observe(repository.observeProjects()) { copy(projects = it) }
        observe(repository.observeArchivedProjects()) { copy(archivedProjects = it) }
        observe(repository.observeDeletedProjects()) { copy(deletedProjects = it) }
        observe(repository.observeTasks()) { copy(tasks = it) }
        observe(repository.observeDeletedTasks()) { copy(deletedTasks = it) }
        observe(repository.observeReviewLogs()) { copy(reviewLogs = it) }
        observe(repository.observeReadingPlans()) { copy(readingPlans = it) }
        observe(repository.observeDeletedReadingPlans()) { copy(deletedReadingPlans = it) }
        observe(repository.observeReadingTargets()) { copy(readingTargets = it) }
        observe(repository.observePageLogs()) { copy(pageLogs = it) }
        observe(repository.observeReadingAdjustments()) { copy(readingAdjustments = it) }
        observe(repository.observeTodos()) { copy(todos = it) }
        observe(repository.observeDeletedTodos()) { copy(deletedTodos = it) }
        observe(repository.observeFocusSessions()) { copy(focusSessions = it) }
        observe(repository.observeGoals()) { copy(goals = it) }
        observe(repository.observeDeletedGoals()) { copy(deletedGoals = it) }
        observe(repository.observeCountdowns()) { copy(countdowns = it) }
        observe(repository.observeDeletedCountdowns()) { copy(deletedCountdowns = it) }
        observe(repository.observeReminders()) { copy(reminders = it) }
        settingsRepository?.let { settings ->
            observe(settings.settings) { value ->
                val startedAt = value.focusStartedAtEpochMillis
                val endAt = value.focusEndAtEpochMillis
                if (value.focusPaused && value.focusRemainingSeconds > 0) {
                    copy(
                        restDays = value.restDaysCsv.toDayOfWeekSet(),
                        reviewBatchSize = value.reviewLimit.coerceIn(1, 1000),
                        focusRunning = false,
                        focusPaused = true,
                        focusRemainingSeconds = value.focusRemainingSeconds,
                        focusPlannedMinutes = value.focusPlannedMinutes.coerceIn(1, 180),
                        focusPhase = value.focusPhase,
                        focusRound = value.focusRound,
                        focusProjectId = value.focusProjectId,
                        focusTaskId = value.focusTaskId,
                    )
                } else if (startedAt != null && endAt != null && endAt > clock.millis()) {
                    copy(
                        restDays = value.restDaysCsv.toDayOfWeekSet(),
                        reviewBatchSize = value.reviewLimit.coerceIn(1, 1000),
                        focusRunning = true,
                        focusPaused = false,
                        focusRemainingSeconds = ((endAt - clock.millis()) / 1000L).toInt().coerceAtLeast(0),
                        focusPlannedMinutes = value.focusPlannedMinutes.coerceIn(1, 180),
                        focusPhase = value.focusPhase,
                        focusRound = value.focusRound,
                        focusProjectId = value.focusProjectId,
                        focusTaskId = value.focusTaskId,
                    )
                } else {
                    copy(
                        restDays = value.restDaysCsv.toDayOfWeekSet(),
                        reviewBatchSize = value.reviewLimit.coerceIn(1, 1000),
                        focusRunning = false,
                        focusPaused = false,
                        focusRemainingSeconds = value.focusRemainingSeconds,
                        focusPlannedMinutes = value.focusPlannedMinutes.coerceIn(1, 180),
                        focusPhase = value.focusPhase,
                        focusRound = value.focusRound,
                        focusProjectId = value.focusProjectId,
                        focusTaskId = value.focusTaskId,
                    )
                }
            }
        }
        recoverFocusTimer()
    }

    private fun <T> observe(flow: kotlinx.coroutines.flow.Flow<T>, update: LearnListUiState.(T) -> LearnListUiState) {
        viewModelScope.launch { flow.collect { value -> _state.update { it.update(value) } } }
    }

    fun addProject(title: String, type: String, description: String, tags: String) = action {
        require(title.isNotBlank()) { "请输入项目名称" }
        repository.addProject(title, type, description, tags)
        say("项目已创建")
    }

    fun addTask(projectId: String, title: String, prompt: String, notes: String, source: String, required: Boolean) = action {
        require(title.isNotBlank()) { "请输入学习任务名称" }
        repository.addTask(projectId, title, prompt, notes, source, required)
        say("学习任务已加入")
    }

    fun updateProject(project: ProjectEntity, title: String, type: String, description: String, tags: String) = action {
        repository.updateProject(project.id, title, type, description, tags, project.colorHex)
        say("项目已保存，复习进度保持不变")
    }

    fun updateTask(task: LearningTaskEntity, title: String, prompt: String, notes: String, source: String, required: Boolean) = action {
        repository.updateTask(task.id, title, prompt, notes, source, required)
        say("学习任务已保存，复习阶段保持不变")
    }

    fun initialLearn(taskId: String, completedDate: LocalDate? = null) = action {
        repository.completeInitialLearning(taskId, completedDate ?: today())
        say("已完成首次学习，明天开始复习")
    }

    fun review(taskId: String, rating: RecallRating, completedDate: LocalDate? = null) = action {
        repository.reviewTask(taskId, rating, completedDate = completedDate ?: today())
        say(if (rating == RecallRating.SNOOZE) "已安排稍后提醒" else "复习记录已保存")
    }

    fun correctReview(task: LearningTaskEntity, correctedStageText: String, nextReviewDateText: String, reason: String) = action {
        val correctedStage = correctedStageText.trim().toIntOrNull() ?: error("复习阶段需要是 0 到 7 的数字")
        val nextReviewDate = runCatching { LocalDate.parse(nextReviewDateText.trim()) }
            .getOrElse { error("下次复习日期需要使用 YYYY-MM-DD") }
        repository.correctReview(task.id, correctedStage, nextReviewDate, reason)
        say("复习纠正已追加，旧记录仍保留")
    }

    fun addReadingPlan(projectId: String, title: String, totalPagesText: String, dailyTargetText: String, deadlineText: String) = action {
        val total = totalPagesText.toIntOrNull() ?: error("总页数需要是数字")
        val target = dailyTargetText.toIntOrNull() ?: error("每日页数需要是数字")
        val deadline = deadlineText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.addReadingPlan(projectId, title, total, target, deadline = deadline)
        say("阅读计划已创建")
    }

    fun updateReadingPlan(plan: ReadingPlanEntity, title: String, totalPagesText: String, dailyTargetText: String, deadlineText: String) = action {
        val total = totalPagesText.toIntOrNull() ?: error("总页数需要是数字")
        val target = dailyTargetText.toIntOrNull() ?: error("每日页数需要是数字")
        val deadline = deadlineText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.updateReadingPlan(plan.id, title, total, target, deadline)
        say("阅读计划已保存")
    }

    fun rebalanceReading(planId: String) = action {
        repository.rebalanceReadingPlan(planId)
        say("已将剩余页数均摊到截止日")
    }

    fun adjustReadingTarget(planId: String, targetPages: Int, date: LocalDate? = null) = action {
        repository.setReadingTarget(planId, date ?: today(), targetPages)
        say("今日阅读目标已调整为 $targetPages 页")
    }

    fun adjustReading(plan: ReadingPlanEntity, deltaText: String, reason: String, date: LocalDate? = null) = action {
        val delta = deltaText.trim().toIntOrNull() ?: error("调整页数需要是数字，可填写负数")
        repository.adjustReading(plan.id, date ?: today(), delta, reason)
        say("阅读纠正已追加，原始阅读记录仍保留")
    }

    fun logReading(planId: String, pagesText: String, date: LocalDate? = null) = action {
        val pages = pagesText.toIntOrNull() ?: error("页数需要是数字")
        repository.logReading(planId, date ?: today(), pages)
        say("阅读进度已更新")
    }

    fun addTodo(
        title: String,
        notes: String,
        required: Boolean,
        repeatRule: String,
        customDays: String,
        dueDateText: String? = null,
        projectId: String? = null,
    ) = action {
        require(title.isNotBlank()) { "请输入待办内容" }
        val dueDate = dueDateText?.trim()?.takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.addTodo(title, notes, required, customRepeatDays = customDays, repeatRule = repeatRule, dueDate = dueDate, projectId = projectId)
        say("待办已添加")
    }

    fun updateTodo(todo: TodoEntity, title: String, notes: String, required: Boolean, repeatRule: String, customDays: String, dueDateText: String, projectId: String?) = action {
        val dueDate = dueDateText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.updateTodo(todo.id, title, notes, required, repeatRule, customDays, dueDate, projectId)
        say("待办已保存")
    }

    fun completeTodo(id: String) = action {
        repository.completeTodo(id)
        say("待办已完成")
    }

    fun createTodoInstanceForToday(id: String) = action {
        repository.createTodoInstanceForToday(id)
        say("已创建今天的补做实例")
    }

    fun setTodoMissedPromptPolicy(id: String, policy: String) = action {
        repository.setTodoMissedPromptPolicy(id, policy)
        if (policy == "NEVER") say("这条待办以后不再提示漏做")
    }

    fun toggleTodo(id: String, date: LocalDate, currentlyCompleted: Boolean) = action {
        repository.setTodoCompleted(id, date, completed = !currentlyCompleted)
        say(if (currentlyCompleted) "已撤销待办完成" else "待办已完成")
    }

    fun addGoal(title: String, metric: String, targetText: String, period: String, endDateText: String = "", projectId: String? = null) = action {
        val target = targetText.toIntOrNull() ?: error("目标值需要是数字")
        val endDate = endDateText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        require(period != "CUSTOM" || endDate != null) { "自定义目标需要填写截止日" }
        repository.addGoal(title, metric, target, period, endDate = endDate, projectId = projectId)
        say("目标已创建")
    }

    fun updateGoal(goal: GoalEntity, title: String, metric: String, targetText: String, period: String, endDateText: String, projectId: String?) = action {
        val target = targetText.toIntOrNull() ?: error("目标值需要是数字")
        val endDate = endDateText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.updateGoal(goal.id, title, metric, target, period, endDate, projectId)
        say("目标已保存")
    }

    fun addCountdown(title: String, dateText: String, timeText: String, note: String, reminderMinutesText: String = "") = action {
        val date = LocalDate.parse(dateText)
        val time = java.time.LocalTime.parse(timeText)
        val reminderMinutes = reminderMinutesText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
        require(reminderMinutesText.trim().isBlank() || reminderMinutes != null) { "提前提醒分钟需要是数字" }
        require(reminderMinutes == null || reminderMinutes >= 0) { "提前提醒分钟不能为负数" }
        val millis = date.atTime(time).atZone(clock.zone).toInstant().toEpochMilli()
        repository.addCountdown(title, millis, note, reminderMinutes)
        say("倒计时已创建")
    }

    fun completeCountdown(id: String) = action {
        repository.completeCountdown(id)
        say("倒计时已标记完成")
    }

    fun updateCountdown(countdown: CountdownEntity, title: String, dateText: String, timeText: String, note: String, reminderMinutesText: String) = action {
        val date = LocalDate.parse(dateText)
        val time = java.time.LocalTime.parse(timeText)
        val reminderMinutes = reminderMinutesText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
        require(reminderMinutesText.trim().isBlank() || reminderMinutes != null) { "提前提醒分钟需要是数字" }
        repository.updateCountdown(countdown.id, title, date.atTime(time).atZone(clock.zone).toInstant().toEpochMilli(), note, reminderMinutes)
        say("倒计时已保存")
    }

    fun addReminder(
        projectId: String?,
        kind: String,
        timeText: String,
        quietStartText: String = "22:00",
        quietEndText: String = "07:00",
        repeatDaysText: String = "1,2,3,4,5,6,7",
    ) = action {
        val time = java.time.LocalTime.parse(timeText)
        val quietStart = java.time.LocalTime.parse(quietStartText)
        val quietEnd = java.time.LocalTime.parse(quietEndText)
        val repeatDays = repeatDaysText.split(',')
            .mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }
            .distinct()
            .sorted()
            .joinToString(",")
        require(repeatDays.isNotBlank()) { "至少选择一天提醒" }
        repository.addReminder(
            projectId = projectId,
            kind = kind,
            timeMinutes = time.hour * 60 + time.minute,
            repeatDays = repeatDays,
            quietStartMinutes = quietStart.hour * 60 + quietStart.minute,
            quietEndMinutes = quietEnd.hour * 60 + quietEnd.minute,
        )
        say(if (kind == "SUMMARY") "每日进度提醒已添加" else "项目提醒已添加")
    }

    fun addSummaryReminder(timeText: String) = addReminder(null, "SUMMARY", timeText)

    fun setReminderEnabled(id: String, enabled: Boolean) = action {
        repository.setReminderEnabled(id, enabled)
        say(if (enabled) "提醒已启用" else "提醒已停用")
    }

    fun deleteReminder(id: String) = action {
        repository.deleteReminder(id)
        say("提醒已删除")
    }

    fun deleteProject(id: String) = action { repository.softDeleteProject(id); say("项目已移入回收站") }
    fun deleteTask(id: String) = action { repository.softDeleteTask(id); say("学习任务已移入回收站") }
    fun deleteReadingPlan(id: String) = action { repository.softDeleteReadingPlan(id); say("阅读计划已移入回收站") }
    fun deleteTodo(id: String) = action { repository.softDeleteTodo(id); say("待办已移入回收站") }
    fun deleteGoal(id: String) = action { repository.softDeleteGoal(id); say("目标已移入回收站") }
    fun deleteCountdown(id: String) = action { repository.softDeleteCountdown(id); say("倒计时已移入回收站") }
    fun restoreDeletedProject(id: String) = action { repository.restoreProject(id); say("项目已恢复") }
    fun restoreDeletedTask(id: String) = action { repository.restoreTask(id); say("学习任务已恢复") }
    fun restoreDeletedReadingPlan(id: String) = action { repository.restoreReadingPlan(id); say("阅读计划已恢复") }
    fun restoreDeletedTodo(id: String) = action { repository.restoreTodo(id); say("待办已恢复") }
    fun restoreDeletedGoal(id: String) = action { repository.restoreGoal(id); say("目标已恢复") }
    fun restoreDeletedCountdown(id: String) = action { repository.restoreCountdown(id); say("倒计时已恢复") }
    fun permanentlyDeleteProject(id: String) = action { repository.permanentlyDeleteProject(id); say("项目已永久删除") }
    fun permanentlyDeleteTask(id: String) = action { repository.permanentlyDeleteTask(id); say("学习任务已永久删除") }
    fun permanentlyDeleteReadingPlan(id: String) = action { repository.permanentlyDeleteReadingPlan(id); say("阅读计划已永久删除") }
    fun permanentlyDeleteTodo(id: String) = action { repository.permanentlyDeleteTodo(id); say("待办已永久删除") }
    fun permanentlyDeleteGoal(id: String) = action { repository.permanentlyDeleteGoal(id); say("目标已永久删除") }
    fun permanentlyDeleteCountdown(id: String) = action { repository.permanentlyDeleteCountdown(id); say("倒计时已永久删除") }

    fun setRestDays(days: Set<DayOfWeek>) = action {
        settingsRepository?.update { settings ->
            settings.copy(restDaysCsv = days.map(DayOfWeek::getValue).sorted().joinToString(","))
        }
        say(if (days.isEmpty()) "已取消休息日" else "休息日设置已保存")
    }

    fun setReviewBatchSize(size: Int) = action {
        val safeSize = size.coerceIn(1, 1000)
        settingsRepository?.update { it.copy(reviewLimit = safeSize) }
        say("复习建议批次已设为 $safeSize 项；逾期内容不会被隐藏")
    }

    fun startFocus(minutes: Int, projectId: String? = null, taskId: String? = null) {
        if (_state.value.focusRunning || _state.value.focusPaused) return
        val safeMinutes = minutes.coerceIn(1, 180)
        focusStartedAt = clock.millis()
        focusEndAt = focusStartedAt + safeMinutes * 60_000L
        focusSessionStartedAt = focusStartedAt
        focusAccumulatedSeconds = 0
        _state.update {
            it.copy(
                focusRunning = true,
                focusPaused = false,
                focusRemainingSeconds = safeMinutes * 60,
                focusPlannedMinutes = safeMinutes,
                focusPhase = FocusPhaseType.WORK.name,
                focusRound = 1,
                focusProjectId = projectId,
                focusTaskId = taskId,
                message = null,
            )
        }
        persistFocusTimer(safeMinutes, FocusPhaseType.WORK.name, 1)
        launchFocusTimer(safeMinutes)
    }

    /** Starts the pending Pomodoro phase without resetting its round or phase. */
    fun startCurrentFocusPhase() {
        if (_state.value.focusRunning || _state.value.focusPaused) return
        val phase = runCatching { FocusPhaseType.valueOf(_state.value.focusPhase) }.getOrDefault(FocusPhaseType.WORK)
        val totalSeconds = _state.value.focusRemainingSeconds.takeIf { it > 0 }
            ?: when (phase) {
                FocusPhaseType.WORK -> PomodoroCycle.WORK_SECONDS
                FocusPhaseType.SHORT_BREAK -> PomodoroCycle.SHORT_BREAK_SECONDS
                FocusPhaseType.LONG_BREAK -> PomodoroCycle.LONG_BREAK_SECONDS
            }
        val plannedMinutes = (totalSeconds / 60).coerceAtLeast(1)
        focusStartedAt = clock.millis()
        focusEndAt = focusStartedAt + totalSeconds * 1000L
        if (phase == FocusPhaseType.WORK) focusSessionStartedAt = focusStartedAt
        focusAccumulatedSeconds = 0
        _state.update {
            it.copy(
                focusRunning = true,
                focusPaused = false,
                focusRemainingSeconds = totalSeconds,
                focusPlannedMinutes = plannedMinutes,
                message = null,
            )
        }
        persistFocusTimer(plannedMinutes, phase.name, _state.value.focusRound)
        launchFocusTimer(plannedMinutes)
    }

    fun pauseFocus() {
        if (!_state.value.focusRunning) return
        focusJob?.cancel()
        val now = clock.millis()
        val elapsed = ((now - focusStartedAt) / 1000L).toInt().coerceAtLeast(0)
        focusAccumulatedSeconds += elapsed
        val remaining = ((focusEndAt - now) / 1000L).toInt().coerceAtLeast(0)
        _state.update { it.copy(focusRunning = false, focusPaused = true, focusRemainingSeconds = remaining, message = "专注已暂停") }
        runCatching { onFocusPaused() }
    }

    fun resumeFocus() {
        if (!_state.value.focusPaused || _state.value.focusRemainingSeconds <= 0) return
        val startedAt = clock.millis()
        val endAt = startedAt + _state.value.focusRemainingSeconds * 1000L
        focusStartedAt = startedAt
        focusEndAt = endAt
        _state.update { it.copy(focusRunning = true, focusPaused = false, message = null) }
        persistFocusTimer(_state.value.focusPlannedMinutes, _state.value.focusPhase, _state.value.focusRound, resetAccumulated = false)
        launchFocusTimer(_state.value.focusPlannedMinutes)
    }

    fun skipFocus() {
        if (!_state.value.focusRunning && !_state.value.focusPaused) return
        focusJob?.cancel()
        val phase = runCatching { FocusPhaseType.valueOf(_state.value.focusPhase) }.getOrDefault(FocusPhaseType.WORK)
        val next = PomodoroCycle.afterCompleted(PomodoroPhase(phase, _state.value.focusRound, _state.value.focusPlannedMinutes * 60))
        focusAccumulatedSeconds = 0
        _state.update {
            it.copy(
                focusRunning = false,
                focusPaused = false,
                focusRemainingSeconds = next.totalSeconds,
                focusPlannedMinutes = next.totalSeconds / 60,
                focusPhase = next.type.name,
                    focusRound = next.round,
                    focusProjectId = _state.value.focusProjectId,
                    focusTaskId = _state.value.focusTaskId,
                    message = "已跳过当前阶段",
            )
        }
        runCatching { onFocusSkipped() }
    }

    fun stopFocus() {
        if (!_state.value.focusRunning && !_state.value.focusPaused) return
        focusJob?.cancel()
        val now = clock.millis()
        val elapsed = if (_state.value.focusRunning) ((now - focusStartedAt) / 1000L).toInt().coerceAtLeast(0) else 0
        val planned = _state.value.focusPlannedMinutes
        val actualSeconds = (focusAccumulatedSeconds + elapsed).coerceIn(0, planned * 60)
        val startedAt = (focusSessionStartedAt.takeIf { it > 0L } ?: focusStartedAt.takeIf { it > 0L } ?: (now - actualSeconds * 1000L)).coerceAtLeast(0L)
        val phase = _state.value.focusPhase
        val round = _state.value.focusRound
        _state.update { it.copy(focusRunning = false, focusPaused = false, focusRemainingSeconds = 0, message = "专注已保存") }
        runCatching { onFocusStopped() }
        action {
            if (actualSeconds > 0 && phase == FocusPhaseType.WORK.name) {
                repository.recordFocusSessionIfNeeded(
                    plannedMinutes = planned,
                    actualMinutes = actualSeconds / 60,
                    actualSeconds = actualSeconds,
                    projectId = _state.value.focusProjectId,
                    taskId = _state.value.focusTaskId,
                    startedAt = startedAt,
                    phase = phase,
                    round = round,
                )
            }
            focusAccumulatedSeconds = 0
        }
    }

    fun archiveProject(id: String) = action {
        repository.archiveProject(id)
        lastArchivedProjectId = id
        say("项目已归档，可撤销或在设置中恢复")
    }

    fun undoLastArchive() {
        val id = lastArchivedProjectId ?: return
        action {
            repository.archiveProject(id, archived = false)
            lastArchivedProjectId = null
            say("已撤销归档")
        }
    }

    fun restoreProject(id: String) = action {
        repository.archiveProject(id, archived = false)
        say("项目已恢复")
    }

    fun setProjectPaused(id: String, paused: Boolean) = action {
        repository.setProjectPaused(id, paused)
        say(if (paused) "项目已暂停，今天不会计入必做进度" else "项目已恢复")
    }

    val canUndoArchive: Boolean get() = lastArchivedProjectId != null

    fun clearMessage() { _state.update { it.copy(message = null) } }

    private fun recoverFocusTimer() {
        val settings = settingsRepository ?: return
        viewModelScope.launch {
            val saved = settings.settings.first()
            val started = saved.focusStartedAtEpochMillis ?: return@launch
            val end = saved.focusEndAtEpochMillis ?: return@launch
            focusStartedAt = started
            focusEndAt = end
            focusSessionStartedAt = saved.focusSessionStartedAtEpochMillis ?: started
            focusAccumulatedSeconds = saved.focusAccumulatedSeconds
            val planned = saved.focusPlannedMinutes.coerceIn(1, 180)
            if (end <= clock.millis()) {
                finishFocus(planned, started, end)
                _state.update { it.copy(message = "后台专注已完成，记录已恢复") }
            } else {
                _state.update {
                    it.copy(
                        focusRunning = true,
                        focusRemainingSeconds = ((end - clock.millis()) / 1000L).toInt().coerceAtLeast(0),
                        focusPlannedMinutes = planned,
                        focusPhase = saved.focusPhase,
                        focusRound = saved.focusRound,
                        focusProjectId = saved.focusProjectId,
                        focusTaskId = saved.focusTaskId,
                    )
                }
                focusTimerScheduler?.schedule(end)
                runCatching { onFocusStarted(started, end, planned, saved.focusPhase, saved.focusRound) }
                launchFocusTimer(planned)
            }
        }
    }

    private fun launchFocusTimer(plannedMinutes: Int) {
        focusJob?.cancel()
        val startedAt = focusStartedAt
        val endAt = focusEndAt
        focusJob = viewModelScope.launch {
            while (isActive) {
                val remaining = ((endAt - clock.millis()) / 1000L).toInt()
                if (remaining <= 0) {
                    finishFocus(plannedMinutes, startedAt, endAt)
                    _state.update { it.copy(focusRunning = false, focusRemainingSeconds = 0, message = "专注完成，太棒了") }
                    break
                }
                _state.update { it.copy(focusRemainingSeconds = remaining) }
                delay(1000)
            }
        }
    }

    private fun persistFocusTimer(plannedMinutes: Int, phase: String = _state.value.focusPhase, round: Int = _state.value.focusRound, resetAccumulated: Boolean = true) {
        settingsRepository?.let { settings ->
            viewModelScope.launch {
                settings.update {
                    it.copy(
                        focusStartedAtEpochMillis = focusStartedAt,
                        focusEndAtEpochMillis = focusEndAt,
                        focusSessionStartedAtEpochMillis = if (phase == FocusPhaseType.WORK.name) {
                            it.focusSessionStartedAtEpochMillis ?: focusSessionStartedAt.takeIf { value -> value > 0L } ?: focusStartedAt
                        } else null,
                        focusPlannedMinutes = plannedMinutes,
                        focusRemainingSeconds = if (resetAccumulated) plannedMinutes * 60 else it.focusRemainingSeconds,
                        focusAccumulatedSeconds = if (resetAccumulated) 0 else it.focusAccumulatedSeconds,
                        focusPaused = false,
                        focusPhase = phase,
                        focusRound = round,
                        focusProjectId = _state.value.focusProjectId,
                        focusTaskId = _state.value.focusTaskId,
                    )
                }
                focusTimerScheduler?.schedule(focusEndAt)
                runCatching { onFocusStarted(focusStartedAt, focusEndAt, plannedMinutes, phase, round) }
            }
        }
    }

    private suspend fun clearPersistedFocusTimer() {
        focusTimerScheduler?.cancel()
        settingsRepository?.update {
            it.copy(
                focusStartedAtEpochMillis = null,
                focusEndAtEpochMillis = null,
                focusSessionStartedAtEpochMillis = null,
            )
        }
    }

    private suspend fun finishFocus(plannedMinutes: Int, startedAt: Long, endAt: Long): Boolean {
        val claimed = if (settingsRepository != null) {
            // The foreground service/alarm receiver owns durable completion when settings are available.
            true
        } else {
            repository.recordFocusSessionIfNeeded(
                plannedMinutes = plannedMinutes,
                actualMinutes = plannedMinutes,
                actualSeconds = plannedMinutes * 60,
                projectId = _state.value.focusProjectId,
                taskId = _state.value.focusTaskId,
                startedAt = startedAt,
            )
            true
        }
        if (claimed) {
            if (settingsRepository == null) {
                focusTimerScheduler?.cancel()
                runCatching { onFocusCompleted(AppSettings()) }
                runCatching { onFocusStopped() }
            }
        }
        return claimed
    }

    private fun action(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { error -> _state.update { it.copy(message = error.message ?: "操作失败") } }
        }
    }

    private fun say(message: String) { _state.update { it.copy(message = message) } }

    companion object {
        fun factory(
            repository: LearnListRepository,
            settingsRepository: SettingsRepository? = null,
            focusTimerScheduler: FocusTimerScheduler? = null,
            onFocusStarted: (startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String, round: Int) -> Unit = { _, _, _, _, _ -> },
            onFocusStopped: () -> Unit = {},
            onFocusCompleted: (AppSettings) -> Unit = {},
            onFocusPaused: () -> Unit = {},
            onFocusSkipped: () -> Unit = {},
            clock: java.time.Clock = java.time.Clock.systemDefaultZone(),
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LearnListViewModel(
                        repository,
                        settingsRepository,
                        focusTimerScheduler,
                        onFocusStarted,
                        onFocusStopped,
                        onFocusCompleted,
                        onFocusPaused,
                        onFocusSkipped,
                        clock,
                    ) as T
            }
    }
}

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun String.toDayOfWeekSet(): Set<DayOfWeek> = split(',').mapNotNull { token ->
    token.trim().toIntOrNull()?.let { runCatching { DayOfWeek.of(it) }.getOrNull() }
}.toSet()
