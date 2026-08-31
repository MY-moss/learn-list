package com.mymoss.learnlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.RecallRating
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
    val tasks: List<LearningTaskEntity> = emptyList(),
    val reviewLogs: List<ReviewLogEntity> = emptyList(),
    val readingPlans: List<ReadingPlanEntity> = emptyList(),
    val readingTargets: List<ReadingTargetEntity> = emptyList(),
    val pageLogs: List<PageLogEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val countdowns: List<CountdownEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val restDays: Set<DayOfWeek> = emptySet(),
    val focusRunning: Boolean = false,
    val focusRemainingSeconds: Int = 0,
    val focusPlannedMinutes: Int = 25,
    val message: String? = null,
)

class LearnListViewModel(
    private val repository: LearnListRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val focusTimerScheduler: FocusTimerScheduler? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(LearnListUiState())
    val state: StateFlow<LearnListUiState> = _state.asStateFlow()
    private var focusJob: Job? = null
    private var focusStartedAt: Long = 0L
    private var focusEndAt: Long = 0L
    private var lastArchivedProjectId: String? = null

    init {
        observe(repository.observeProjects()) { copy(projects = it) }
        observe(repository.observeArchivedProjects()) { copy(archivedProjects = it) }
        observe(repository.observeTasks()) { copy(tasks = it) }
        observe(repository.observeReviewLogs()) { copy(reviewLogs = it) }
        observe(repository.observeReadingPlans()) { copy(readingPlans = it) }
        observe(repository.observeReadingTargets()) { copy(readingTargets = it) }
        observe(repository.observePageLogs()) { copy(pageLogs = it) }
        observe(repository.observeTodos()) { copy(todos = it) }
        observe(repository.observeFocusSessions()) { copy(focusSessions = it) }
        observe(repository.observeGoals()) { copy(goals = it) }
        observe(repository.observeCountdowns()) { copy(countdowns = it) }
        observe(repository.observeReminders()) { copy(reminders = it) }
        settingsRepository?.let { settings ->
            observe(settings.settings) { copy(restDays = it.restDaysCsv.toDayOfWeekSet()) }
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

    fun initialLearn(taskId: String) = action {
        repository.completeInitialLearning(taskId)
        say("已完成首次学习，明天开始复习")
    }

    fun review(taskId: String, rating: RecallRating) = action {
        repository.reviewTask(taskId, rating, snoozeUntil = if (rating == RecallRating.SNOOZE) LocalDate.now().plusDays(1) else null)
        say(if (rating == RecallRating.SNOOZE) "已安排稍后提醒" else "复习记录已保存")
    }

    fun addReadingPlan(projectId: String, title: String, totalPagesText: String, dailyTargetText: String, deadlineText: String) = action {
        val total = totalPagesText.toIntOrNull() ?: error("总页数需要是数字")
        val target = dailyTargetText.toIntOrNull() ?: error("每日页数需要是数字")
        val deadline = deadlineText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.addReadingPlan(projectId, title, total, target, deadline = deadline)
        say("阅读计划已创建")
    }

    fun rebalanceReading(planId: String) = action {
        repository.rebalanceReadingPlan(planId)
        say("已将剩余页数均摊到截止日")
    }

    fun adjustReadingTarget(planId: String, targetPages: Int) = action {
        repository.setReadingTarget(planId, LocalDate.now(), targetPages)
        say("今日阅读目标已调整为 $targetPages 页")
    }

    fun logReading(planId: String, pagesText: String) = action {
        val pages = pagesText.toIntOrNull() ?: error("页数需要是数字")
        repository.logReading(planId, LocalDate.now(), pages)
        say("阅读进度已更新")
    }

    fun addTodo(
        title: String,
        notes: String,
        required: Boolean,
        repeatRule: String,
        customDays: String,
        dueDateText: String = LocalDate.now().toString(),
    ) = action {
        require(title.isNotBlank()) { "请输入待办内容" }
        val dueDate = dueDateText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        repository.addTodo(title, notes, required, repeatRule, customDays, dueDate = dueDate)
        say("待办已添加")
    }

    fun completeTodo(id: String) = action {
        repository.completeTodo(id)
        say("待办已完成")
    }

    fun addGoal(title: String, metric: String, targetText: String, period: String, endDateText: String = "") = action {
        val target = targetText.toIntOrNull() ?: error("目标值需要是数字")
        val endDate = endDateText.trim().takeIf(String::isNotBlank)?.let(LocalDate::parse)
        require(period != "CUSTOM" || endDate != null) { "自定义目标需要填写截止日" }
        repository.addGoal(title, metric, target, period, endDate = endDate)
        say("目标已创建")
    }

    fun addCountdown(title: String, dateText: String, timeText: String, note: String, reminderMinutesText: String = "") = action {
        val date = LocalDate.parse(dateText)
        val time = java.time.LocalTime.parse(timeText)
        val reminderMinutes = reminderMinutesText.trim().takeIf(String::isNotBlank)?.toIntOrNull()
        require(reminderMinutesText.trim().isBlank() || reminderMinutes != null) { "提前提醒分钟需要是数字" }
        require(reminderMinutes == null || reminderMinutes >= 0) { "提前提醒分钟不能为负数" }
        val millis = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repository.addCountdown(title, millis, note, reminderMinutes)
        say("倒计时已创建")
    }

    fun completeCountdown(id: String) = action {
        repository.completeCountdown(id)
        say("倒计时已标记完成")
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

    fun setRestDays(days: Set<DayOfWeek>) = action {
        settingsRepository?.update { settings ->
            settings.copy(restDaysCsv = days.map(DayOfWeek::getValue).sorted().joinToString(","))
        }
        say(if (days.isEmpty()) "已取消休息日" else "休息日设置已保存")
    }

    fun startFocus(minutes: Int) {
        if (_state.value.focusRunning) return
        val safeMinutes = minutes.coerceIn(1, 180)
        focusStartedAt = System.currentTimeMillis()
        focusEndAt = focusStartedAt + safeMinutes * 60_000L
        _state.update { it.copy(focusRunning = true, focusRemainingSeconds = safeMinutes * 60, focusPlannedMinutes = safeMinutes, message = null) }
        persistFocusTimer(safeMinutes)
        launchFocusTimer(safeMinutes)
    }

    fun stopFocus() {
        if (!_state.value.focusRunning) return
        focusJob?.cancel()
        val actual = ((System.currentTimeMillis() - focusStartedAt) / 60_000L).toInt().coerceAtLeast(0)
        val planned = _state.value.focusPlannedMinutes
        _state.update { it.copy(focusRunning = false, focusRemainingSeconds = 0, message = "专注已保存") }
        action {
            repository.recordFocusSessionIfNeeded(planned, actual, startedAt = focusStartedAt)
            clearPersistedFocusTimer()
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
            val planned = saved.focusPlannedMinutes.coerceIn(1, 180)
            if (end <= System.currentTimeMillis()) {
                repository.recordFocusSessionIfNeeded(planned, planned, startedAt = started)
                clearPersistedFocusTimer()
                _state.update { it.copy(message = "后台专注已完成，记录已恢复") }
            } else {
                _state.update {
                    it.copy(
                        focusRunning = true,
                        focusRemainingSeconds = ((end - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0),
                        focusPlannedMinutes = planned,
                    )
                }
                focusTimerScheduler?.schedule(end)
                launchFocusTimer(planned)
            }
        }
    }

    private fun launchFocusTimer(plannedMinutes: Int) {
        focusJob?.cancel()
        focusJob = viewModelScope.launch {
            while (isActive) {
                val remaining = ((focusEndAt - System.currentTimeMillis()) / 1000L).toInt()
                if (remaining <= 0) {
                    repository.recordFocusSessionIfNeeded(plannedMinutes, plannedMinutes, startedAt = focusStartedAt)
                    clearPersistedFocusTimer()
                    _state.update { it.copy(focusRunning = false, focusRemainingSeconds = 0, message = "专注完成，太棒了") }
                    break
                }
                _state.update { it.copy(focusRemainingSeconds = remaining) }
                delay(1000)
            }
        }
    }

    private fun persistFocusTimer(plannedMinutes: Int) {
        settingsRepository?.let { settings ->
            viewModelScope.launch {
                settings.update {
                    it.copy(
                        focusStartedAtEpochMillis = focusStartedAt,
                        focusEndAtEpochMillis = focusEndAt,
                        focusPlannedMinutes = plannedMinutes,
                    )
                }
                focusTimerScheduler?.schedule(focusEndAt)
            }
        }
    }

    private suspend fun clearPersistedFocusTimer() {
        focusTimerScheduler?.cancel()
        settingsRepository?.update { it.copy(focusStartedAtEpochMillis = null, focusEndAtEpochMillis = null) }
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
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LearnListViewModel(repository, settingsRepository, focusTimerScheduler) as T
            }
    }
}

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun String.toDayOfWeekSet(): Set<DayOfWeek> = split(',').mapNotNull { token ->
    token.trim().toIntOrNull()?.let { runCatching { DayOfWeek.of(it) }.getOrNull() }
}.toSet()
