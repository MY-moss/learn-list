package com.mymoss.learnlist.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.DailyProgressMapper
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
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

enum class AppTab(val label: String, val icon: ImageVector) {
    TODAY("今日", Icons.Default.Home),
    LEARN("学习", Icons.AutoMirrored.Filled.MenuBook),
    TODO("待办", Icons.Default.TaskAlt),
    FOCUS("专注", Icons.Default.Timer),
    STATS("统计", Icons.Default.BarChart),
    SETTINGS("设置", Icons.Default.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnListApp(
    viewModel: LearnListViewModel,
    onExportBackup: (Boolean, String) -> Unit = { _, _ -> },
    onImportBackup: (String, BackupImportMode) -> Unit = { _, _ -> },
    onCheckForUpdate: () -> Unit = {},
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
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.label, fontWeight = FontWeight.Bold) },
                actions = {
                    if (selectedTab == AppTab.TODAY) {
                        IconButton(onClick = onRequestNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "通知权限")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbars) },
        floatingActionButton = {
            when (selectedTab) {
                AppTab.LEARN -> FloatingActionButton(onClick = { showProjectDialog = true }) { Icon(Icons.Default.Add, "新建项目") }
                AppTab.TODO -> FloatingActionButton(onClick = { showTodoDialog = true }) { Icon(Icons.Default.Add, "新建待办") }
                AppTab.STATS -> FloatingActionButton(onClick = { showGoalDialog = true }) { Icon(Icons.Default.Add, "新建目标") }
                else -> Unit
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
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
                TodayScreen(
                    state = state,
                    padding = padding,
                    today = currentDay,
                    viewModel = viewModel,
                    onReview = { showReviewDialog = it },
                    onPages = { showPagesDialog = it },
                    onRebalance = viewModel::rebalanceReading,
                    onAdjustTarget = viewModel::adjustReadingTarget,
                )
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
                StatsScreen(
                    state = state,
                    padding = padding,
                    today = currentDay,
                    onNewGoal = { showGoalDialog = true },
                    onNewCountdown = { showCountdownDialog = true },
                    onCompleteCountdown = viewModel::completeCountdown,
                )
            }
            composable(AppTab.SETTINGS.name) {
                SettingsScreen(
                    state = state,
                    padding = padding,
                    projects = state.projects,
                    onBackup = { showBackupDialog = true },
                    onImport = { showImportDialog = true },
                    onCheckForUpdate = onCheckForUpdate,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onNewReminder = viewModel::addReminder,
                    onSetReminderEnabled = viewModel::setReminderEnabled,
                    restDays = state.restDays,
                    onSetRestDays = viewModel::setRestDays,
                    onRestoreProject = viewModel::restoreProject,
                )
            }
        }
    }

    if (showProjectDialog) {
        ProjectDialog(
            onDismiss = { showProjectDialog = false },
            onSave = { title, type, description, tags -> viewModel.addProject(title, type, description, tags); showProjectDialog = false },
        )
    }
    if (showTaskDialog) {
        TaskDialog(
            projects = state.projects,
            initialProjectId = taskProjectId,
            onDismiss = { showTaskDialog = false },
            onSave = { projectId, title, prompt, notes, source, required -> viewModel.addTask(projectId, title, prompt, notes, source, required); showTaskDialog = false },
        )
    }
    if (showReadingDialog) {
        ReadingDialog(
            projects = state.projects,
            initialProjectId = readingProjectId,
            onDismiss = { showReadingDialog = false },
            onSave = { projectId, title, total, target, deadline -> viewModel.addReadingPlan(projectId, title, total, target, deadline); showReadingDialog = false },
        )
    }
    if (showTodoDialog) {
        TodoDialog(
            onDismiss = { showTodoDialog = false },
            onSave = { title, notes, required, repeat, custom, dueDate -> viewModel.addTodo(title, notes, required, repeat, custom, dueDate); showTodoDialog = false },
        )
    }
    showReviewDialog?.let { task ->
        ReviewDialog(
            task = task,
            onDismiss = { showReviewDialog = null },
            onReview = { rating -> viewModel.review(task.id, rating); showReviewDialog = null },
        )
    }
    showPagesDialog?.let { plan ->
        PagesDialog(
            plan = plan,
            pagesToday = state.pageLogs.filter { it.planId == plan.id && it.localDate == currentDay.toString() }.sumOf(PageLogEntity::pagesRead),
            onDismiss = { showPagesDialog = null },
            onSave = { pages -> viewModel.logReading(plan.id, pages); showPagesDialog = null },
        )
    }
    if (showGoalDialog) {
        GoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { title, metric, target, period, endDate -> viewModel.addGoal(title, metric, target, period, endDate); showGoalDialog = false },
        )
    }
    if (showCountdownDialog) {
        CountdownDialog(
            onDismiss = { showCountdownDialog = false },
            onSave = { title, date, time, note, reminder -> viewModel.addCountdown(title, date, time, note, reminder); showCountdownDialog = false },
        )
    }
    if (showBackupDialog) {
        BackupDialog(
            title = "导出备份",
            confirmLabel = "生成备份",
            onDismiss = { showBackupDialog = false },
            onConfirm = { encrypted, password -> onExportBackup(encrypted, password); showBackupDialog = false },
        )
    }
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { password, mode -> onImportBackup(password, mode); showImportDialog = false },
        )
    }

    pendingImport?.let { request ->
        ImportPreviewDialog(
            request = request,
            onDismiss = onCancelImport,
            onConfirm = onConfirmImport,
        )
    }
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
    val reviewDone = state.reviewLogs.filter { it.reviewedOn == today.toString() }.map { it.taskId }.toSet()
    val newLearningDone = state.tasks.filter { InitialLearningTracker.isCompletedOn(it.initialLearningDate, today) }.map { it.id }.toSet()
    val taskActionsToday = state.tasks.filter { task ->
        task.projectId in activeProjectIds &&
            (task.isDueOn(today) || task.id in reviewDone || task.id in newLearningDone)
    }
    val readingDone = state.readingPlans.filter { plan ->
        val startsOnOrBeforeToday = runCatching { LocalDate.parse(plan.startDate) <= today }.getOrDefault(true)
        if (plan.isPaused || plan.projectId !in activeProjectIds || !startsOnOrBeforeToday) return@filter false
        plan.currentPage < plan.totalPages || state.pageLogs.any { it.planId == plan.id && it.localDate == today.toString() }
    }.associateWith { plan -> state.pageLogs.filter { it.planId == plan.id && it.localDate == today.toString() }.sumOf(PageLogEntity::pagesRead) }
    val dueTodos = state.todos.filter { it.isDueOn(today) }
    val progress = DailyProgressCalculator().calculate(
        input = DailyProgressMapper.from(
            projects = state.projects + state.archivedProjects,
            tasks = state.tasks,
            reviewLogs = state.reviewLogs,
            readingPlans = state.readingPlans,
            readingTargets = state.readingTargets,
            pageLogs = state.pageLogs,
            todos = state.todos,
        ),
        date = today,
    )
    val completed = progress.completedRequired
    val total = progress.totalRequired
    val percent = progress.percent
    val streak = calculateStreak(state, today, state.restDays)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 12.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("早上好，今天也向前一点点", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(today.format(DateTimeFormatter.ofPattern("yyyy年M月d日 · E")), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(86.dp)) {
                        CircularProgressIndicator(progress = { (percent ?: 0) / 100f }, modifier = Modifier.fillMaxSize(), strokeWidth = 8.dp)
                        Text(percent?.let { "$it%" } ?: "—", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text("今日必做进度", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (percent == null) "先添加一个学习行动" else "$completed / $total 项已完成")
                        Text("连续打卡 $streak 天", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        item {
            val reviewCount = dueTasks.count(LearningTaskEntity::hasLearned)
            val learningCount = dueTasks.count { !it.hasLearned }
            val todoCompleted = dueTodos.count { it.isCompletedOn(today) }
            val focusMinutes = state.focusSessions.filter { it.startedAt.toLocalDate() == today }.sumOf(FocusSessionEntity::actualMinutes)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("复习", reviewCount.toString(), Modifier.weight(1f))
                    MetricCard("学习任务", learningCount.toString(), Modifier.weight(1f))
                    MetricCard("阅读", "${readingDone.values.sum()}页", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("待办", "$todoCompleted/${dueTodos.size}", Modifier.weight(1f))
                    MetricCard("专注", "${focusMinutes}分", Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        item { SectionTitle("今天的学习行动") }
        if (dueTasks.isEmpty()) item { EmptyCard("没有积压复习，去学习页添加一个任务吧") }
        items(dueTasks, key = { it.id }) { task ->
            ReviewTaskCard(task, onReview = { onReview(task) }, onInitial = { viewModel.initialLearn(task.id) }, compact = true)
        }
        item { SectionTitle("阅读计划") }
        if (readingDone.isEmpty()) item { EmptyCard("还没有阅读计划") }
        items(readingDone.keys.toList(), key = { it.id }) { plan ->
            ReadingPlanCard(
                plan = plan,
                pagesToday = readingDone.getValue(plan),
                targetPages = state.readingTargets.targetFor(plan.id, today, plan.dailyTarget),
                onLog = { onPages(plan) },
                onRebalance = { onRebalance(plan.id) },
                onAdjustTarget = { onAdjustTarget(plan.id, it) },
            )
        }
        item { SectionTitle("今日待办") }
        val todoItems = dueTodos.take(5)
        if (todoItems.isEmpty()) item { EmptyCard("今天没有到期待办") }
        items(todoItems, key = { it.id }) { todo ->
            TodoCard(todo, today, onToggle = { viewModel.toggleTodo(todo.id, today, todo.isCompletedOn(today)) })
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
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("把知识拆成今天能完成的一小步", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("复习按 1 / 2 / 4 / 7 / 15 / 30 / 60 / 90 天自动安排，逾期内容不会被隐藏。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("建议每天先处理 20 项积压内容；系统不会替你隐藏未完成的复习。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索项目、任务、标签") },
                singleLine = true,
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
            )
        }
        if (visibleProjects.isEmpty()) item { EmptyCard(if (state.projects.isEmpty()) "创建第一个学习项目：书籍、课程或技能" else "没有匹配的学习项目") }
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
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${project.type}${if (project.tagCsv.isBlank()) "" else " · ${project.tagCsv}"}${if (project.isPaused) " · 已暂停" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.Visibility else Icons.Default.Pause, "展开") }
                IconButton(onClick = onPause) { Icon(if (project.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, if (project.isPaused) "恢复项目" else "暂停项目") }
                IconButton(onClick = onArchive) { Icon(Icons.Default.Archive, "归档") }
            }
            if (expanded) {
                if (project.isPaused) {
                    Text("项目已暂停，暂停期间不会生成必做进度。点击播放按钮恢复。", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        ReviewTaskCard(task, onReview = { onReview(task) }, onInitial = { onInitialLearn(task.id) })
                    } else if (!project.isPaused) {
                        Text("${task.title} · 下次 ${task.nextReviewDate ?: "待开始"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNewTask) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("学习任务") }
                    OutlinedButton(onClick = onNewReading) { Icon(Icons.AutoMirrored.Filled.MenuBook, null); Spacer(Modifier.width(4.dp)); Text("阅读计划") }
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
    val todos = state.todos.filter { it.isDueOn(today) && (query.isBlank() || it.title.contains(query.trim(), ignoreCase = true) || it.notes.contains(query.trim(), ignoreCase = true)) }
    LazyColumn(
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("重复待办不会和学习复习混在一起", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("搜索待办") }, singleLine = true)
        }
        if (todos.isEmpty()) item { EmptyCard("没有到期待办，点击右下角添加") }
        items(todos, key = { it.id }) { todo ->
            TodoCard(todo, today, onToggle = { onToggle(todo.id, today, todo.isCompletedOn(today)) })
        }
        item {
            SectionTitle("重复规则")
            Text("支持一次性、每天、每周、工作日和自定义星期；完成记录按日期保存。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FocusScreen(state: LearnListUiState, padding: PaddingValues, onStart: (Int) -> Unit, onStop: () -> Unit) {
    val minutes = state.focusRemainingSeconds / 60
    val seconds = state.focusRemainingSeconds % 60
    var customMinutes by rememberSaveable { mutableStateOf("25") }
    LazyColumn(
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (state.focusRunning) "专注进行中" else "准备开始专注", style = MaterialTheme.typography.titleLarge)
                    Text(if (state.focusRunning) "%02d:%02d".format(minutes, seconds) else "选择一段不被打扰的时间", fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    if (state.focusRunning) {
                        Button(onClick = onStop) { Icon(Icons.Default.Stop, null); Spacer(Modifier.width(6.dp)); Text("结束并保存") }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(25, 50, 90).forEach { preset -> Button(onClick = { onStart(preset) }) { Text("${preset}分") } }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = customMinutes,
                                onValueChange = { customMinutes = it.filter(Char::isDigit).take(3) },
                                modifier = Modifier.weight(1f),
                                label = { Text("自定义分钟") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                            Button(onClick = { customMinutes.toIntOrNull()?.let(onStart) }) { Text("开始") }
                        }
                        Text("可设置 1–180 分钟", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { SectionTitle("最近专注") }
        if (state.focusSessions.isEmpty()) item { EmptyCard("完成第一段番茄钟后，这里会出现你的专注记录") }
        items(state.focusSessions.take(20), key = { it.id }) { session ->
            Card {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text("${session.actualMinutes} 分钟专注"); Text(session.startedAt.toLocalDate().toString(), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(session.status)
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
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("复习总数", state.reviewLogs.size.toString(), Modifier.weight(1f))
                MetricCard("阅读总页", state.pageLogs.sumOf(PageLogEntity::pagesRead).toString(), Modifier.weight(1f))
                MetricCard("专注分钟", state.focusSessions.sumOf(FocusSessionEntity::actualMinutes).toString(), Modifier.weight(1f))
            }
        }
        item {
            SectionTitle("最近 28 天热力图")
            HeatMap(state, today)
        }
        item {
            SectionTitle("最近 7 天趋势")
            TrendChart(state, today)
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("量化目标", Modifier.weight(1f))
                TextButton(onClick = onNewGoal) { Icon(Icons.Default.Add, null); Text("新增") }
            }
        }
        if (state.goals.isEmpty()) item { EmptyCard("例如：每天专注 50 分钟、每周复习 20 项") }
        items(state.goals, key = { it.id }) { goal -> GoalCard(goal, state, today) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("倒计时", Modifier.weight(1f))
                TextButton(onClick = onNewCountdown) { Icon(Icons.Default.Add, null); Text("新增") }
            }
        }
        if (state.countdowns.isEmpty()) item { EmptyCard("为考试、截止日或重要事件设置提醒") }
        items(state.countdowns, key = { it.id }) { countdown -> CountdownCard(countdown, onComplete = { onCompleteCountdown(countdown.id) }) }
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
    onRequestExactAlarms: () -> Unit,
    onNewReminder: (String?, String, String, String, String, String) -> Unit,
    onSetReminderEnabled: (String, Boolean) -> Unit,
    restDays: Set<DayOfWeek>,
    onSetRestDays: (Set<DayOfWeek>) -> Unit,
    onRestoreProject: (String) -> Unit,
) {
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Learn List · 学习清单", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("离线优先，本机保存；不需要账号，不上传学习记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Android 8.0+ · 艾宾浩斯间隔：1 / 2 / 4 / 7 / 15 / 30 / 60 / 90 天", fontSize = 12.sp)
                }
            }
        }
        item { SectionTitle("固定提醒") }
        item {
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("每日进度汇总", fontWeight = FontWeight.Bold)
                    Text("提醒会根据系统权限和安静时段重新排程。精确闹钟不可用时自动降级为近似提醒。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showReminderDialog = true }) { Icon(Icons.Default.Add, null); Text("添加时间") }
                        OutlinedButton(onClick = onCheckForUpdate) { Icon(Icons.Default.Refresh, null); Text("检查更新") }
                    }
                    OutlinedButton(onClick = onRequestExactAlarms) { Icon(Icons.Default.Notifications, null); Text("管理精确提醒权限") }
                    if (state.reminders.isNotEmpty()) {
                        state.reminders.forEach { reminder ->
                            val projectTitle = projects.firstOrNull { it.id == reminder.projectId }?.title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${reminder.timeMinutes / 60}:${(reminder.timeMinutes % 60).toString().padStart(2, '0')} · ${if (reminder.kind == "SUMMARY") "每日总览" else projectTitle ?: "项目提醒"}",
                                        fontSize = 12.sp,
                                        color = if (reminder.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text("${reminder.repeatDays} · 安静 ${formatMinutes(reminder.quietStartMinutes)}—${formatMinutes(reminder.quietEndMinutes)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onSetReminderEnabled(reminder.id, !reminder.enabled) }) { Text(if (reminder.enabled) "停用" else "启用") }
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("数据与升级") }
        item {
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("备份默认使用加密格式；明文导出仅适合临时迁移。导入前可选择合并或替换。")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onBackup) { Icon(Icons.Default.FileDownload, null); Text("导出备份") }
                        OutlinedButton(onClick = onImport) { Icon(Icons.Default.FileUpload, null); Text("导入备份") }
                    }
                    Text("更新包下载后会校验 SHA-256，再交给 Android 安装器，不会静默安装。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        item {
            SectionTitle("连续打卡休息日")
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("休息日不会打断连续打卡；默认不设置休息日。")
                    val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1..4, 5..7).forEach { range ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                range.forEach { value ->
                                    val day = DayOfWeek.of(value)
                                    FilterChip(
                                        selected = day in restDays,
                                        onClick = {
                                            val next = if (day in restDays) restDays - day else restDays + day
                                            onSetRestDays(next)
                                        },
                                        label = { Text("周${labels[value - 1]}") },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (state.archivedProjects.isNotEmpty()) {
            item { SectionTitle("已归档项目") }
            items(state.archivedProjects, key = { "archived-${it.id}" }) { project ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(project.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { onRestoreProject(project.id) }) { Text("恢复") }
                    }
                }
            }
        }
    }
    if (showReminderDialog) {
        ReminderDialog(
            projects = projects,
            onDismiss = { showReminderDialog = false },
            onSave = { projectId, kind, time, quietStart, quietEnd, repeatDays ->
                onNewReminder(projectId, kind, time, quietStart, quietEnd, repeatDays)
                showReminderDialog = false
            },
        )
    }
}

@Composable
private fun ReviewTaskCard(task: LearningTaskEntity, onReview: () -> Unit, onInitial: () -> Unit, compact: Boolean = false) {
    Card {
        Column(Modifier.padding(if (compact) 12.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(task.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("阶段 ${task.stage + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            if (task.prompt.isNotBlank()) Text("提示：${task.prompt}", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (task.hasLearned) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("到期：${task.nextReviewDate ?: "今天"}", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Button(onClick = onReview) { Text("开始复习") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("首次学习完成后，明天开始第一次复习", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Button(onClick = onInitial) { Text("学完") }
                }
            }
        }
    }
}

@Composable
private fun ReadingPlanCard(
    plan: ReadingPlanEntity,
    pagesToday: Int,
    targetPages: Int,
    onLog: () -> Unit,
    onRebalance: () -> Unit,
    onAdjustTarget: (Int) -> Unit,
) {
    val percent = (plan.currentPage * 100 / plan.totalPages.coerceAtLeast(1)).coerceIn(0, 100)
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(plan.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("$percent%", fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("第 ${plan.currentPage} / ${plan.totalPages} 页 · 今日 $pagesToday / $targetPages 页", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Button(onClick = onLog) { Text("记页数") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("今日目标", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                IconButton(onClick = { onAdjustTarget((targetPages - 1).coerceAtLeast(1)) }) { Text("−", fontSize = 20.sp) }
                Text("$targetPages 页", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onAdjustTarget(targetPages + 1) }) { Text("+", fontSize = 20.sp) }
                Spacer(Modifier.weight(1f))
            }
            if (plan.deadline != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("截止 ${plan.deadline}", Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = onRebalance) { Text("剩余页数均摊") }
                }
            } else {
                Text("设置截止日后，可将欠页均摊到每天；也可直接调整今日目标。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TodoCard(todo: TodoEntity, today: LocalDate, onToggle: () -> Unit) {
    val completed = todo.isCompletedOn(today)
    Card {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (completed) Icons.Default.CheckCircle else Icons.Default.Event, null, tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(todo.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "${repeatLabel(todo.repeatRule)}${if (todo.isRequired) " · 必做" else " · 可选"}${todo.dueDate?.let { " · $it" } ?: ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            IconButton(onClick = onToggle) {
                Icon(if (completed) Icons.AutoMirrored.Filled.Undo else Icons.Default.Check, if (completed) "撤销完成" else "完成")
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, state: LearnListUiState, today: LocalDate) {
    val current = goalCurrent(goal, state, today)
    val progress = GoalProgressCalculator().calculate(current, goal.targetValue.coerceAtLeast(1))
    val percent = progress.percent
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(goal.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text("$percent%", fontWeight = FontWeight.Bold) }
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
            Text("${metricLabel(goal.metric)}：$current / ${goal.targetValue} · ${periodLabel(goal.period)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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
    val text = when {
        countdown.isCompleted -> "已完成"
        duration.isNegative -> "已到期"
        else -> "${duration.toDays()}天 ${duration.toHours() % 24}小时 ${duration.toMinutes() % 60}分"
    }
    Card {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(countdown.title, fontWeight = FontWeight.SemiBold); Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(countdown.note, fontSize = 12.sp) }
            if (!countdown.isCompleted) IconButton(onClick = onComplete) { Icon(Icons.Default.Check, "完成") }
        }
    }
}

@Composable
private fun HeatMap(state: LearnListUiState, today: LocalDate) {
    val values = (0 until 28).map { offset ->
        val date = today.minusDays((27 - offset).toLong()).toString()
        state.reviewLogs.count { it.reviewedOn == date } + state.pageLogs.filter { it.localDate == date }.sumOf(PageLogEntity::pagesRead) / 10
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(4) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { day ->
                    val value = values[week * 7 + day]
                    Box(Modifier.size(28.dp).background(heatColor(value), RoundedCornerShape(6.dp)))
                }
            }
        }
        Text("颜色越深代表当天完成的复习或阅读越多", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TrendChart(state: LearnListUiState, today: LocalDate) {
    val values = (0..6).map { offset ->
        val date = today.minusDays((6 - offset).toLong())
        val token = date.toString()
        state.reviewLogs.count { it.reviewedOn == token } +
            state.pageLogs.filter { it.localDate == token }.sumOf(PageLogEntity::pagesRead) / 10 +
            state.focusSessions.filter { it.startedAt.toLocalDate() == date }.sumOf(FocusSessionEntity::actualMinutes) / 25
    }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEachIndexed { index, value ->
            val date = today.minusDays((6 - index).toLong())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.CHINA), modifier = Modifier.width(28.dp), fontSize = 12.sp)
                LinearProgressIndicator(progress = { value / maxValue.toFloat() }, modifier = Modifier.weight(1f))
                Text(value.toString(), modifier = Modifier.width(24.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("复习项 + 阅读每 10 页 + 专注每 25 分钟的综合趋势", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp)) { Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) } }
}

@Composable
private fun EmptyCard(text: String) { Card { Text(text, Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) { Text(text, modifier, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

private fun List<ReadingTargetEntity>.targetFor(planId: String, date: LocalDate, fallback: Int): Int =
    firstOrNull { it.planId == planId && it.localDate == date.toString() }?.targetPages?.coerceAtLeast(1) ?: fallback.coerceAtLeast(1)

private fun LearningTaskEntity.isDueOn(date: LocalDate): Boolean {
    if (snoozedUntil?.let { runCatching { LocalDate.parse(it) }.getOrNull()?.isAfter(date) } == true) return false
    if (!hasLearned) return true
    return nextReviewDate == null || runCatching { LocalDate.parse(nextReviewDate) <= date }.getOrDefault(true)
}

private fun TodoEntity.isDueOn(date: LocalDate): Boolean {
    val rule = runCatching { TodoRepeatRule.valueOf(repeatRule) }.getOrDefault(TodoRepeatRule.ONCE)
    val base = dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val custom = customRepeatDays.split(',').mapNotNull { token -> token.trim().toIntOrNull()?.takeIf { it in 1..7 }?.let(java.time.DayOfWeek::of) }.toSet()
    val completed = completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.toSet()
    return TodoRecurrence.isDue(rule, base, date, customDays = custom, completedDates = completed)
}

private fun TodoEntity.isCompletedOn(date: LocalDate): Boolean = TodoCompletion.isCompleted(completedDates, date)

private fun repeatLabel(rule: String): String = when (rule) {
    "DAILY" -> "每天"
    "WEEKLY" -> "每周"
    "WORKDAYS" -> "工作日"
    "CUSTOM" -> "自定义"
    else -> "一次性"
}

private fun calculateStreak(state: LearnListUiState, today: LocalDate, restDays: Set<DayOfWeek> = emptySet()): Int {
    val input = DailyProgressMapper.from(
        projects = state.projects + state.archivedProjects,
        tasks = state.tasks,
        reviewLogs = state.reviewLogs,
        readingPlans = state.readingPlans,
        readingTargets = state.readingTargets,
        pageLogs = state.pageLogs,
        todos = state.todos,
    )
    val calculator = DailyProgressCalculator()
    var streak = 0
    var date = today
    var inspectedDays = 0
    while (streak < 365 && inspectedDays < 365 * 7) {
        inspectedDays += 1
        if (date.dayOfWeek in restDays) {
            date = date.minusDays(1)
            continue
        }
        val summary = calculator.calculate(input, date)
        if (summary.totalRequired == 0) {
            date = date.minusDays(1)
            continue
        }
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
    return GoalProgressAggregator().current(
        goal = GoalDefinition(metric = metric, period = period, startDate = startDate, endDate = endDate, projectId = goal.projectId),
        today = today,
        activities = goalActivities(state),
    )
}

private fun goalActivities(state: LearnListUiState): List<GoalActivity> = buildList {
    val planProjects = state.readingPlans.associate { it.id to it.projectId }
    val taskProjects = state.tasks.associate { it.id to it.projectId }
    state.pageLogs.forEach { log ->
        runCatching { LocalDate.parse(log.localDate) }.getOrNull()?.let { date ->
            add(GoalActivity(GoalMetric.READING_PAGES, date, log.pagesRead, planProjects[log.planId]))
        }
    }
    state.reviewLogs.forEach { log ->
        runCatching { LocalDate.parse(log.reviewedOn) }.getOrNull()?.let { date ->
            add(GoalActivity(GoalMetric.REVIEW_TASKS, date, 1, taskProjects[log.taskId]))
        }
    }
    state.todos.forEach { todo ->
        todo.completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.forEach { date ->
            add(GoalActivity(GoalMetric.TODO_DONE, date, 1))
        }
    }
    state.focusSessions.forEach { session ->
        add(GoalActivity(GoalMetric.FOCUS_MINUTES, session.startedAt.toLocalDate(), session.actualMinutes, session.projectId))
    }
}

private fun metricLabel(metric: String): String = when (metric) {
    "READING_PAGES" -> "阅读页数"
    "REVIEW_TASKS" -> "复习项"
    "TODO_DONE" -> "待办完成"
    else -> "专注分钟"
}

private fun periodLabel(period: String): String = when (period) {
    "WEEKLY" -> "本周"
    "MONTHLY" -> "本月"
    "CUSTOM" -> "自定义"
    else -> "今天"
}

private fun formatMinutes(value: Int?): String = value?.let {
    "${it / 60}:${(it % 60).toString().padStart(2, '0')}"
} ?: "未设置"

@Composable
private fun heatColor(value: Int): Color = when {
    value <= 0 -> MaterialTheme.colorScheme.surfaceVariant
    value < 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    value < 5 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun ProjectPicker(projects: List<ProjectEntity>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = projects.firstOrNull { it.id == selected } ?: projects.firstOrNull()
    Box {
        OutlinedButton(
            onClick = {
                current?.let { if (selected != it.id) onSelected(it.id) }
                expanded = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(current?.title ?: "选择项目", Modifier.weight(1f)); Text("⌄") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            projects.forEach { project -> DropdownMenuItem(text = { Text(project.title) }, onClick = { onSelected(project.id); expanded = false }) }
        }
    }
}

@Composable
private fun ProjectDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("书籍") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    FormDialog("新建学习项目", onDismiss, "创建", content = {
        OutlinedTextField(title, { title = it }, label = { Text("名称") }, singleLine = true)
        OutlinedTextField(type, { type = it }, label = { Text("类型：书籍 / 课程 / 技能") }, singleLine = true)
        OutlinedTextField(description, { description = it }, label = { Text("简介（可选）") })
        OutlinedTextField(tags, { tags = it }, label = { Text("标签，用逗号分隔") }, singleLine = true)
    }, onConfirm = { onSave(title, type, description, tags) })
}

@Composable
private fun TaskDialog(projects: List<ProjectEntity>, initialProjectId: String, onDismiss: () -> Unit, onSave: (String, String, String, String, String, Boolean) -> Unit) {
    var projectId by rememberSaveable { mutableStateOf(initialProjectId) }
    var title by rememberSaveable { mutableStateOf("") }
    var prompt by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var source by rememberSaveable { mutableStateOf("") }
    var required by rememberSaveable { mutableStateOf(true) }
    FormDialog("新建学习任务", onDismiss, "加入", content = {
        ProjectPicker(projects, projectId, { projectId = it })
        OutlinedTextField(title, { title = it }, label = { Text("任务标题") }, singleLine = true)
        OutlinedTextField(prompt, { prompt = it }, label = { Text("回忆提示（可选）") })
        OutlinedTextField(notes, { notes = it }, label = { Text("资料/笔记（复习时默认隐藏）") })
        OutlinedTextField(source, { source = it }, label = { Text("来源（可选）") }, singleLine = true)
        FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做行动" else "可选行动") })
    }, onConfirm = { onSave(projectId, title, prompt, notes, source, required) })
}

@Composable
private fun ReadingDialog(projects: List<ProjectEntity>, initialProjectId: String, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var projectId by rememberSaveable { mutableStateOf(initialProjectId) }
    var title by rememberSaveable { mutableStateOf("") }
    var total by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableStateOf("") }
    FormDialog("新建阅读计划", onDismiss, "创建", content = {
        ProjectPicker(projects, projectId, { projectId = it })
        OutlinedTextField(title, { title = it }, label = { Text("书名或资料名") }, singleLine = true)
        OutlinedTextField(total, { total = it }, label = { Text("总页数") }, singleLine = true)
        OutlinedTextField(target, { target = it }, label = { Text("每日必须看多少页") }, singleLine = true)
        OutlinedTextField(deadline, { deadline = it }, label = { Text("截止日 YYYY-MM-DD（可选）") }, singleLine = true)
        Text("支持每天记实际页数；后续可根据欠页一键均摊到截止日。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }, onConfirm = { onSave(projectId, title, total, target, deadline) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoDialog(onDismiss: () -> Unit, onSave: (String, String, Boolean, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var required by rememberSaveable { mutableStateOf(true) }
    var repeat by rememberSaveable { mutableStateOf("ONCE") }
    var custom by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    FormDialog("新建待办", onDismiss, "添加", content = {
        OutlinedTextField(title, { title = it }, label = { Text("待办内容") }, singleLine = true)
        OutlinedTextField(notes, { notes = it }, label = { Text("备注（可选）") })
        PrimaryScrollableTabRow(selectedTabIndex = listOf("ONCE", "DAILY", "WEEKLY", "WORKDAYS", "CUSTOM").indexOf(repeat).coerceAtLeast(0), edgePadding = 0.dp) {
            listOf("ONCE", "DAILY", "WEEKLY", "WORKDAYS", "CUSTOM").forEachIndexed { index, value -> androidx.compose.material3.Tab(selected = repeat == value, onClick = { repeat = value }, text = { Text(repeatLabel(value)) }) }
        }
        if (repeat == "CUSTOM") OutlinedTextField(custom, { custom = it }, label = { Text("星期数字：1,3,5") }, singleLine = true)
        OutlinedTextField(
            dueDate,
            { dueDate = it },
            label = { Text(if (repeat == "ONCE") "到期日 YYYY-MM-DD" else "开始日期 YYYY-MM-DD") },
            singleLine = true,
        )
        FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做" else "可选") })
    }, onConfirm = { onSave(title, notes, required, repeat, custom, dueDate) })
}

@Composable
private fun GoalDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var metric by rememberSaveable { mutableStateOf("FOCUS_MINUTES") }
    var target by rememberSaveable { mutableStateOf("") }
    var period by rememberSaveable { mutableStateOf("DAILY") }
    var endDate by rememberSaveable { mutableStateOf("") }
    FormDialog("新建量化目标", onDismiss, "创建", content = {
        OutlinedTextField(title, { title = it }, label = { Text("目标名称") }, singleLine = true)
        ChoiceRow("统计对象", metric, listOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE"), ::metricLabel) { metric = it }
        OutlinedTextField(target, { target = it }, label = { Text("目标值") }, singleLine = true)
        ChoiceRow("周期", period, listOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM"), ::periodLabel) { period = it }
        if (period == "CUSTOM") OutlinedTextField(endDate, { endDate = it }, label = { Text("截止日 YYYY-MM-DD") }, singleLine = true)
    }, onConfirm = { onSave(title, metric, target, period, endDate) })
}

@Composable
private fun CountdownDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var time by rememberSaveable { mutableStateOf("09:00") }
    var note by rememberSaveable { mutableStateOf("") }
    var reminder by rememberSaveable { mutableStateOf("30") }
    FormDialog("新建倒计时", onDismiss, "创建", content = {
        OutlinedTextField(title, { title = it }, label = { Text("事件名称") }, singleLine = true)
        OutlinedTextField(date, { date = it }, label = { Text("日期 YYYY-MM-DD") }, singleLine = true)
        OutlinedTextField(time, { time = it }, label = { Text("时间 HH:MM") }, singleLine = true)
        OutlinedTextField(reminder, { reminder = it }, label = { Text("提前提醒分钟（可选）") }, singleLine = true)
        OutlinedTextField(note, { note = it }, label = { Text("备注（可选）") })
    }, onConfirm = { onSave(title, date, time, note, reminder) })
}

@Composable
private fun ReviewDialog(task: LearningTaskEntity, onDismiss: () -> Unit, onReview: (RecallRating) -> Unit) {
    var showNotes by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("复习：${task.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (task.prompt.isNotBlank()) Text("回忆提示：${task.prompt}")
                Text("先在脑中回忆，再选择你这次的状态。笔记默认隐藏。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { showNotes = !showNotes }) { Icon(if (showNotes) Icons.Default.VisibilityOff else Icons.Default.Visibility, null); Spacer(Modifier.width(6.dp)); Text(if (showNotes) "隐藏资料" else "查看资料") }
                if (showNotes) {
                    if (task.notes.isNotBlank()) Text(task.notes)
                    if (task.source.isNotBlank()) Text("来源：${task.source}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onReview(RecallRating.FORGOT) }) { Text("忘记") }
                TextButton(onClick = { onReview(RecallRating.FUZZY) }) { Text("模糊") }
                Button(onClick = { onReview(RecallRating.REMEMBERED) }) { Text("记得") }
            }
        },
        dismissButton = { TextButton(onClick = { onReview(RecallRating.SNOOZE) }) { Text("稍后") } },
    )
}

@Composable
private fun PagesDialog(plan: ReadingPlanEntity, pagesToday: Int, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pages by rememberSaveable { mutableStateOf("") }
    FormDialog("记录阅读页数", onDismiss, "保存", content = {
        Text("${plan.title} · 今天已读 $pagesToday 页")
        OutlinedTextField(pages, { pages = it }, label = { Text("本次读了多少页") }, singleLine = true)
        Text("当前页数会自动累加，最多不超过总页数。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }, onConfirm = { onSave(pages) })
}

@Composable
private fun BackupDialog(title: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: (Boolean, String) -> Unit) {
    var encrypted by rememberSaveable { mutableStateOf(true) }
    var password by rememberSaveable { mutableStateOf("") }
    var showPlainConfirm by rememberSaveable { mutableStateOf(false) }
    FormDialog(title, onDismiss, confirmLabel, content = {
        FilterChip(selected = encrypted, onClick = { encrypted = !encrypted }, label = { Text(if (encrypted) "加密备份（推荐）" else "明文备份") })
        if (encrypted) OutlinedTextField(password, { password = it }, label = { Text("密码（至少 8 位）") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Text(if (encrypted) "密码不会保存；忘记密码无法恢复备份。" else "明文备份包含全部学习记录，请妥善保管。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }, onConfirm = { if (encrypted) onConfirm(true, password) else showPlainConfirm = true })
    if (showPlainConfirm) {
        AlertDialog(
            onDismissRequest = { showPlainConfirm = false },
            title = { Text("确认导出明文备份？") },
            text = { Text("明文文件包含全部学习记录，任何拿到文件的人都可以读取。确定继续吗？") },
            confirmButton = {
                Button(onClick = { showPlainConfirm = false; onConfirm(false, "") }) { Text("继续导出") }
            },
            dismissButton = { TextButton(onClick = { showPlainConfirm = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun ImportDialog(onDismiss: () -> Unit, onConfirm: (String, BackupImportMode) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(BackupImportMode.MERGE) }
    FormDialog("导入备份", onDismiss, "选择文件", content = {
        OutlinedTextField(password, { password = it }, label = { Text("加密备份密码（明文可留空）") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Text("合并：保留本机数据并按 ID 覆盖同名记录；替换：清空本机数据后导入。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == BackupImportMode.MERGE, onClick = { mode = BackupImportMode.MERGE }, label = { Text("合并") })
            FilterChip(selected = mode == BackupImportMode.REPLACE, onClick = { mode = BackupImportMode.REPLACE }, label = { Text("替换") })
        }
    }, onConfirm = { onConfirm(password, mode) })
}

@Composable
private fun ImportPreviewDialog(
    request: PendingBackupImport,
    onDismiss: () -> Unit,
    onConfirm: (BackupImportMode) -> Unit,
) {
    var mode by rememberSaveable(request.preview.createdAt, request.preview.counts.size) { mutableStateOf(request.initialMode) }
    val total = request.preview.counts.values.sum()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认导入备份") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (request.preview.encrypted) "已通过密码解密" else "明文备份")
                Text("共 $total 条记录 · 版本 ${request.preview.schemaVersion ?: "—"}")
                request.preview.counts.filterValues { it > 0 }.forEach { (name, count) -> Text("$name：$count", fontSize = 12.sp) }
                Text("合并会保留本机数据并按 ID 覆盖；替换会清空本机数据。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == BackupImportMode.MERGE, onClick = { mode = BackupImportMode.MERGE }, label = { Text("合并") })
                    FilterChip(selected = mode == BackupImportMode.REPLACE, onClick = { mode = BackupImportMode.REPLACE }, label = { Text("替换") })
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(mode) }) { Text(if (mode == BackupImportMode.REPLACE) "清空并导入" else "合并导入") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun ReminderDialog(
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, String, String, String) -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf("SUMMARY") }
    var projectId by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("20:00") }
    var quietStart by rememberSaveable { mutableStateOf("22:00") }
    var quietEnd by rememberSaveable { mutableStateOf("07:00") }
    var repeatDays by rememberSaveable { mutableStateOf("1,2,3,4,5,6,7") }
    val selectedDays = repeatDays.split(',').mapNotNull { it.toIntOrNull() }.toSet()
    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    FormDialog("添加固定提醒", onDismiss, "保存", content = {
        ChoiceRow("提醒对象", kind, listOf("SUMMARY", "PROJECT"), { if (it == "SUMMARY") "每日进度" else "学习项目" }) {
            kind = it
            if (it == "PROJECT" && projectId.isBlank()) projectId = projects.firstOrNull()?.id.orEmpty()
        }
        if (kind == "PROJECT") ProjectPicker(projects, projectId, { projectId = it })
        OutlinedTextField(time, { time = it }, label = { Text("提醒时间 HH:MM") }, singleLine = true)
        Text("提醒日期", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1..4, 5..7).forEach { range ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    range.forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                val next = if (day in selectedDays) selectedDays - day else selectedDays + day
                                repeatDays = next.sorted().joinToString(",")
                            },
                            label = { Text(dayLabels[day - 1]) },
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(quietStart, { quietStart = it }, label = { Text("安静开始") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(quietEnd, { quietEnd = it }, label = { Text("安静结束") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        Text("安静时段内不会触发这条提醒；默认 22:00—07:00。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }, onConfirm = { onSave(if (kind == "PROJECT") projectId.takeIf(String::isNotBlank) else null, kind, time, quietStart, quietEnd, repeatDays) })
}

@Composable
private fun SimpleTimeDialog(title: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var time by rememberSaveable { mutableStateOf("20:00") }
    FormDialog(title, onDismiss, "保存", content = { OutlinedTextField(time, { time = it }, label = { Text("提醒时间 HH:MM") }, singleLine = true) }, onConfirm = { onSave(time) })
}

@Composable
private fun ChoiceRow(label: String, selected: String, options: List<String>, display: (String) -> String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) { options.forEach { option -> FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(display(option)) }) } }
    }
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, confirmLabel: String, content: @Composable ColumnScope.() -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) },
        confirmButton = { Button(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

