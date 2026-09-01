package com.mymoss.learnlist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mymoss.learnlist.BuildConfig
import com.mymoss.learnlist.data.DailyProgressMapper
import com.mymoss.learnlist.data.backup.BackupImportMode
import com.mymoss.learnlist.data.backup.PendingBackupImport
import com.mymoss.learnlist.data.local.CountdownEntity
import com.mymoss.learnlist.data.local.FocusSessionEntity
import com.mymoss.learnlist.data.local.GoalEntity
import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReadingPlanEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.DailyProgressCalculator
import com.mymoss.learnlist.domain.GoalActivity
import com.mymoss.learnlist.domain.GoalDefinition
import com.mymoss.learnlist.domain.GoalMetric
import com.mymoss.learnlist.domain.GoalPeriod
import com.mymoss.learnlist.domain.GoalProgressAggregator
import com.mymoss.learnlist.domain.GoalProgressCalculator
import com.mymoss.learnlist.domain.InitialLearningTracker
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.TodoCompletion
import com.mymoss.learnlist.domain.TodoRecurrence
import com.mymoss.learnlist.domain.TodoRepeatRule
import com.mymoss.learnlist.system.UpdateInfo
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay

enum class AppTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("今日", Icons.Default.Home),
    LEARN("学习", Icons.AutoMirrored.Filled.MenuBook),
    TODO("待办", Icons.Default.TaskAlt),
    FOCUS("专注", Icons.Default.Timer),
    STATS("统计", Icons.Default.BarChart),
    SETTINGS("设置", Icons.Default.Settings),
}

data class UpdateUiState(
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val available: UpdateInfo? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val lastCheckedAtEpochMillis: Long? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnListApp(
    viewModel: LearnListViewModel,
    onExportBackup: (Boolean, String) -> Unit = { _, _ -> },
    onImportBackup: (String, BackupImportMode) -> Unit = { _, _ -> },
    onCheckForUpdate: () -> Unit = {},
    updateState: UpdateUiState = UpdateUiState(),
    onDownloadUpdate: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
    onRequestNotifications: () -> Unit = {},
    onRequestExactAlarms: () -> Unit = {},
    pendingImport: PendingBackupImport? = null,
    onConfirmImport: (BackupImportMode) -> Unit = {},
    onCancelImport: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = AppTab.entries.firstOrNull { it.name == backStackEntry?.destination?.route } ?: AppTab.TODAY
    var showProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var taskProjectId by rememberSaveable { mutableStateOf("") }
    var showReadingDialog by rememberSaveable { mutableStateOf(false) }
    var readingProjectId by rememberSaveable { mutableStateOf("") }
    var showTodoDialog by rememberSaveable { mutableStateOf(false) }
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }
    var showCountdownDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf<LearningTaskEntity?>(null) }
    var showPagesDialog by remember { mutableStateOf<ReadingPlanEntity?>(null) }
    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    val snackbars = remember { SnackbarHostState() }
    var currentDay by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentDay = LocalDate.now()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            val result = snackbars.showSnackbar(it, actionLabel = if (viewModel.canUndoArchive) "撤销" else null)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoLastArchive()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "LEARN / LIST",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                            )
                            Text(selectedTab.label, style = MaterialTheme.typography.headlineSmall)
                        }
                        if (updateState.available != null) {
                            AssistChip(
                                onClick = onDownloadUpdate,
                                enabled = !updateState.isDownloading,
                                label = { Text(if (updateState.isDownloading) "下载中" else "有更新") },
                                leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            )
                        }
                        if (selectedTab == AppTab.TODAY) {
                            IconButton(onClick = onRequestNotifications) {
                                Icon(Icons.Default.Notifications, contentDescription = "通知权限")
                            }
                        }
                    }
                    if (selectedTab == AppTab.TODAY) {
                        Text(
                            text = "把今天过好，剩下的交给节奏。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbars) },
        floatingActionButton = {
            when (selectedTab) {
                AppTab.LEARN -> FloatingActionButton(
                    onClick = { showProjectDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Icon(Icons.Default.Add, "新建项目") }
                AppTab.TODO -> FloatingActionButton(
                    onClick = { showTodoDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Icon(Icons.Default.Add, "新建待办") }
                AppTab.STATS -> FloatingActionButton(
                    onClick = { showGoalDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Icon(Icons.Default.Add, "新建目标") }
                else -> Unit
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            navController.navigate(tab.name) {
                                popUpTo(AppTab.TODAY.name) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.TODAY.name,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(AppTab.TODAY.name) {
                TodayScreen(state, padding, currentDay, viewModel, { showReviewDialog = it }, { showPagesDialog = it }, viewModel::rebalanceReading, viewModel::adjustReadingTarget)
            }
            composable(AppTab.LEARN.name) {
                LearnScreen(
                    state = state,
                    padding = padding,
                    today = currentDay,
                    onNewTask = { taskProjectId = it; showTaskDialog = true },
                    onNewReading = { readingProjectId = it; showReadingDialog = true },
                    onPages = { showPagesDialog = it },
                    onRebalance = viewModel::rebalanceReading,
                    onAdjustTarget = viewModel::adjustReadingTarget,
                    onInitialLearn = viewModel::initialLearn,
                    onReview = { showReviewDialog = it },
                    onArchive = viewModel::archiveProject,
                    onPause = viewModel::setProjectPaused,
                )
            }
            composable(AppTab.TODO.name) { TodoScreen(state, padding, currentDay, viewModel::toggleTodo) }
            composable(AppTab.FOCUS.name) { FocusScreen(state, padding, viewModel::startFocus, viewModel::stopFocus) }
            composable(AppTab.STATS.name) {
                StatsScreen(state, padding, currentDay, { showGoalDialog = true }, { showCountdownDialog = true }, viewModel::completeCountdown)
            }
            composable(AppTab.SETTINGS.name) {
                SettingsScreen(
                    state = state,
                    padding = padding,
                    projects = state.projects,
                    onBackup = { showBackupDialog = true },
                    onImport = { showImportDialog = true },
                    onCheckForUpdate = onCheckForUpdate,
                    updateState = updateState,
                    onDownloadUpdate = onDownloadUpdate,
                    onDismissUpdate = onDismissUpdate,
                    onRequestNotifications = onRequestNotifications,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onNewReminder = viewModel::addReminder,
                    onSetReminderEnabled = viewModel::setReminderEnabled,
                    onDeleteReminder = viewModel::deleteReminder,
                    restDays = state.restDays,
                    onSetRestDays = viewModel::setRestDays,
                    onRestoreProject = viewModel::restoreProject,
                )
            }
        }
    }

    if (showProjectDialog) {
        ProjectDialog({ showProjectDialog = false }) { title, type, description, tags -> viewModel.addProject(title, type, description, tags); showProjectDialog = false }
    }
    if (showTaskDialog) {
        TaskDialog(state.projects, taskProjectId, { showTaskDialog = false }) { projectId, title, prompt, notes, source, required -> viewModel.addTask(projectId, title, prompt, notes, source, required); showTaskDialog = false }
    }
    if (showReadingDialog) {
        ReadingDialog(state.projects, readingProjectId, { showReadingDialog = false }) { projectId, title, total, target, deadline -> viewModel.addReadingPlan(projectId, title, total, target, deadline); showReadingDialog = false }
    }
    if (showTodoDialog) {
        TodoDialog({ showTodoDialog = false }) { title, notes, required, repeat, custom, dueDate -> viewModel.addTodo(title, notes, required, repeat, custom, dueDate); showTodoDialog = false }
    }
    showReviewDialog?.let { task ->
        ReviewDialog(task, { showReviewDialog = null }) { rating -> viewModel.review(task.id, rating); showReviewDialog = null }
    }
    showPagesDialog?.let { plan ->
        PagesDialog(plan, state.pageLogs.filter { it.planId == plan.id && it.localDate == currentDay.toString() }.sumOf(PageLogEntity::pagesRead), { showPagesDialog = null }) { pages -> viewModel.logReading(plan.id, pages); showPagesDialog = null }
    }
    if (showGoalDialog) {
        GoalDialog({ showGoalDialog = false }) { title, metric, target, period, endDate -> viewModel.addGoal(title, metric, target, period, endDate); showGoalDialog = false }
    }
    if (showCountdownDialog) {
        CountdownDialog({ showCountdownDialog = false }) { title, date, time, note, reminder -> viewModel.addCountdown(title, date, time, note, reminder); showCountdownDialog = false }
    }
    if (showBackupDialog) {
        BackupDialog("导出备份", "生成备份", { showBackupDialog = false }) { encrypted, password -> onExportBackup(encrypted, password); showBackupDialog = false }
    }
    if (showImportDialog) {
        ImportDialog({ showImportDialog = false }) { password, mode -> onImportBackup(password, mode); showImportDialog = false }
    }
    pendingImport?.let { request -> ImportPreviewDialog(request, onCancelImport, onConfirmImport) }
}

@Composable
private fun TodayScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    today: LocalDate,
    viewModel: LearnListViewModel,
    onReview: (LearningTaskEntity) -> Unit,
    onPages: (ReadingPlanEntity) -> Unit,
    onRebalance: (String) -> Unit,
    onAdjustTarget: (String, Int) -> Unit,
) {
    val activeProjectIds = state.projects.filterNot(ProjectEntity::isPaused).map(ProjectEntity::id).toSet()
    val dueTasks = state.tasks.filter { it.projectId in activeProjectIds && it.isDueOn(today) }
    val readingPlans = state.readingPlans.filter { plan ->
        val started = runCatching { LocalDate.parse(plan.startDate) <= today }.getOrDefault(true)
        !plan.isPaused && plan.projectId in activeProjectIds && started && (plan.currentPage < plan.totalPages || state.pageLogs.any { it.planId == plan.id && it.localDate == today.toString() })
    }
    val dueTodos = state.todos.filter { it.isDueOn(today) }
    val progress = DailyProgressCalculator().calculate(
        DailyProgressMapper.from(
            projects = state.projects + state.archivedProjects,
            tasks = state.tasks,
            reviewLogs = state.reviewLogs,
            readingPlans = state.readingPlans,
            readingTargets = state.readingTargets,
            pageLogs = state.pageLogs,
            todos = state.todos,
        ),
        today,
    )
    val streak = calculateStreak(state, today, state.restDays)
    val readingPages = state.pageLogs.filter { it.localDate == today.toString() }.sumOf(PageLogEntity::pagesRead)
    val todoDone = dueTodos.count { it.isCompletedOn(today) }
    val focusMinutes = state.focusSessions.filter { it.startedAt.toLocalDate() == today }.sumOf(FocusSessionEntity::actualMinutes)
    val percent = progress.percent

    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(today.format(DateTimeFormatter.ofPattern("M月d日 · E", Locale.CHINA)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(if (progress.totalRequired == 0) "准备好开始了吗？" else "今天，完成一个小动作。", style = MaterialTheme.typography.headlineSmall)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$streak 天", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFFE88A6B)))),
            ) {
                Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("今日必做", color = Color.White.copy(alpha = 0.76f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (percent == null) "先放下一件事" else "${progress.completedRequired} / ${progress.totalRequired} 项完成",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            if (percent == null) "创建学习项目后，进度会自动出现" else if (percent == 100) "今天的节奏完成得很漂亮" else "保持轻量，继续下一步",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 13.sp,
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(94.dp)) {
                        CircularProgressIndicator(
                            progress = { (percent ?: 0) / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.22f),
                            strokeWidth = 8.dp,
                        )
                        Text(percent?.let { "$it%" } ?: "—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("今天的节奏", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("复习", dueTasks.count(LearningTaskEntity::hasLearned).toString(), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    MetricCard("阅读", "${readingPages}页", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                    MetricCard("专注", "${focusMinutes}分", Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("新任务", dueTasks.count { !it.hasLearned }.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                    MetricCard("待办", "$todoDone/${dueTodos.size}", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    MetricCard("阅读计划", readingPlans.size.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        item { SectionHeader("今天先做这些", "建议先完成 20 项；所有逾期复习都会列出") }
        if (dueTasks.isEmpty()) item { EmptyCard("没有积压复习。去学习页添加一个新任务吧。", Icons.Default.AutoAwesome) }
        items(dueTasks, key = { it.id }) { task ->
            ReviewTaskCard(task, { onReview(task) }, { viewModel.initialLearn(task.id) }, compact = true)
        }
        item { SectionHeader("阅读进度", "今天达标就算完成一项") }
        if (readingPlans.isEmpty()) item { EmptyCard("还没有进行中的阅读计划。", Icons.AutoMirrored.Filled.MenuBook) }
        items(readingPlans, key = { it.id }) { plan ->
            ReadingPlanCard(
                plan = plan,
                pagesToday = state.pageLogs.filter { it.planId == plan.id && it.localDate == today.toString() }.sumOf(PageLogEntity::pagesRead),
                targetPages = state.readingTargets.targetFor(plan.id, today, plan.dailyTarget),
                onLog = { onPages(plan) },
                onRebalance = { onRebalance(plan.id) },
                onAdjustTarget = { onAdjustTarget(plan.id, it) },
            )
        }
        item { SectionHeader("今日待办", "重复规则会自动带到正确的日期") }
        if (dueTodos.isEmpty()) item { EmptyCard("今天没有到期待办，给自己留一点空间。", Icons.Default.CheckCircleOutline) }
        items(dueTodos, key = { it.id }) { todo ->
            TodoCard(todo, today) { viewModel.toggleTodo(todo.id, today, todo.isCompletedOn(today)) }
        }
    }
}

@Composable
private fun LearnScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    today: LocalDate,
    onNewTask: (String) -> Unit,
    onNewReading: (String) -> Unit,
    onPages: (ReadingPlanEntity) -> Unit,
    onRebalance: (String) -> Unit,
    onAdjustTarget: (String, Int) -> Unit,
    onInitialLearn: (String) -> Unit,
    onReview: (LearningTaskEntity) -> Unit,
    onArchive: (String) -> Unit,
    onPause: (String, Boolean) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = query.trim().lowercase()
    val visibleProjects = state.projects.filter { project ->
        normalizedQuery.isBlank() || listOf(project.title, project.type, project.description, project.tagCsv).any { it.lowercase().contains(normalizedQuery) } ||
            state.tasks.any { it.projectId == project.id && it.title.lowercase().contains(normalizedQuery) } ||
            state.readingPlans.any { it.projectId == project.id && it.title.lowercase().contains(normalizedQuery) }
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("学习库", style = MaterialTheme.typography.headlineSmall)
                        Text("把书、课程和技能拆成可回忆、可完成的行动。", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索项目、任务或标签") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (query.isNotBlank()) ({ IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "清除搜索") } }) else null,
                shape = MaterialTheme.shapes.medium,
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${visibleProjects.size} 个项目", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("逾期内容不会隐藏", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        if (visibleProjects.isEmpty()) item { EmptyCard(if (state.projects.isEmpty()) "创建第一个学习项目：书籍、课程或技能" else "没有匹配的学习项目", Icons.Default.Search) }
        items(visibleProjects, key = { it.id }) { project ->
            ProjectCard(
                project = project,
                tasks = state.tasks.filter { it.projectId == project.id },
                plans = state.readingPlans.filter { it.projectId == project.id },
                pageLogs = state.pageLogs,
                readingTargets = state.readingTargets,
                today = today,
                onNewTask = { onNewTask(project.id) },
                onNewReading = { onNewReading(project.id) },
                onPages = onPages,
                onRebalance = onRebalance,
                onAdjustTarget = onAdjustTarget,
                onInitialLearn = onInitialLearn,
                onReview = onReview,
                onArchive = { onArchive(project.id) },
                onPause = { onPause(project.id, !project.isPaused) },
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectEntity,
    tasks: List<LearningTaskEntity>,
    plans: List<ReadingPlanEntity>,
    pageLogs: List<PageLogEntity>,
    readingTargets: List<ReadingTargetEntity>,
    today: LocalDate,
    onNewTask: () -> Unit,
    onNewReading: () -> Unit,
    onPages: (ReadingPlanEntity) -> Unit,
    onRebalance: (String) -> Unit,
    onAdjustTarget: (String, Int) -> Unit,
    onInitialLearn: (String) -> Unit,
    onReview: (LearningTaskEntity) -> Unit,
    onArchive: () -> Unit,
    onPause: () -> Unit,
) {
    var expanded by rememberSaveable(project.id) { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }
    val accent = projectAccent(project)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(project.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(project.type + if (project.isPaused) " · 已暂停" else " · 进行中", color = if (project.isPaused) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, if (expanded) "收起" else "展开") }
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, "更多操作") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(if (project.isPaused) "恢复项目" else "暂停项目") },
                            leadingIcon = { Icon(if (project.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null) },
                            onClick = { menuExpanded = false; onPause() },
                        )
                        DropdownMenuItem(
                            text = { Text("归档项目") },
                            leadingIcon = { Icon(Icons.Default.Archive, null) },
                            onClick = { menuExpanded = false; onArchive() },
                        )
                    }
                }
            }
            if (expanded) {
                if (project.description.isNotBlank()) Text(project.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                if (project.tagCsv.isNotBlank()) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        project.tagCsv.split(',').map(String::trim).filter(String::isNotBlank).forEach { tag ->
                            TagPill(tag, accent)
                        }
                    }
                }
                plans.forEach { plan ->
                    ReadingPlanCard(
                        plan = plan,
                        pagesToday = pageLogs.filter { it.planId == plan.id && it.localDate == today.toString() }.sumOf(PageLogEntity::pagesRead),
                        targetPages = readingTargets.targetFor(plan.id, today, plan.dailyTarget),
                        onLog = { onPages(plan) },
                        onRebalance = { onRebalance(plan.id) },
                        onAdjustTarget = { onAdjustTarget(plan.id, it) },
                    )
                }
                tasks.forEach { task ->
                    if (!project.isPaused && (task.isDueOn(today) || !task.hasLearned)) {
                        ReviewTaskCard(task, { onReview(task) }, { onInitialLearn(task.id) })
                    } else if (!project.isPaused) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircleOutline, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(task.title, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                                Text("${task.nextReviewDate ?: "待开始"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNewTask, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("学习任务") }
                    OutlinedButton(onClick = onNewReading, modifier = Modifier.weight(1f)) { Icon(Icons.AutoMirrored.Filled.MenuBook, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("阅读计划") }
                }
            }
        }
    }
}

@Composable
private fun TodoScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    today: LocalDate,
    onToggle: (String, LocalDate, Boolean) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val todos = state.todos.filter { it.isDueOn(today) && (query.isBlank() || it.title.contains(query.trim(), true) || it.notes.contains(query.trim(), true)) }
    val done = todos.count { it.isCompletedOn(today) }
    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("今日清单", style = MaterialTheme.typography.headlineSmall)
                        Text(if (todos.isEmpty()) "给今天留一张干净的纸。" else "$done / ${todos.size} 项已完成", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                    }
                    Icon(Icons.Default.TaskAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
                }
            }
        }
        item {
            OutlinedTextField(
                query,
                { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索待办") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (query.isNotBlank()) ({ IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "清除搜索") } }) else null,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )
        }
        item { Text("${todos.size} 项安排", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
        if (todos.isEmpty()) item { EmptyCard("没有到期待办，点击右下角添加。", Icons.Default.CheckCircleOutline) }
        items(todos, key = { it.id }) { todo -> TodoCard(todo, today) { onToggle(todo.id, today, todo.isCompletedOn(today)) } }
        item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("重复规则", fontWeight = FontWeight.Bold)
                        Text("一次性、每天、每周、工作日和自定义星期都会按日期保存完成记录。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusScreen(state: LearnListUiState, padding: PaddingValues, onStart: (Int) -> Unit, onStop: () -> Unit) {
    val minutes = state.focusRemainingSeconds / 60
    val seconds = state.focusRemainingSeconds % 60
    var customMinutes by rememberSaveable { mutableStateOf("25") }
    val progress = if (state.focusRunning && state.focusPlannedMinutes > 0) state.focusRemainingSeconds / (state.focusPlannedMinutes * 60f) else 0f
    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, Color(0xFF4D9E88)))),
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (state.focusRunning) "专注进行中" else "专注工作台", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (state.focusRunning) "让这一段时间只属于一件事" else "选择一段不被打扰的时间", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        Icon(Icons.Default.Timer, null, tint = Color.White.copy(alpha = 0.86f), modifier = Modifier.size(32.dp))
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(208.dp)) {
                        CircularProgressIndicator(progress = { if (state.focusRunning) progress else 0f }, modifier = Modifier.fillMaxSize(), color = Color.White, trackColor = Color.White.copy(alpha = 0.2f), strokeWidth = 12.dp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (state.focusRunning) "%02d:%02d".format(minutes, seconds) else "25:00", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            Text(if (state.focusRunning) "剩余时间" else "番茄钟", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                    }
                    if (state.focusRunning) {
                        Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary)) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("结束并保存")
                        }
                    } else {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf(25, 50, 90).forEach { preset ->
                                FilterChip(selected = false, onClick = { onStart(preset) }, label = { Text("${preset} 分") }, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)), colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(labelColor = Color.White, iconColor = Color.White))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(customMinutes, { customMinutes = it.filter(Char::isDigit).take(3) }, Modifier.weight(1f), label = { Text("自定义分钟") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Button(onClick = { customMinutes.toIntOrNull()?.let(onStart) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary)) { Text("开始") }
                        }
                        Text("可设置 1–180 分钟 · 离开应用后会自动恢复", color = Color.White.copy(alpha = 0.74f), fontSize = 12.sp)
                    }
                }
            }
        }
        item { SectionHeader("最近专注", "完成后自动计入统计") }
        if (state.focusSessions.isEmpty()) item { EmptyCard("完成第一段番茄钟后，这里会出现你的专注记录。", Icons.Default.Timer) }
        items(state.focusSessions.take(20), key = { it.id }) { session ->
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(19.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text("${session.actualMinutes} 分钟专注", fontWeight = FontWeight.SemiBold); Text(session.startedAt.toLocalDate().toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                    Text(session.status, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    today: LocalDate,
    onNewGoal: () -> Unit,
    onNewCountdown: () -> Unit,
    onCompleteCountdown: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("看见你的积累", style = MaterialTheme.typography.headlineSmall)
                    Text("把每一次复习、每一页和每一段专注，变成可回看的轨迹。", color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 13.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("复习总数", state.reviewLogs.size.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                MetricCard("阅读总页", state.pageLogs.sumOf(PageLogEntity::pagesRead).toString(), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                MetricCard("专注分钟", state.focusSessions.sumOf(FocusSessionEntity::actualMinutes).toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }
        }
        item {
            SectionHeader("最近 28 天", "复习 + 阅读页数的密度")
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.padding(16.dp)) { HeatMap(state, today) } }
        }
        item {
            SectionHeader("最近 7 天", "复习、阅读和专注的综合趋势")
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.padding(16.dp)) { TrendChart(state, today) } }
        }
        item { SectionHeader("量化目标", "给想坚持的事一个可见的终点", trailing = { TextButton(onClick = onNewGoal) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Text("新增") } }) }
        if (state.goals.isEmpty()) item { EmptyCard("例如：每天专注 50 分钟、每周复习 20 项。", Icons.Default.Flag) }
        items(state.goals, key = { it.id }) { goal -> GoalCard(goal, state, today) }
        item { SectionHeader("倒计时", "考试、截止日或下一次重要事件", trailing = { TextButton(onClick = onNewCountdown) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Text("新增") } }) }
        if (state.countdowns.isEmpty()) item { EmptyCard("为重要事件留一个提前量。", Icons.Default.CalendarToday) }
        items(state.countdowns, key = { it.id }) { countdown -> CountdownCard(countdown) { onCompleteCountdown(countdown.id) } }
    }
}

@Composable
private fun SettingsScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    projects: List<ProjectEntity>,
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onCheckForUpdate: () -> Unit,
    updateState: UpdateUiState,
    onDownloadUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onNewReminder: (String?, String, String, String, String, String) -> Unit,
    onSetReminderEnabled: (String, Boolean) -> Unit,
    onDeleteReminder: (String) -> Unit,
    restDays: Set<DayOfWeek>,
    onSetRestDays: (Set<DayOfWeek>) -> Unit,
    onRestoreProject: (String) -> Unit,
) {
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    var reminderToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("学习清单", style = MaterialTheme.typography.titleLarge)
                        Text("离线优先 · 本机保存 · 无账号", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        item { SectionHeader("更新中心", "每 24 小时自动检查一次，也可以现在手动检查") }
        item {
            UpdateCenterCard(updateState, onCheckForUpdate, onDownloadUpdate, onDismissUpdate)
        }
        item { SectionHeader("固定提醒", "在你习惯的时间，把今天拉回眼前") }
        item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text("每日进度和项目提醒", fontWeight = FontWeight.Bold); Text("支持多个固定时间、星期选择和安静时段", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showReminderDialog = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("添加提醒") }
                        OutlinedButton(onClick = onRequestNotifications, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Notifications, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("通知权限") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRequestExactAlarms, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Timer, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("精确提醒权限") }
                    }
                    if (state.reminders.isEmpty()) Text("还没有固定提醒，添加一个适合你的节奏。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    state.reminders.forEach { reminder ->
                        val projectTitle = projects.firstOrNull { it.id == reminder.projectId }?.title
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("${reminder.timeMinutes / 60}:${(reminder.timeMinutes % 60).toString().padStart(2, '0')} · ${if (reminder.kind == "SUMMARY") "每日总览" else projectTitle ?: "项目提醒"}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (reminder.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${reminder.repeatDays.replace(',', '、')} · 安静 ${formatMinutes(reminder.quietStartMinutes)}—${formatMinutes(reminder.quietEndMinutes)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onSetReminderEnabled(reminder.id, !reminder.enabled) }) { Text(if (reminder.enabled) "停用" else "启用") }
                                IconButton(onClick = { reminderToDeleteId = reminder.id }) {
                                    Icon(Icons.Default.Delete, "删除提醒", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { SectionHeader("数据安全", "备份、迁移和恢复都由你掌握") }
        item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("加密备份是默认选择。明文导出会在确认后执行，导入前可预览并选择合并或替换。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onBackup, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("导出") }
                        OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("导入") }
                    }
                }
            }
        }
        item { SectionHeader("连续打卡", "休息日不会打断你的节奏") }
        item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("选择固定休息日", fontWeight = FontWeight.Bold)
                    Text("没有必做行动的日期也不会被计算进连续打卡。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                        (1..7).forEach { value ->
                            val day = DayOfWeek.of(value)
                            FilterChip(selected = day in restDays, onClick = { onSetRestDays(if (day in restDays) restDays - day else restDays + day) }, label = { Text("周${labels[value - 1]}") })
                        }
                    }
                }
            }
        }
        if (state.archivedProjects.isNotEmpty()) {
            item { SectionHeader("已归档项目", "需要时可以恢复") }
            items(state.archivedProjects, key = { "archived-${it.id}" }) { project ->
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(project.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { onRestoreProject(project.id) }) { Text("恢复"); Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(17.dp)) }
                    }
                }
            }
        }
    }
    if (showReminderDialog) {
        ReminderDialog(projects, { showReminderDialog = false }) { projectId, kind, time, quietStart, quietEnd, repeatDays -> onNewReminder(projectId, kind, time, quietStart, quietEnd, repeatDays); showReminderDialog = false }
    }
    val reminderToDelete = reminderToDeleteId?.let { id -> state.reminders.firstOrNull { it.id == id } }
    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDeleteId = null },
            title = { Text("删除这条提醒？") },
            text = { Text("${reminderToDelete.timeMinutes / 60}:${(reminderToDelete.timeMinutes % 60).toString().padStart(2, '0')} 的提醒将不再触发。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteReminder(reminderToDelete.id)
                    reminderToDeleteId = null
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { reminderToDeleteId = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun UpdateCenterCard(updateState: UpdateUiState, onCheck: () -> Unit, onDownload: () -> Unit, onDismiss: () -> Unit) {
    val available = updateState.available
    Surface(shape = MaterialTheme.shapes.medium, color = if (available != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (available != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (available == null) Icons.Default.CloudDownload else Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (available == null) "当前版本 v${BuildConfig.VERSION_NAME}" else "发现新版本 v${available.versionName}", fontWeight = FontWeight.Bold)
                    Text(
                        when {
                            updateState.isDownloading -> "正在下载并校验安装包…"
                            updateState.isChecking -> "正在连接 GitHub Release…"
                            updateState.errorMessage != null -> updateState.errorMessage
                            available != null -> "校验通过后交给系统安装器，需要你确认"
                            updateState.statusMessage != null -> updateState.statusMessage
                            else -> "稳定版更新来自 GitHub，数据不会上传"
                        },
                        color = if (updateState.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (available != null) IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "暂不更新") }
            }
            if (available != null && available.releaseNotes.isNotBlank()) {
                Text(available.releaseNotes.trim(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("上次检查：${formatLastChecked(updateState.lastCheckedAtEpochMillis)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
                if (available != null) {
                    Button(onClick = onDownload, enabled = !updateState.isDownloading) { Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("下载并安装") }
                } else {
                    OutlinedButton(onClick = onCheck, enabled = !updateState.isChecking && !updateState.isDownloading) { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text(if (updateState.isChecking) "检查中" else "检查更新") }
                }
            }
            Text("下载后会验证 SHA-256；不会静默安装，也不会覆盖你的本地数据。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ReviewTaskCard(task: LearningTaskEntity, onReview: () -> Unit, onInitial: () -> Unit, compact: Boolean = false) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(if (compact) 13.dp else 15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(task.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (task.hasLearned) "第 ${task.stage + 1} 个记忆间隔" else "首次学习", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                TagPill(if (task.hasLearned) "到期" else "新任务", MaterialTheme.colorScheme.primary)
            }
            if (task.prompt.isNotBlank()) Text("提示：${task.prompt}", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (task.hasLearned) "计划 ${task.nextReviewDate ?: "今天"}" else "完成后明天开始第一次复习", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                if (task.hasLearned) {
                    FilledTonalButton(onClick = onReview, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)) { Text("开始复习") }
                } else {
                    Button(onClick = onInitial, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)) { Icon(Icons.Default.Check, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("学完") }
                }
            }
        }
    }
}

@Composable
private fun ReadingPlanCard(plan: ReadingPlanEntity, pagesToday: Int, targetPages: Int, onLog: () -> Unit, onRebalance: () -> Unit, onAdjustTarget: (Int) -> Unit) {
    val percent = (plan.currentPage * 100 / plan.totalPages.coerceAtLeast(1)).coerceIn(0, 100)
    val planFinished = plan.currentPage >= plan.totalPages
    val targetDone = planFinished || pagesToday >= targetPages
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) { Text(plan.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("第 ${plan.currentPage} / ${plan.totalPages} 页", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
                Text("$percent%", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.secondaryContainer)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (planFinished) "本书已读完" else "今日 ${pagesToday} / ${targetPages} 页", color = if (targetDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(if (planFinished) "全部页数已完成" else if (targetDone) "今日目标已完成" else "还差 ${targetPages - pagesToday} 页", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                FilledTonalButton(onClick = onLog, enabled = !planFinished, contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)) { Text(if (planFinished) "已完成" else "记页数") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("每日目标", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                IconButton(onClick = { onAdjustTarget((targetPages - 1).coerceAtLeast(1)) }, modifier = Modifier.size(30.dp)) { Text("−", fontSize = 18.sp) }
                Text("$targetPages 页", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                IconButton(onClick = { onAdjustTarget(targetPages + 1) }, modifier = Modifier.size(30.dp)) { Text("+", fontSize = 18.sp) }
                Spacer(Modifier.weight(1f))
                if (plan.deadline != null) Text("截止 ${plan.deadline}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            if (plan.deadline != null) OutlinedButton(onClick = onRebalance, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 7.dp)) { Text("将剩余页数均摊到截止日") }
        }
    }
}

@Composable
private fun TodoCard(todo: TodoEntity, today: LocalDate, onToggle: () -> Unit) {
    val completed = todo.isCompletedOn(today)
    Surface(shape = MaterialTheme.shapes.medium, color = if (completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Icon(if (completed) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline, if (completed) "撤销完成" else "完成", tint = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(todo.title, fontWeight = FontWeight.SemiBold, color = if (completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${repeatLabel(todo.repeatRule)}${if (todo.isRequired) " · 必做" else " · 可选"}${todo.dueDate?.let { " · $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            if (completed) Text("已完成", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, state: LearnListUiState, today: LocalDate) {
    val current = goalCurrent(goal, state, today)
    val percent = GoalProgressCalculator().calculate(current, goal.targetValue.coerceAtLeast(1)).percent
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(9.dp))
                Text(goal.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$percent%", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.tertiaryContainer)
            Text("${metricLabel(goal.metric)}：$current / ${goal.targetValue} · ${periodLabel(goal.period)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CountdownCard(countdown: CountdownEntity, onComplete: () -> Unit) {
    var now by remember(countdown.id, countdown.isCompleted, countdown.eventAtEpochMillis) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(countdown.id, countdown.isCompleted, countdown.eventAtEpochMillis) {
        while (!countdown.isCompleted) {
            now = System.currentTimeMillis()
            if (now >= countdown.eventAtEpochMillis) break
            delay(1000)
        }
        now = System.currentTimeMillis()
    }
    val duration = Duration.ofMillis(countdown.eventAtEpochMillis - now)
    val text = when { countdown.isCompleted -> "已完成"; duration.isNegative -> "已到期"; else -> "${duration.toDays()}天 ${duration.toHours() % 24}小时 ${duration.toMinutes() % 60}分" }
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(19.dp)) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(countdown.title, fontWeight = FontWeight.SemiBold); Text(text, color = if (duration.isNegative && !countdown.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); if (countdown.note.isNotBlank()) Text(countdown.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!countdown.isCompleted) IconButton(onClick = onComplete) { Icon(Icons.Default.Check, "完成", tint = MaterialTheme.colorScheme.secondary) }
        }
    }
}

@Composable
private fun HeatMap(state: LearnListUiState, today: LocalDate) {
    val values = (0 until 28).map { offset ->
        val date = today.minusDays((27 - offset).toLong()).toString()
        state.reviewLogs.count { it.reviewedOn == date } + state.pageLogs.filter { it.localDate == date }.sumOf(PageLogEntity::pagesRead) / 10
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(4) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(7) { day -> Box(Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(7.dp)).background(heatColor(values[week * 7 + day]))) }
            }
        }
        Text("颜色越深，代表当天完成的复习或阅读越多。", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TrendChart(state: LearnListUiState, today: LocalDate) {
    val values = (0..6).map { offset ->
        val date = today.minusDays((6 - offset).toLong())
        val token = date.toString()
        state.reviewLogs.count { it.reviewedOn == token } + state.pageLogs.filter { it.localDate == token }.sumOf(PageLogEntity::pagesRead) / 10 + state.focusSessions.filter { it.startedAt.toLocalDate() == date }.sumOf(FocusSessionEntity::actualMinutes) / 25
    }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEachIndexed { index, value ->
            val date = today.minusDays((6 - index).toLong())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA), Modifier.width(25.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(progress = { value / maxValue.toFloat() }, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer)
                Text(value.toString(), Modifier.width(22.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier, accent: Color = MaterialTheme.colorScheme.primary) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.width(24.dp).height(3.dp).clip(RoundedCornerShape(4.dp)).background(accent))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun SectionHeader(text: String, subtitle: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
        }
        trailing?.invoke()
    }
}

@Composable
private fun TagPill(text: String, accent: Color) {
    Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.12f)) { Text(text, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
}

@Composable
private fun EmptyCard(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

private fun List<ReadingTargetEntity>.targetFor(planId: String, date: LocalDate, fallback: Int): Int = firstOrNull { it.planId == planId && it.localDate == date.toString() }?.targetPages?.coerceAtLeast(1) ?: fallback.coerceAtLeast(1)

private fun LearningTaskEntity.isDueOn(date: LocalDate): Boolean {
    if (snoozedUntil?.let { runCatching { LocalDate.parse(it) }.getOrNull()?.isAfter(date) } == true) return false
    if (!hasLearned) return true
    return nextReviewDate == null || runCatching { LocalDate.parse(nextReviewDate) <= date }.getOrDefault(true)
}

private fun TodoEntity.isDueOn(date: LocalDate): Boolean {
    val rule = runCatching { TodoRepeatRule.valueOf(repeatRule) }.getOrDefault(TodoRepeatRule.ONCE)
    val base = dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val custom = customRepeatDays.split(',').mapNotNull { token -> token.trim().toIntOrNull()?.takeIf { it in 1..7 }?.let(DayOfWeek::of) }.toSet()
    val completed = completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.toSet()
    return TodoRecurrence.isDue(rule, base, date, customDays = custom, completedDates = completed)
}

private fun TodoEntity.isCompletedOn(date: LocalDate): Boolean = TodoCompletion.isCompleted(completedDates, date)

private fun repeatLabel(rule: String): String = when (rule) { "DAILY" -> "每天"; "WEEKLY" -> "每周"; "WORKDAYS" -> "工作日"; "CUSTOM" -> "自定义"; else -> "一次性" }

private fun calculateStreak(state: LearnListUiState, today: LocalDate, restDays: Set<DayOfWeek> = emptySet()): Int {
    val input = DailyProgressMapper.from(state.projects + state.archivedProjects, state.tasks, state.reviewLogs, state.readingPlans, state.readingTargets, state.pageLogs, state.todos)
    val calculator = DailyProgressCalculator()
    var streak = 0
    var date = today
    var inspectedDays = 0
    while (streak < 365 && inspectedDays < 365 * 7) {
        inspectedDays += 1
        if (date.dayOfWeek in restDays) { date = date.minusDays(1); continue }
        val summary = calculator.calculate(input, date)
        if (summary.totalRequired == 0) { date = date.minusDays(1); continue }
        if (summary.completedRequired == 0) break
        streak += 1
        date = date.minusDays(1)
    }
    return streak
}

private fun goalCurrent(goal: GoalEntity, state: LearnListUiState, today: LocalDate): Int {
    val metric = GoalMetric.fromStorage(goal.metric) ?: return 0
    val period = GoalPeriod.fromStorage(goal.period) ?: return 0
    val startDate = runCatching { LocalDate.parse(goal.startDate) }.getOrDefault(today)
    val endDate = goal.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    return GoalProgressAggregator().current(GoalDefinition(metric, period, startDate, endDate, goal.projectId), today, goalActivities(state))
}

private fun goalActivities(state: LearnListUiState): List<GoalActivity> = buildList {
    val planProjects = state.readingPlans.associate { it.id to it.projectId }
    val taskProjects = state.tasks.associate { it.id to it.projectId }
    state.pageLogs.forEach { log -> runCatching { LocalDate.parse(log.localDate) }.getOrNull()?.let { add(GoalActivity(GoalMetric.READING_PAGES, it, log.pagesRead, planProjects[log.planId])) } }
    state.reviewLogs.forEach { log -> runCatching { LocalDate.parse(log.reviewedOn) }.getOrNull()?.let { add(GoalActivity(GoalMetric.REVIEW_TASKS, it, 1, taskProjects[log.taskId])) } }
    state.todos.forEach { todo -> todo.completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.forEach { add(GoalActivity(GoalMetric.TODO_DONE, it, 1)) } }
    state.focusSessions.forEach { session -> add(GoalActivity(GoalMetric.FOCUS_MINUTES, session.startedAt.toLocalDate(), session.actualMinutes, session.projectId)) }
}

private fun metricLabel(metric: String): String = when (metric) { "READING_PAGES" -> "阅读页数"; "REVIEW_TASKS" -> "复习项"; "TODO_DONE" -> "待办完成"; else -> "专注分钟" }
private fun periodLabel(period: String): String = when (period) { "WEEKLY" -> "本周"; "MONTHLY" -> "本月"; "CUSTOM" -> "自定义"; else -> "今天" }
private fun formatMinutes(value: Int?): String = value?.let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" } ?: "未设置"
private fun formatLastChecked(epoch: Long?): String = epoch?.let { Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA)) } ?: "尚未检查"
@Composable
private fun projectAccent(project: ProjectEntity): Color = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, Color(0xFF7A6AB8))[project.id.hashCode().absoluteValue % 4]

@Composable
private fun heatColor(value: Int): Color = when { value <= 0 -> MaterialTheme.colorScheme.surfaceVariant; value < 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f); value < 5 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f); else -> MaterialTheme.colorScheme.primary }

@Composable
private fun ProjectPicker(projects: List<ProjectEntity>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = projects.firstOrNull { it.id == selected } ?: projects.firstOrNull()
    Box {
        OutlinedButton(onClick = { current?.let { if (selected != it.id) onSelected(it.id) }; expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(current?.title ?: "选择项目", Modifier.weight(1f)); Icon(Icons.Default.ExpandMore, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { projects.forEach { project -> DropdownMenuItem(text = { Text(project.title) }, onClick = { onSelected(project.id); expanded = false }) } }
    }
}

@Composable
private fun ProjectDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }; var type by rememberSaveable { mutableStateOf("书籍") }; var description by rememberSaveable { mutableStateOf("") }; var tags by rememberSaveable { mutableStateOf("") }
    FormDialog("新建学习项目", onDismiss, "创建", { OutlinedTextField(title, { title = it }, label = { Text("名称") }, singleLine = true); ChoiceRow("项目类型", type, listOf("书籍", "课程", "技能"), { it }) { type = it }; OutlinedTextField(description, { description = it }, label = { Text("简介（可选）") }); OutlinedTextField(tags, { tags = it }, label = { Text("标签，用逗号分隔") }, singleLine = true) }) { onSave(title, type, description, tags) }
}

@Composable
private fun TaskDialog(projects: List<ProjectEntity>, initialProjectId: String, onDismiss: () -> Unit, onSave: (String, String, String, String, String, Boolean) -> Unit) {
    var projectId by rememberSaveable { mutableStateOf(initialProjectId) }; var title by rememberSaveable { mutableStateOf("") }; var prompt by rememberSaveable { mutableStateOf("") }; var notes by rememberSaveable { mutableStateOf("") }; var source by rememberSaveable { mutableStateOf("") }; var required by rememberSaveable { mutableStateOf(true) }
    FormDialog("新建学习任务", onDismiss, "加入", { ProjectPicker(projects, projectId) { projectId = it }; OutlinedTextField(title, { title = it }, label = { Text("任务标题") }, singleLine = true); OutlinedTextField(prompt, { prompt = it }, label = { Text("回忆提示（可选）") }); OutlinedTextField(notes, { notes = it }, label = { Text("资料/笔记（复习时默认隐藏）") }); OutlinedTextField(source, { source = it }, label = { Text("来源（可选）") }, singleLine = true); FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做行动" else "可选行动") }) }) { onSave(projectId, title, prompt, notes, source, required) }
}

@Composable
private fun ReadingDialog(projects: List<ProjectEntity>, initialProjectId: String, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var projectId by rememberSaveable { mutableStateOf(initialProjectId) }; var title by rememberSaveable { mutableStateOf("") }; var total by rememberSaveable { mutableStateOf("") }; var target by rememberSaveable { mutableStateOf("") }; var deadline by rememberSaveable { mutableStateOf("") }
    FormDialog("新建阅读计划", onDismiss, "创建", { ProjectPicker(projects, projectId) { projectId = it }; OutlinedTextField(title, { title = it }, label = { Text("书名或资料名") }, singleLine = true); OutlinedTextField(total, { total = it }, label = { Text("总页数") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(target, { target = it }, label = { Text("每日必须看多少页") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(deadline, { deadline = it }, label = { Text("截止日 YYYY-MM-DD（可选）") }, singleLine = true); Text("设置截止日后，可将剩余页数一键均摊。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }) { onSave(projectId, title, total, target, deadline) }
}

@Composable
private fun TodoDialog(onDismiss: () -> Unit, onSave: (String, String, Boolean, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }; var notes by rememberSaveable { mutableStateOf("") }; var required by rememberSaveable { mutableStateOf(true) }; var repeat by rememberSaveable { mutableStateOf("ONCE") }; var custom by rememberSaveable { mutableStateOf("") }; var dueDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    FormDialog("新建待办", onDismiss, "添加", { OutlinedTextField(title, { title = it }, label = { Text("待办内容") }, singleLine = true); OutlinedTextField(notes, { notes = it }, label = { Text("备注（可选）") }); ChoiceRow("重复方式", repeat, listOf("ONCE", "DAILY", "WEEKLY", "WORKDAYS", "CUSTOM"), ::repeatLabel) { repeat = it }; if (repeat == "CUSTOM") OutlinedTextField(custom, { custom = it }, label = { Text("星期数字：1,3,5") }, singleLine = true); OutlinedTextField(dueDate, { dueDate = it }, label = { Text(if (repeat == "ONCE") "到期日 YYYY-MM-DD" else "开始日期 YYYY-MM-DD") }, singleLine = true); FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做" else "可选") }) }) { onSave(title, notes, required, repeat, custom, dueDate) }
}

@Composable
private fun GoalDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }; var metric by rememberSaveable { mutableStateOf("FOCUS_MINUTES") }; var target by rememberSaveable { mutableStateOf("") }; var period by rememberSaveable { mutableStateOf("DAILY") }; var endDate by rememberSaveable { mutableStateOf("") }
    FormDialog("新建量化目标", onDismiss, "创建", { OutlinedTextField(title, { title = it }, label = { Text("目标名称") }, singleLine = true); ChoiceRow("统计对象", metric, listOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE"), ::metricLabel) { metric = it }; OutlinedTextField(target, { target = it }, label = { Text("目标值") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); ChoiceRow("周期", period, listOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM"), ::periodLabel) { period = it }; if (period == "CUSTOM") OutlinedTextField(endDate, { endDate = it }, label = { Text("截止日 YYYY-MM-DD") }, singleLine = true) }) { onSave(title, metric, target, period, endDate) }
}

@Composable
private fun CountdownDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }; var date by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(7).toString()) }; var time by rememberSaveable { mutableStateOf("09:00") }; var note by rememberSaveable { mutableStateOf("") }; var reminder by rememberSaveable { mutableStateOf("30") }
    FormDialog("新建倒计时", onDismiss, "创建", { OutlinedTextField(title, { title = it }, label = { Text("事件名称") }, singleLine = true); OutlinedTextField(date, { date = it }, label = { Text("日期 YYYY-MM-DD") }, singleLine = true); OutlinedTextField(time, { time = it }, label = { Text("时间 HH:MM") }, singleLine = true); OutlinedTextField(reminder, { reminder = it }, label = { Text("提前提醒分钟（可选）") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(note, { note = it }, label = { Text("备注（可选）") }) }) { onSave(title, date, time, note, reminder) }
}

@Composable
private fun ReviewDialog(task: LearningTaskEntity, onDismiss: () -> Unit, onReview: (RecallRating) -> Unit) {
    var showNotes by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text("复习：${task.title}") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { if (task.prompt.isNotBlank()) Text("回忆提示：${task.prompt}", fontWeight = FontWeight.SemiBold); Text("先在脑中回忆，再选择这次的状态。资料默认隐藏。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp); OutlinedButton(onClick = { showNotes = !showNotes }) { Icon(if (showNotes) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text(if (showNotes) "隐藏资料" else "查看资料") }; if (showNotes) { if (task.notes.isNotBlank()) Text(task.notes); if (task.source.isNotBlank()) Text("来源：${task.source}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }, confirmButton = { Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { TextButton(onClick = { onReview(RecallRating.FORGOT) }) { Text("忘记") }; TextButton(onClick = { onReview(RecallRating.FUZZY) }) { Text("模糊") }; Button(onClick = { onReview(RecallRating.REMEMBERED) }) { Text("记得") } } }, dismissButton = { TextButton(onClick = { onReview(RecallRating.SNOOZE) }) { Text("稍后") } })
}

@Composable
private fun PagesDialog(plan: ReadingPlanEntity, pagesToday: Int, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pages by rememberSaveable { mutableStateOf("") }
    FormDialog("记录阅读页数", onDismiss, "保存", { Text("${plan.title} · 今天已读 $pagesToday 页", fontWeight = FontWeight.SemiBold); OutlinedTextField(pages, { pages = it }, label = { Text("本次读了多少页") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); Text("当前页数会自动累加，最多不超过总页数。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }) { onSave(pages) }
}

@Composable
private fun BackupDialog(title: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: (Boolean, String) -> Unit) {
    var encrypted by rememberSaveable { mutableStateOf(true) }; var password by rememberSaveable { mutableStateOf("") }; var showPlainConfirm by rememberSaveable { mutableStateOf(false) }
    FormDialog(title, onDismiss, confirmLabel, { FilterChip(selected = encrypted, onClick = { encrypted = !encrypted }, label = { Text(if (encrypted) "加密备份（推荐）" else "明文备份") }); if (encrypted) OutlinedTextField(password, { password = it }, label = { Text("密码（至少 8 位）") }, singleLine = true, visualTransformation = PasswordVisualTransformation()); Text(if (encrypted) "密码不会保存；忘记密码无法恢复备份。" else "明文备份包含全部学习记录，请妥善保管。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }) { if (encrypted) onConfirm(true, password) else showPlainConfirm = true }
    if (showPlainConfirm) AlertDialog(onDismissRequest = { showPlainConfirm = false }, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text("确认导出明文备份？") }, text = { Text("明文文件包含全部学习记录，任何拿到文件的人都可以读取。确定继续吗？") }, confirmButton = { Button(onClick = { showPlainConfirm = false; onConfirm(false, "") }) { Text("继续导出") } }, dismissButton = { TextButton(onClick = { showPlainConfirm = false }) { Text("取消") } })
}

@Composable
private fun ImportDialog(onDismiss: () -> Unit, onConfirm: (String, BackupImportMode) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }; var mode by rememberSaveable { mutableStateOf(BackupImportMode.MERGE) }
    FormDialog("导入备份", onDismiss, "选择文件", { OutlinedTextField(password, { password = it }, label = { Text("加密备份密码（明文可留空）") }, singleLine = true, visualTransformation = PasswordVisualTransformation()); Text("导入前会先预览记录数量；合并保留本机数据，替换会清空本机数据。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp); ChoiceRow("导入方式", mode.name, listOf(BackupImportMode.MERGE.name, BackupImportMode.REPLACE.name), { if (it == BackupImportMode.MERGE.name) "合并" else "替换" }) { mode = BackupImportMode.valueOf(it) } }) { onConfirm(password, mode) }
}

@Composable
private fun ImportPreviewDialog(request: PendingBackupImport, onDismiss: () -> Unit, onConfirm: (BackupImportMode) -> Unit) {
    var mode by rememberSaveable(request.preview.createdAt, request.preview.counts.size) { mutableStateOf(request.initialMode) }; val total = request.preview.counts.values.sum()
    AlertDialog(onDismissRequest = onDismiss, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text("确认导入备份") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (request.preview.encrypted) "已通过密码解密" else "明文备份", fontWeight = FontWeight.SemiBold); Text("共 $total 条记录 · 版本 ${request.preview.schemaVersion ?: "—"}"); request.preview.counts.filterValues { it > 0 }.forEach { (name, count) -> Text("$name：$count", fontSize = 12.sp) }; Text("合并保留本机数据并按 ID 覆盖；替换会清空本机数据。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp); ChoiceRow("导入方式", mode.name, listOf(BackupImportMode.MERGE.name, BackupImportMode.REPLACE.name), { if (it == BackupImportMode.MERGE.name) "合并" else "替换" }) { mode = BackupImportMode.valueOf(it) } } }, confirmButton = { Button(onClick = { onConfirm(mode) }) { Text(if (mode == BackupImportMode.REPLACE) "清空并导入" else "合并导入") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

@Composable
private fun ReminderDialog(projects: List<ProjectEntity>, onDismiss: () -> Unit, onSave: (String?, String, String, String, String, String) -> Unit) {
    var kind by rememberSaveable { mutableStateOf("SUMMARY") }; var projectId by rememberSaveable { mutableStateOf("") }; var time by rememberSaveable { mutableStateOf("20:00") }; var quietStart by rememberSaveable { mutableStateOf("22:00") }; var quietEnd by rememberSaveable { mutableStateOf("07:00") }; var repeatDays by rememberSaveable { mutableStateOf("1,2,3,4,5,6,7") }
    val selectedDays = repeatDays.split(',').mapNotNull { it.toIntOrNull() }.toSet(); val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    FormDialog("添加固定提醒", onDismiss, "保存", { ChoiceRow("提醒对象", kind, listOf("SUMMARY", "PROJECT"), { if (it == "SUMMARY") "每日进度" else "学习项目" }) { kind = it; if (it == "PROJECT" && projectId.isBlank()) projectId = projects.firstOrNull()?.id.orEmpty() }; if (kind == "PROJECT") ProjectPicker(projects, projectId) { projectId = it }; OutlinedTextField(time, { time = it }, label = { Text("提醒时间 HH:MM") }, singleLine = true); Text("提醒日期", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) { (1..7).forEach { day -> FilterChip(selected = day in selectedDays, onClick = { repeatDays = (if (day in selectedDays) selectedDays - day else selectedDays + day).sorted().joinToString(",") }, label = { Text(dayLabels[day - 1]) }) } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(quietStart, { quietStart = it }, label = { Text("安静开始") }, singleLine = true, modifier = Modifier.weight(1f)); OutlinedTextField(quietEnd, { quietEnd = it }, label = { Text("安静结束") }, singleLine = true, modifier = Modifier.weight(1f)) }; Text("安静时段内不会触发这条提醒；默认 22:00—07:00。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }) { onSave(if (kind == "PROJECT") projectId.takeIf(String::isNotBlank) else null, kind, time, quietStart, quietEnd, repeatDays) }
}

@Composable
private fun ChoiceRow(label: String, selected: String, options: List<String>, display: (String) -> String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { options.forEach { option -> FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(display(option)) }) } } }
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, confirmLabel: String, content: @Composable ColumnScope.() -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text(title) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }, confirmButton = { Button(onClick = onConfirm) { Text(confirmLabel) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

