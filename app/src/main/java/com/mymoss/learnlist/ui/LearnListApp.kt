package com.mymoss.learnlist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.mymoss.learnlist.data.local.ReadingAdjustmentEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.DailyProgressCalculator
import com.mymoss.learnlist.domain.DailyProgressSummary
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
import com.mymoss.learnlist.system.FocusTimerService
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("今日", Icons.Default.Home),
    LEARN("学习", Icons.AutoMirrored.Filled.MenuBook),
    TODO("待办", Icons.Default.TaskAlt),
    FOCUS("专注", Icons.Default.Timer),
    STATS("统计", Icons.Default.BarChart),
    SETTINGS("设置", Icons.Default.Settings),
}

internal const val RAIL_NAVIGATION_BREAKPOINT_DP = 600

internal fun usesRailNavigation(widthDp: Int): Boolean = widthDp >= RAIL_NAVIGATION_BREAKPOINT_DP

enum class UpdatePhase {
    IDLE,
    CHECKING,
    CONNECTING,
    RESUMING,
    DOWNLOADING,
    VERIFYING,
    CERTIFICATE,
    INSTALLING,
}

data class UpdateUiState(
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val available: UpdateInfo? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val lastCheckedAtEpochMillis: Long? = null,
    val phase: UpdatePhase = UpdatePhase.IDLE,
    val downloadProgress: Float? = null,
    val downloadedBytes: Long = 0L,
    val totalDownloadBytes: Long? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnListApp(
    viewModel: LearnListViewModel,
    onExportBackup: (Boolean, String) -> Unit = { _, _ -> },
    onImportBackup: (String, BackupImportMode) -> Unit = { _, _ -> },
    onExportDiagnostics: () -> Unit = {},
    onCheckForUpdate: () -> Unit = {},
    updateState: UpdateUiState = UpdateUiState(),
    onDownloadUpdate: () -> Unit = {},
    onInstallUpdate: () -> Unit = {},
    onCancelUpdate: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
    onRequestNotifications: () -> Unit = {},
    onRequestExactAlarms: () -> Unit = {},
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true,
    focusFeedbackMode: String = "GLOBAL",
    reminderFeedbackMode: String = "GLOBAL",
    countdownFeedbackMode: String = "GLOBAL",
    feedbackAudioName: String? = null,
    reviewBatchSize: Int = 20,
    onReviewBatchSizeChange: (Int) -> Unit = {},
    focusAutoStartBreaks: Boolean = false,
    onFocusAutoStartBreaksChange: (Boolean) -> Unit = {},
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onVibrationEnabledChange: (Boolean) -> Unit = {},
    onFocusFeedbackModeChange: (String) -> Unit = {},
    onReminderFeedbackModeChange: (String) -> Unit = {},
    onCountdownFeedbackModeChange: (String) -> Unit = {},
    onChooseFeedbackAudio: () -> Unit = {},
    onPreviewFeedbackAudio: () -> Unit = {},
    onClearFeedbackAudio: () -> Unit = {},
    onboardingCompleted: Boolean? = null,
    onCompleteOnboarding: () -> Unit = {},
    pendingImport: PendingBackupImport? = null,
    onConfirmImport: (BackupImportMode) -> Unit = {},
    onCancelImport: () -> Unit = {},
    appClock: Clock = Clock.systemDefaultZone(),
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
    var editProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var editTask by remember { mutableStateOf<LearningTaskEntity?>(null) }
    var editReadingPlan by remember { mutableStateOf<ReadingPlanEntity?>(null) }
    var editTodo by remember { mutableStateOf<TodoEntity?>(null) }
    var editGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var editCountdown by remember { mutableStateOf<CountdownEntity?>(null) }
    var showReviewDialog by remember { mutableStateOf<LearningTaskEntity?>(null) }
    var showCorrectionDialog by remember { mutableStateOf<LearningTaskEntity?>(null) }
    var showPagesDialog by remember { mutableStateOf<ReadingPlanEntity?>(null) }
    var showReadingAdjustmentDialog by remember { mutableStateOf<ReadingPlanEntity?>(null) }
    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    val snackbars = remember { SnackbarHostState() }
    val deviceToday = LocalDate.now(appClock)
    var currentDay by remember { mutableStateOf(deviceToday) }
    var followsToday by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(onboardingCompleted) {
        if (onboardingCompleted == false) showOnboarding = true
    }

    LaunchedEffect(appClock) {
        while (true) {
            delay(60_000)
            if (followsToday) currentDay = LocalDate.now(appClock)
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            val result = snackbars.showSnackbar(it, actionLabel = if (viewModel.canUndoArchive) "撤销" else null)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoLastArchive()
            viewModel.clearMessage()
        }
    }

    val navigateToTab: (AppTab) -> Unit = { tab ->
        navController.navigate(tab.name) {
            popUpTo(AppTab.TODAY.name) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useRailNavigation = usesRailNavigation(maxWidth.value.roundToInt())
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp)
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
                                        label = {
                                            Text(
                                                if (!updateState.isDownloading) "有更新"
                                                else updateState.downloadProgress?.let { "${(it * 100).roundToInt()}%" } ?: "更新中",
                                            )
                                        },
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
                if (!useRailNavigation) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { navigateToTab(tab) },
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
                }
            },
        ) { padding ->
            Row(Modifier.fillMaxSize()) {
                if (useRailNavigation) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = selectedTab == tab,
                                onClick = { navigateToTab(tab) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                alwaysShowLabel = true,
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    NavHost(
                        navController = navController,
                        startDestination = AppTab.TODAY.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .fillMaxHeight(),
                    ) {
            composable(AppTab.TODAY.name) {
                TodayScreen(state, padding, currentDay, deviceToday, { followsToday = it == deviceToday; currentDay = it }, viewModel, { showReviewDialog = it }, { showCorrectionDialog = it }, { showPagesDialog = it }, { showReadingAdjustmentDialog = it }, viewModel::rebalanceReading, viewModel::adjustReadingTarget, reviewBatchSize, appClock.zone)
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
                    onInitialLearn = { taskId, date -> viewModel.initialLearn(taskId, date) },
                    onReview = { showReviewDialog = it },
                    onCorrectReview = { showCorrectionDialog = it },
                    onAdjustReading = { showReadingAdjustmentDialog = it },
                    onArchive = viewModel::archiveProject,
                    onPause = viewModel::setProjectPaused,
                    onEditProject = { editProject = it },
                    onDeleteProject = viewModel::deleteProject,
                    onEditTask = { editTask = it },
                    onDeleteTask = viewModel::deleteTask,
                    onEditReadingPlan = { editReadingPlan = it },
                    onDeleteReadingPlan = viewModel::deleteReadingPlan,
                )
            }
            composable(AppTab.TODO.name) { TodoScreen(state, padding, currentDay, viewModel::toggleTodo, { editTodo = it }, viewModel::deleteTodo) }
            composable(AppTab.FOCUS.name) {
                FocusScreen(
                    state = state,
                    padding = padding,
                    projects = state.projects,
                    tasks = state.tasks,
                    onStart = { minutes, projectId, taskId -> viewModel.startFocus(minutes, projectId, taskId) },
                    onStartPhase = viewModel::startCurrentFocusPhase,
                    onStop = viewModel::stopFocus,
                    onPause = viewModel::pauseFocus,
                    onResume = viewModel::resumeFocus,
                    onSkip = viewModel::skipFocus,
                    zoneId = appClock.zone,
                )
            }
            composable(AppTab.STATS.name) {
                StatsScreen(state, padding, currentDay, { showGoalDialog = true }, { showCountdownDialog = true }, viewModel::completeCountdown, { editGoal = it }, viewModel::deleteGoal, { editCountdown = it }, viewModel::deleteCountdown, appClock.zone, appClock)
            }
            composable(AppTab.SETTINGS.name) {
                SettingsScreen(
                    state = state,
                    padding = padding,
                    projects = state.projects,
                    onBackup = { showBackupDialog = true },
                    onImport = { showImportDialog = true },
                    onExportDiagnostics = onExportDiagnostics,
                    onCheckForUpdate = onCheckForUpdate,
                    updateState = updateState,
                    onDownloadUpdate = onDownloadUpdate,
                    onInstallUpdate = onInstallUpdate,
                    onCancelUpdate = onCancelUpdate,
                    onDismissUpdate = onDismissUpdate,
                    onRequestNotifications = onRequestNotifications,
                    onRequestExactAlarms = onRequestExactAlarms,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    focusFeedbackMode = focusFeedbackMode,
                    reminderFeedbackMode = reminderFeedbackMode,
                    countdownFeedbackMode = countdownFeedbackMode,
                    feedbackAudioName = feedbackAudioName,
                    reviewBatchSize = reviewBatchSize,
                    onReviewBatchSizeChange = onReviewBatchSizeChange,
                    focusAutoStartBreaks = focusAutoStartBreaks,
                    onFocusAutoStartBreaksChange = onFocusAutoStartBreaksChange,
                    onSoundEnabledChange = onSoundEnabledChange,
                    onVibrationEnabledChange = onVibrationEnabledChange,
                    onFocusFeedbackModeChange = onFocusFeedbackModeChange,
                    onReminderFeedbackModeChange = onReminderFeedbackModeChange,
                    onCountdownFeedbackModeChange = onCountdownFeedbackModeChange,
                    onChooseFeedbackAudio = onChooseFeedbackAudio,
                    onPreviewFeedbackAudio = onPreviewFeedbackAudio,
                    onClearFeedbackAudio = onClearFeedbackAudio,
                    onReplayOnboarding = { showOnboarding = true },
                    onNewReminder = { projectId, kind, time, quietStart, quietEnd, repeatDays, onResult ->
                        viewModel.addReminder(projectId, kind, time, quietStart, quietEnd, repeatDays, onResult)
                    },
                    onUpdateReminder = { id, projectId, kind, time, quietStart, quietEnd, repeatDays, onResult ->
                        viewModel.updateReminder(id, projectId, kind, time, quietStart, quietEnd, repeatDays, onResult)
                    },
                    onSetReminderEnabled = viewModel::setReminderEnabled,
                    onDeleteReminder = viewModel::deleteReminder,
                    restDays = state.restDays,
                    onSetRestDays = viewModel::setRestDays,
                    onRestoreProject = viewModel::restoreProject,
                    onRestoreDeletedProject = viewModel::restoreDeletedProject,
                    onPermanentlyDeleteProject = viewModel::permanentlyDeleteProject,
                    onRestoreDeletedTask = viewModel::restoreDeletedTask,
                    onPermanentlyDeleteTask = viewModel::permanentlyDeleteTask,
                    onRestoreDeletedReadingPlan = viewModel::restoreDeletedReadingPlan,
                    onPermanentlyDeleteReadingPlan = viewModel::permanentlyDeleteReadingPlan,
                    onRestoreDeletedTodo = viewModel::restoreDeletedTodo,
                    onPermanentlyDeleteTodo = viewModel::permanentlyDeleteTodo,
                    onRestoreDeletedGoal = viewModel::restoreDeletedGoal,
                    onPermanentlyDeleteGoal = viewModel::permanentlyDeleteGoal,
                    onRestoreDeletedCountdown = viewModel::restoreDeletedCountdown,
                    onPermanentlyDeleteCountdown = viewModel::permanentlyDeleteCountdown,
                    zoneId = appClock.zone,
                )
            }
            }
        }
    }
    }
}

    if (showProjectDialog) {
        ProjectDialog(onDismiss = { showProjectDialog = false }) { title, type, description, tags ->
            viewModel.addProject(title, type, description, tags) { success -> if (success) showProjectDialog = false }
        }
    }
    if (showTaskDialog) {
        TaskDialog(projects = state.projects, initialProjectId = taskProjectId, onDismiss = { showTaskDialog = false }) { projectId, title, prompt, notes, source, required ->
            viewModel.addTask(projectId, title, prompt, notes, source, required) { success -> if (success) showTaskDialog = false }
        }
    }
    if (showReadingDialog) {
        ReadingDialog(projects = state.projects, initialProjectId = readingProjectId, today = deviceToday, onDismiss = { showReadingDialog = false }) { projectId, title, total, target, deadline ->
            viewModel.addReadingPlan(projectId, title, total, target, deadline) { success -> if (success) showReadingDialog = false }
        }
    }
    if (showTodoDialog) {
        TodoDialog(onDismiss = { showTodoDialog = false }, projects = state.projects, today = deviceToday) { title, notes, required, repeat, custom, dueDate, projectId ->
            viewModel.addTodo(title, notes, required, repeat, custom, dueDate, projectId) { success -> if (success) showTodoDialog = false }
        }
    }
    showReviewDialog?.let { task ->
        ReviewDialog(task, { showReviewDialog = null }) { rating -> viewModel.review(task.id, rating, currentDay); showReviewDialog = null }
    }
    showCorrectionDialog?.let { task ->
        ReviewCorrectionDialog(task, { showCorrectionDialog = null }, today = deviceToday) { stage, nextDate, reason ->
            viewModel.correctReview(task, stage, nextDate, reason) { success -> if (success) showCorrectionDialog = null }
        }
    }
    showPagesDialog?.let { plan ->
        PagesDialog(plan, readingPagesOn(state.pageLogs, state.readingAdjustments, plan.id, currentDay), { showPagesDialog = null }) { pages ->
            viewModel.logReading(plan.id, pages, currentDay) { success -> if (success) showPagesDialog = null }
        }
    }
    showReadingAdjustmentDialog?.let { plan ->
        ReadingAdjustmentDialog(plan, { showReadingAdjustmentDialog = null }) { delta, reason ->
            viewModel.adjustReading(plan, delta, reason, currentDay) { success -> if (success) showReadingAdjustmentDialog = null }
        }
    }
    if (showGoalDialog) {
        GoalDialog(onDismiss = { showGoalDialog = false }, projects = state.projects, today = deviceToday) { title, metric, target, period, endDate, projectId ->
            viewModel.addGoal(title, metric, target, period, endDate, projectId) { success -> if (success) showGoalDialog = false }
        }
    }
    if (showCountdownDialog) {
        CountdownDialog(onDismiss = { showCountdownDialog = false }, today = deviceToday, zoneId = appClock.zone) { title, date, time, note, reminder ->
            viewModel.addCountdown(title, date, time, note, reminder) { success -> if (success) showCountdownDialog = false }
        }
    }
    if (showBackupDialog) {
        BackupDialog("导出备份", "生成备份", { showBackupDialog = false }) { encrypted, password -> onExportBackup(encrypted, password); showBackupDialog = false }
    }
    if (showImportDialog) {
        ImportDialog({ showImportDialog = false }) { password, mode -> onImportBackup(password, mode); showImportDialog = false }
    }
    pendingImport?.let { request -> ImportPreviewDialog(request, onCancelImport, onConfirmImport) }
    if (showOnboarding) {
        OnboardingDialog(
            onComplete = {
                showOnboarding = false
                onCompleteOnboarding()
            },
        )
    }
    editProject?.let { project ->
        ProjectDialog(initialProject = project, onDismiss = { editProject = null }) { title, type, description, tags ->
            viewModel.updateProject(project, title, type, description, tags) { success -> if (success) editProject = null }
        }
    }
    editTask?.let { task ->
        TaskDialog(projects = state.projects, initialProjectId = task.projectId, initialTask = task, onDismiss = { editTask = null }) { _, title, prompt, notes, source, required ->
            viewModel.updateTask(task, title, prompt, notes, source, required) { success -> if (success) editTask = null }
        }
    }
    editReadingPlan?.let { plan ->
        ReadingDialog(projects = state.projects, initialProjectId = plan.projectId, initialPlan = plan, today = deviceToday, onDismiss = { editReadingPlan = null }) { _, title, total, target, deadline ->
            viewModel.updateReadingPlan(plan, title, total, target, deadline) { success -> if (success) editReadingPlan = null }
        }
    }
    editTodo?.let { todo ->
        TodoDialog(onDismiss = { editTodo = null }, projects = state.projects, initialTodo = todo, today = deviceToday) { title, notes, required, repeat, custom, dueDate, projectId ->
            viewModel.updateTodo(todo, title, notes, required, repeat, custom, dueDate, projectId) { success -> if (success) editTodo = null }
        }
    }
    editGoal?.let { goal ->
        GoalDialog(onDismiss = { editGoal = null }, projects = state.projects, initialGoal = goal, today = deviceToday) { title, metric, target, period, endDate, projectId ->
            viewModel.updateGoal(goal, title, metric, target, period, endDate, projectId) { success -> if (success) editGoal = null }
        }
    }
    editCountdown?.let { countdown ->
        CountdownDialog(onDismiss = { editCountdown = null }, initialCountdown = countdown, today = deviceToday, zoneId = appClock.zone) { title, date, time, note, reminder ->
            viewModel.updateCountdown(countdown, title, date, time, note, reminder) { success -> if (success) editCountdown = null }
        }
    }
}

private enum class OnboardingAccent {
    TOMATO,
    TEAL,
    AMBER,
}

private data class OnboardingStep(
    val eyebrow: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: OnboardingAccent,
    val tips: List<String>,
)

private val onboardingSteps = listOf(
    OnboardingStep(
        eyebrow = "01 / 开始",
        title = "把每天的学习变成一张清单",
        description = "Learn List 会把复习、阅读、待办和专注放进同一个今日节奏里。",
        icon = Icons.Default.AutoAwesome,
        accent = OnboardingAccent.TOMATO,
        tips = listOf("先创建一个学习项目，不需要一次规划完。", "今日页会告诉你今天必须完成什么。"),
    ),
    OnboardingStep(
        eyebrow = "02 / 今日",
        title = "先看今日，再开始行动",
        description = "今日页是你的驾驶舱，完成一个必做行动，进度就会向前走。",
        icon = Icons.Default.Home,
        accent = OnboardingAccent.TEAL,
        tips = listOf("学习页：创建项目、学习任务和阅读计划。", "待办页：安排一次性或重复任务。"),
    ),
    OnboardingStep(
        eyebrow = "03 / 记忆",
        title = "让复习按遗忘曲线回来",
        description = "完成首次学习后，系统会在明天开始按 1 / 2 / 4 / 7 / 15 / 30 / 60 / 90 天安排复习。",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        accent = OnboardingAccent.AMBER,
        tips = listOf("记得、模糊、忘记会改变下一次间隔。", "需要时点击“查看资料”，平时先凭记忆回答。"),
    ),
    OnboardingStep(
        eyebrow = "04 / 专注",
        title = "用专注和提醒守住节奏",
        description = "专注页用番茄钟陪你完成一段学习；设置页可以安排固定提醒、目标和数据备份。",
        icon = Icons.Default.Timer,
        accent = OnboardingAccent.TOMATO,
        tips = listOf("离开应用后，番茄钟和提醒会继续工作。", "通知权限可以稍后在设置里开启。"),
    ),
)

@Composable
private fun OnboardingDialog(onComplete: () -> Unit) {
    var pageIndex by rememberSaveable { mutableStateOf(0) }
    val step = onboardingSteps[pageIndex]
    val scheme = MaterialTheme.colorScheme
    val accent = when (step.accent) {
        OnboardingAccent.TOMATO -> scheme.primary
        OnboardingAccent.TEAL -> scheme.secondary
        OnboardingAccent.AMBER -> scheme.tertiary
    }

    Dialog(
        onDismissRequest = onComplete,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(scheme.background, scheme.surface)))
                .padding(12.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(30.dp),
                color = scheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "LEARN / LIST",
                            color = scheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onComplete) { Text("跳过") }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), scheme.primaryContainer))),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 34.dp, y = (-38).dp)
                                    .size(150.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.12f)),
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(step.eyebrow, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.weight(1f))
                                    Surface(shape = RoundedCornerShape(50), color = scheme.surface.copy(alpha = 0.78f)) {
                                        Text("1 分钟", color = scheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(accent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(step.icon, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(34.dp))
                                }
                                Text("每天一点，长期很远", color = scheme.onSurface, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(step.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(step.description, color = scheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 21.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            step.tips.forEachIndexed { index, tip ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(accent.copy(alpha = 0.13f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("${index + 1}", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(tip, color = scheme.onSurface, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                            onboardingSteps.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .width(if (index == pageIndex) 24.dp else 7.dp)
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (index == pageIndex) accent else scheme.outlineVariant),
                                )
                            }
                        }
                        Text("${pageIndex + 1}/${onboardingSteps.size}", color = scheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pageIndex > 0) {
                            OutlinedButton(onClick = { pageIndex -= 1 }, modifier = Modifier.weight(1f)) { Text("上一步") }
                        }
                        Button(
                            onClick = { if (pageIndex == onboardingSteps.lastIndex) onComplete() else pageIndex += 1 },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (pageIndex == onboardingSteps.lastIndex) "开始使用" else "下一步")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    today: LocalDate,
    currentDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    viewModel: LearnListViewModel,
    onReview: (LearningTaskEntity) -> Unit,
    onCorrectReview: (LearningTaskEntity) -> Unit,
    onPages: (ReadingPlanEntity) -> Unit,
    onAdjustReading: (ReadingPlanEntity) -> Unit,
    onRebalance: (String) -> Unit,
    onAdjustTarget: (String, Int) -> Unit,
    reviewBatchSize: Int,
    zoneId: ZoneId,
) {
    val activeProjectIds = state.projects.filterNot(ProjectEntity::isPaused).map(ProjectEntity::id).toSet()
    val dueTasks = state.tasks
        .filter { it.projectId in activeProjectIds && it.isDueOn(today) }
        .sortedWith(
            compareByDescending<LearningTaskEntity> { task ->
                task.nextReviewDate?.let { runCatching { LocalDate.parse(it).isBefore(today) }.getOrDefault(false) } == true
            }.thenBy { task -> task.nextReviewDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today },
        )
    val readingPlans = state.readingPlans.filter { plan ->
        val started = runCatching { LocalDate.parse(plan.startDate) <= today }.getOrDefault(true)
        !plan.isPaused && plan.projectId in activeProjectIds && started && (plan.currentPage < plan.totalPages || state.pageLogs.any { it.planId == plan.id && it.localDate == today.toString() })
    }
    val todayInstanceSourceIds = state.todos.filter { it.recurrenceSourceId != null && it.dueDate == today.toString() }.mapNotNull(TodoEntity::recurrenceSourceId).toSet()
    val dueTodos = state.todos.filter { todo ->
        todo.id !in todayInstanceSourceIds &&
            (todo.projectId == null || todo.projectId in activeProjectIds) &&
            todo.isDueOn(today)
    }
    val dailyProgressInput = DailyProgressMapper.from(
        projects = state.projects + state.archivedProjects,
        tasks = state.tasks,
        reviewLogs = state.reviewLogs,
        readingPlans = state.readingPlans,
        readingTargets = state.readingTargets,
        pageLogs = state.pageLogs,
        readingAdjustments = state.readingAdjustments,
        todos = state.todos,
        zoneId = zoneId,
    )
    val dailyProgressCalculator = remember { DailyProgressCalculator() }
    var missedTodoPrompt by remember { mutableStateOf<TodoEntity?>(null) }
    val promptedMissedTodoIds = remember { mutableSetOf<String>() }
    val canPromptMissedTodo = shouldPromptMissedTodo(
        selectedDate = today,
        currentDate = currentDate,
        isRestDay = today.dayOfWeek in state.restDays,
        hasEligibleProject = true,
        hasMissedOccurrence = true,
    )
    LaunchedEffect(state.todos, state.projects, today, currentDate, state.restDays) {
        if (!canPromptMissedTodo) {
            missedTodoPrompt = null
        } else if (missedTodoPrompt == null) {
            state.todos.firstOrNull {
                it.id !in promptedMissedTodoIds &&
                    it.id !in todayInstanceSourceIds &&
                    (it.projectId == null || it.projectId in activeProjectIds) &&
                    shouldPromptMissedTodo(
                        selectedDate = today,
                        currentDate = currentDate,
                        isRestDay = false,
                        hasEligibleProject = true,
                        hasMissedOccurrence = it.previousMissedOccurrence(today) != null,
                    )
            }?.let {
                promptedMissedTodoIds += it.id
                missedTodoPrompt = it
            }
        }
    }
    val progress = dailyProgressCalculator.calculate(dailyProgressInput, today)
    val streak = calculateStreak(state, today, state.restDays, zoneId)
    val readingPages = readingPagesOn(state.pageLogs, state.readingAdjustments, date = today)
    val todoDone = dueTodos.count { it.isCompletedOn(today) }
    val focusMinutes = state.focusSessions.filter { it.activityDate(zoneId) == today }.sumOf { it.actualSeconds / 60 }
    val percent = progress.percent
    var showHistoryCalendar by rememberSaveable { mutableStateOf(false) }
    val mainScope = rememberCoroutineScope()
    var requiredActionsExpanded by rememberSaveable { mutableStateOf(true) }
    var readingExpanded by rememberSaveable { mutableStateOf(true) }
    var todoExpanded by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp, padding.calculateTopPadding() + 4.dp, 20.dp, padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDateChange(today.minusDays(1)) },
                    enabled = today.isAfter(LocalDate.MIN.plusDays(1)),
                    modifier = Modifier.size(34.dp),
                ) { Icon(Icons.Default.ChevronLeft, "查看前一天") }
                Column(Modifier.weight(1f)) {
                    Text(today.format(DateTimeFormatter.ofPattern("M月d日 · E", Locale.CHINA)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text(if (today == currentDate) "今天" else "历史回看", style = MaterialTheme.typography.headlineSmall)
                }
                IconButton(
                    onClick = { mainScope.launch(Dispatchers.Main.immediate) { showHistoryCalendar = true } },
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(Icons.Default.CalendarToday, "打开学习日历")
                }
                IconButton(onClick = { if (today.isBefore(currentDate)) onDateChange(today.plusDays(1)) }, enabled = today.isBefore(currentDate), modifier = Modifier.size(34.dp)) { Icon(Icons.Default.ChevronRight, "查看后一天") }
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
        item { CollapsibleSectionHeader("今天先做这些", "建议先完成 $reviewBatchSize 项；所有逾期复习都会列出", requiredActionsExpanded) { requiredActionsExpanded = it } }
        if (requiredActionsExpanded) {
            if (dueTasks.isEmpty()) item { EmptyCard("没有积压复习。去学习页添加一个新任务吧。", Icons.Default.AutoAwesome) }
            items(dueTasks, key = { it.id }) { task ->
                ReviewTaskCard(task, { onReview(task) }, { viewModel.initialLearn(task.id, today) }, compact = true, onCorrect = { onCorrectReview(task) })
            }
        }
        item { CollapsibleSectionHeader("阅读进度", "今天达标就算完成一项", readingExpanded) { readingExpanded = it } }
        if (readingExpanded) {
            if (readingPlans.isEmpty()) item { EmptyCard("还没有进行中的阅读计划。", Icons.AutoMirrored.Filled.MenuBook) }
            items(readingPlans, key = { it.id }) { plan ->
                ReadingPlanCard(
                    plan = plan,
                    pagesToday = readingPagesOn(state.pageLogs, state.readingAdjustments, plan.id, today),
                    targetPages = state.readingTargets.targetFor(plan.id, today, plan.dailyTarget),
                    onLog = { onPages(plan) },
                    onAdjust = { onAdjustReading(plan) },
                    onRebalance = { onRebalance(plan.id) },
                    onAdjustTarget = { onAdjustTarget(plan.id, it) },
                )
            }
        }
        item { CollapsibleSectionHeader("今日待办", "重复规则会自动带到正确的日期", todoExpanded) { todoExpanded = it } }
        if (todoExpanded) {
            if (dueTodos.isEmpty()) item { EmptyCard("今天没有到期待办，给自己留一点空间。", Icons.Default.CheckCircleOutline) }
            items(dueTodos, key = { it.id }) { todo ->
                TodoCard(todo, today, onToggle = { viewModel.toggleTodo(todo.id, today, todo.isCompletedOn(today)) })
            }
        }
    }
    if (showHistoryCalendar) {
        HistoryCalendarDialog(
            selectedDate = today,
            currentDate = currentDate,
            progressFor = { date -> dailyProgressCalculator.calculate(dailyProgressInput, date) },
            onDismiss = { mainScope.launch(Dispatchers.Main.immediate) { showHistoryCalendar = false } },
            onDateSelected = { date ->
                mainScope.launch(Dispatchers.Main.immediate) {
                    onDateChange(date)
                    showHistoryCalendar = false
                }
            },
        )
    }
    if (canPromptMissedTodo) missedTodoPrompt?.takeIf { it.projectId == null || it.projectId in activeProjectIds }?.let { todo ->
        val missedDate = todo.previousMissedOccurrence(today)
        AlertDialog(
            onDismissRequest = { missedTodoPrompt = null },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("有一项重复待办漏做了") },
            text = { Text("“${todo.title}”在 ${missedDate ?: "之前"} 没有完成。要把它作为今天的一次性待办处理吗？") },
            confirmButton = {
                Button(onClick = { viewModel.createTodoInstanceForToday(todo.id); missedTodoPrompt = null }) { Text("今天处理") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { viewModel.setTodoMissedPromptPolicy(todo.id, "NEVER"); missedTodoPrompt = null }) { Text("不再询问") }
                    TextButton(onClick = { missedTodoPrompt = null }) { Text("稍后") }
                }
            },
        )
    }
}

@Composable
private fun HistoryCalendarDialog(
    selectedDate: LocalDate,
    currentDate: LocalDate,
    progressFor: (LocalDate) -> DailyProgressSummary,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    var month by rememberSaveable(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    val currentMonth = YearMonth.from(currentDate)
    val today = currentDate
    val leadingEmptyDays = month.atDay(1).dayOfWeek.value - 1
    val rowCount = (leadingEmptyDays + month.lengthOfMonth() + 6) / 7
    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("学习日历", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { month = month.minusMonths(1) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ChevronLeft, "上一个月")
                    }
                    Text("${month.year}年${month.monthValue}月", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { month = month.plusMonths(1) }, enabled = month.isBefore(currentMonth), modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ChevronRight, "下一个月")
                    }
                }
                Text("点选日期查看当天进度；未来日期不可补记。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(Modifier.fillMaxWidth()) {
                    weekdays.forEach { weekday ->
                        Text(weekday, Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                repeat(rowCount) { row ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { column ->
                            val dayOfMonth = row * 7 + column - leadingEmptyDays + 1
                            if (dayOfMonth !in 1..month.lengthOfMonth()) {
                                Spacer(Modifier.weight(1f).height(56.dp))
                            } else {
                                val date = month.atDay(dayOfMonth)
                                val isFuture = date.isAfter(today)
                                val progress = if (isFuture) null else progressFor(date)
                                val isSelected = date == selectedDate
                                val accessibilityDescription = buildString {
                                    append("${date.year}年${date.monthValue}月${date.dayOfMonth}日")
                                    append("，")
                                    when {
                                        isFuture -> append("未来日期，不可选择")
                                        progress?.percent != null -> append("完成${progress.percent}%")
                                        else -> append("无必做行动")
                                    }
                                    if (isSelected) append("，已选中")
                                }
                                val background = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    progress?.percent == 100 -> MaterialTheme.colorScheme.secondaryContainer
                                    progress?.percent != null -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .padding(2.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(background)
                                        .clickable(enabled = !isFuture) { onDateSelected(date) }
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = accessibilityDescription
                                            role = Role.Button
                                            if (isSelected) stateDescription = "已选中"
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            dayOfMonth.toString(),
                                            color = if (isFuture) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                        Text(
                                            progress?.percent?.let { "$it%" } ?: if (isFuture) "·" else "—",
                                            color = when {
                                                progress?.percent == 100 -> MaterialTheme.colorScheme.secondary
                                                progress?.percent != null -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.outline
                                            },
                                            fontSize = 9.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected(today) }) { Text("回到今天") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
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
    onInitialLearn: (String, LocalDate) -> Unit,
    onReview: (LearningTaskEntity) -> Unit,
    onCorrectReview: (LearningTaskEntity) -> Unit,
    onAdjustReading: (ReadingPlanEntity) -> Unit,
    onArchive: (String) -> Unit,
    onPause: (String, Boolean) -> Unit,
    onEditProject: (ProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit,
    onEditTask: (LearningTaskEntity) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditReadingPlan: (ReadingPlanEntity) -> Unit,
    onDeleteReadingPlan: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var projectsExpanded by rememberSaveable { mutableStateOf(true) }
    val normalizedQuery = query.trim().lowercase()
    val visibleProjects = state.projects.filter { project ->
        normalizedQuery.isBlank() || listOf(project.title, project.type, project.description, project.tagCsv).any { it.lowercase().contains(normalizedQuery) } ||
            state.tasks.any { it.projectId == project.id && it.title.lowercase().contains(normalizedQuery) } ||
            state.readingPlans.any { it.projectId == project.id && it.title.lowercase().contains(normalizedQuery) }
    }
    LaunchedEffect(normalizedQuery) {
        if (normalizedQuery.isNotBlank()) projectsExpanded = true
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
            CollapsibleSectionHeader(
                "学习项目",
                "${visibleProjects.size} 个项目 · 逾期内容不会隐藏",
                projectsExpanded,
            ) { projectsExpanded = it }
        }
        if (projectsExpanded && visibleProjects.isEmpty()) item { EmptyCard(if (state.projects.isEmpty()) "创建第一个学习项目：书籍、课程或技能" else "没有匹配的学习项目", Icons.Default.Search) }
        items(if (projectsExpanded) visibleProjects else emptyList(), key = { it.id }) { project ->
            ProjectCard(
                project = project,
                tasks = state.tasks.filter { it.projectId == project.id },
                plans = state.readingPlans.filter { it.projectId == project.id },
                pageLogs = state.pageLogs,
                readingAdjustments = state.readingAdjustments,
                readingTargets = state.readingTargets,
                today = today,
                onNewTask = { onNewTask(project.id) },
                onNewReading = { onNewReading(project.id) },
                onPages = onPages,
                onRebalance = onRebalance,
                onAdjustTarget = onAdjustTarget,
                onInitialLearn = onInitialLearn,
                onReview = onReview,
                onCorrectReview = onCorrectReview,
                onAdjustReading = onAdjustReading,
                onArchive = { onArchive(project.id) },
                onPause = { onPause(project.id, !project.isPaused) },
                onEdit = { onEditProject(project) },
                onDelete = { onDeleteProject(project.id) },
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onEditReadingPlan = onEditReadingPlan,
                onDeleteReadingPlan = onDeleteReadingPlan,
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
    readingAdjustments: List<ReadingAdjustmentEntity>,
    readingTargets: List<ReadingTargetEntity>,
    today: LocalDate,
    onNewTask: () -> Unit,
    onNewReading: () -> Unit,
    onPages: (ReadingPlanEntity) -> Unit,
    onRebalance: (String) -> Unit,
    onAdjustTarget: (String, Int) -> Unit,
    onInitialLearn: (String, LocalDate) -> Unit,
    onReview: (LearningTaskEntity) -> Unit,
    onCorrectReview: (LearningTaskEntity) -> Unit,
    onAdjustReading: (ReadingPlanEntity) -> Unit,
    onArchive: () -> Unit,
    onPause: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditTask: (LearningTaskEntity) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditReadingPlan: (ReadingPlanEntity) -> Unit,
    onDeleteReadingPlan: (String) -> Unit,
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
                        DropdownMenuItem(
                            text = { Text("编辑项目") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { menuExpanded = false; onEdit() },
                        )
                        DropdownMenuItem(
                            text = { Text("移入回收站") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { menuExpanded = false; onDelete() },
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
                        pagesToday = readingPagesOn(pageLogs, readingAdjustments, plan.id, today),
                        targetPages = readingTargets.targetFor(plan.id, today, plan.dailyTarget),
                        onLog = { onPages(plan) },
                        onAdjust = { onAdjustReading(plan) },
                        onRebalance = { onRebalance(plan.id) },
                        onAdjustTarget = { onAdjustTarget(plan.id, it) },
                        onEdit = { onEditReadingPlan(plan) },
                        onDelete = { onDeleteReadingPlan(plan.id) },
                    )
                }
                tasks.forEach { task ->
                    if (!project.isPaused && (task.isDueOn(today) || !task.hasLearned)) {
                        ReviewTaskCard(task, { onReview(task) }, { onInitialLearn(task.id, today) }, onEdit = { onEditTask(task) }, onDelete = { onDeleteTask(task.id) }, onCorrect = { onCorrectReview(task) })
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
    onEdit: (TodoEntity) -> Unit,
    onDelete: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val activeProjectIds = state.projects.filterNot(ProjectEntity::isPaused).map(ProjectEntity::id).toSet()
    val instanceSourceIds = state.todos.filter { it.recurrenceSourceId != null && it.dueDate == today.toString() }.mapNotNull(TodoEntity::recurrenceSourceId).toSet()
    val todos = state.todos.filter { todo ->
        todo.id !in instanceSourceIds &&
            (todo.projectId == null || todo.projectId in activeProjectIds) &&
            todo.isDueOn(today) &&
            (query.isBlank() || todo.title.contains(query.trim(), true) || todo.notes.contains(query.trim(), true))
    }
    val done = todos.count { it.isCompletedOn(today) }
    val pendingTodos = todos.filterNot { it.isCompletedOn(today) }
    val completedTodos = todos.filter { it.isCompletedOn(today) }
    var pendingExpanded by rememberSaveable { mutableStateOf(true) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(query.trim()) {
        if (query.trim().isNotBlank()) {
            pendingExpanded = true
            completedExpanded = true
        }
    }
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
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${todos.size} 项安排", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("待完成 $pendingTodos.size · 已完成 $done", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        item { CollapsibleSectionHeader("待完成", if (pendingTodos.isEmpty()) "今天先留一点空间" else "优先处理这 $pendingTodos.size 项", pendingExpanded) { pendingExpanded = it } }
        if (pendingExpanded) {
            if (pendingTodos.isEmpty()) item { EmptyCard(if (todos.isEmpty()) "没有到期待办，点击右下角添加。" else "今天的待办都完成了。", Icons.Default.CheckCircleOutline) }
            items(pendingTodos, key = { it.id }) { todo ->
                TodoCard(
                    todo,
                    today,
                    onToggle = {
                        onToggle(todo.id, today, todo.isCompletedOn(today))
                        completedExpanded = true
                    },
                    onEdit = { onEdit(todo) },
                    onDelete = { onDelete(todo.id) },
                )
            }
        }
        item { CollapsibleSectionHeader("已完成", if (completedTodos.isEmpty()) "完成后会收进这里" else "今天完成了 $done 项", completedExpanded) { completedExpanded = it } }
        if (completedExpanded) {
            if (completedTodos.isEmpty()) item { EmptyCard("完成的待办会显示在这里。", Icons.Default.CheckCircleOutline) }
            items(completedTodos, key = { it.id }) { todo ->
                TodoCard(
                    todo,
                    today,
                    onToggle = {
                        onToggle(todo.id, today, todo.isCompletedOn(today))
                        pendingExpanded = true
                    },
                    onEdit = { onEdit(todo) },
                    onDelete = { onDelete(todo.id) },
                )
            }
        }
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
private fun FocusScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    projects: List<ProjectEntity>,
    tasks: List<LearningTaskEntity>,
    onStart: (Int, String?, String?) -> Unit,
    onStartPhase: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    zoneId: ZoneId,
) {
    val minutes = state.focusRemainingSeconds / 60
    val seconds = state.focusRemainingSeconds % 60
    var customMinutes by rememberSaveable { mutableStateOf("25") }
    var selectedProjectId by rememberSaveable { mutableStateOf(state.focusProjectId.orEmpty()) }
    var selectedTaskId by rememberSaveable { mutableStateOf(state.focusTaskId.orEmpty()) }
    val selectableTasks = tasks.filter { task -> selectedProjectId.isBlank() || task.projectId == selectedProjectId }
    val phaseActive = state.focusRunning || state.focusPaused
    LaunchedEffect(state.focusProjectId, state.focusTaskId, phaseActive) {
        if (!phaseActive) {
            selectedProjectId = state.focusProjectId.orEmpty()
            selectedTaskId = state.focusTaskId.orEmpty()
        }
    }
    LaunchedEffect(selectedProjectId) {
        if (selectedTaskId.isNotBlank() && selectableTasks.none { it.id == selectedTaskId }) selectedTaskId = ""
    }
    val progress = if (phaseActive && state.focusPlannedMinutes > 0) state.focusRemainingSeconds / (state.focusPlannedMinutes * 60f) else 0f
    var recentFocusExpanded by rememberSaveable { mutableStateOf(true) }
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
                            Text(if (state.focusRunning) phaseLabel(state.focusPhase) else if (state.focusPaused) "${phaseLabel(state.focusPhase)}已暂停" else if (state.focusPhase != "WORK") "${phaseLabel(state.focusPhase)}待开始" else "专注工作台", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (state.focusRunning) "第 ${state.focusRound} 轮 · 让这一段时间只属于一件事" else if (state.focusPaused) "准备好后继续这一段专注" else if (state.focusPhase != "WORK") "可以继续上一轮的节奏，也可以开始新的专注" else "选择一段不被打扰的时间", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        Icon(Icons.Default.Timer, null, tint = Color.White.copy(alpha = 0.86f), modifier = Modifier.size(32.dp))
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(208.dp)) {
                        CircularProgressIndicator(progress = { if (phaseActive) progress else 0f }, modifier = Modifier.fillMaxSize(), color = Color.White, trackColor = Color.White.copy(alpha = 0.2f), strokeWidth = 12.dp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (phaseActive) "%02d:%02d".format(minutes, seconds) else if (state.focusPhase == "WORK") "25:00" else FocusTimerService.formatRemaining(state.focusRemainingSeconds), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            Text(if (phaseActive) "剩余时间" else "番茄钟", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                    }
                    if (state.focusRunning) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onPause, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary)) {
                                Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("暂停")
                            }
                            OutlinedButton(onClick = onStop, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                                Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("结束并保存")
                            }
                        }
                        TextButton(onClick = onSkip, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("跳过这一阶段") }
                    } else if (state.focusPaused) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onResume, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary)) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("继续")
                            }
                            OutlinedButton(onClick = onStop, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                                Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("结束并保存")
                            }
                        }
                        TextButton(onClick = onSkip, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("跳过这一阶段") }
                    } else {
                        FocusBindingPicker("关联项目（可选）", listOf("" to "不关联") + projects.map { it.id to it.title }, selectedProjectId) { selectedProjectId = it }
                        FocusBindingPicker("关联任务（可选）", listOf("" to "不关联") + selectableTasks.map { it.id to it.title }, selectedTaskId) { selectedTaskId = it }
                        Button(
                            onClick = onStartPhase,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary),
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("开始当前${phaseLabel(state.focusPhase)}")
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf(25, 50, 90).forEach { preset ->
                                FilterChip(selected = false, onClick = { onStart(preset, selectedProjectId.takeIf(String::isNotBlank), selectedTaskId.takeIf(String::isNotBlank)) }, label = { Text("${preset} 分") }, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)), colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(labelColor = Color.White, iconColor = Color.White))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(customMinutes, { customMinutes = it.filter(Char::isDigit).take(3) }, Modifier.weight(1f), label = { Text("自定义分钟") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Button(onClick = { customMinutes.toIntOrNull()?.let { onStart(it, selectedProjectId.takeIf(String::isNotBlank), selectedTaskId.takeIf(String::isNotBlank)) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.secondary)) { Text("开始") }
                        }
                        Text("可设置 1–180 分钟 · 离开应用后会自动恢复", color = Color.White.copy(alpha = 0.74f), fontSize = 12.sp)
                    }
                }
            }
        }
        item { CollapsibleSectionHeader("最近专注", "完成后自动计入统计", recentFocusExpanded) { recentFocusExpanded = it } }
        if (recentFocusExpanded) {
            if (state.focusSessions.isEmpty()) item { EmptyCard("完成第一段番茄钟后，这里会出现你的专注记录。", Icons.Default.Timer) }
            items(state.focusSessions.take(20), key = { it.id }) { session ->
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(19.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text("${session.actualMinutes} 分钟专注", fontWeight = FontWeight.SemiBold); Text(session.startedAt.toLocalDate(zoneId).toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                        Text(session.status, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    }
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
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onEditCountdown: (CountdownEntity) -> Unit,
    onDeleteCountdown: (String) -> Unit,
    zoneId: ZoneId,
    clock: Clock,
) {
    var heatMapExpanded by rememberSaveable { mutableStateOf(true) }
    var trendExpanded by rememberSaveable { mutableStateOf(true) }
    var goalsExpanded by rememberSaveable { mutableStateOf(true) }
    var countdownExpanded by rememberSaveable { mutableStateOf(true) }
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
                MetricCard("阅读总页", (state.pageLogs.sumOf(PageLogEntity::pagesRead) + state.readingAdjustments.sumOf(ReadingAdjustmentEntity::deltaPages)).coerceAtLeast(0).toString(), Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                MetricCard("专注分钟", state.focusSessions.sumOf(FocusSessionEntity::actualMinutes).toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CollapsibleSectionHeader("最近 28 天", "按单位分别查看复习、阅读、专注和待办", heatMapExpanded) { heatMapExpanded = it }
                if (heatMapExpanded) Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.padding(16.dp)) { MetricHeatMap(state, today, zoneId) } }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CollapsibleSectionHeader("最近 7 天", "每条趋势保持自己的计量单位", trendExpanded) { trendExpanded = it }
                if (trendExpanded) Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Column(Modifier.padding(16.dp)) { MetricTrendChart(state, today, zoneId) } }
            }
        }
        item {
            CollapsibleSectionHeader(
                text = "量化目标",
                subtitle = "给想坚持的事一个可见的终点",
                expanded = goalsExpanded,
                onExpandedChange = { goalsExpanded = it },
                trailing = { TextButton(onClick = onNewGoal) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Text("新增") } },
            )
        }
        if (goalsExpanded) {
            if (state.goals.isEmpty()) item { EmptyCard("例如：每天专注 50 分钟、每周复习 20 项。", Icons.Default.Flag) }
            items(state.goals, key = { it.id }) { goal -> GoalCard(goal, state, today, zoneId, { onEditGoal(goal) }, { onDeleteGoal(goal.id) }) }
        }
        item {
            CollapsibleSectionHeader(
                text = "倒计时",
                subtitle = "考试、截止日或下一次重要事件",
                expanded = countdownExpanded,
                onExpandedChange = { countdownExpanded = it },
                trailing = { TextButton(onClick = onNewCountdown) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Text("新增") } },
            )
        }
        if (countdownExpanded) {
            if (state.countdowns.isEmpty()) item { EmptyCard("为重要事件留一个提前量。", Icons.Default.CalendarToday) }
            items(state.countdowns, key = { it.id }) { countdown -> CountdownCard(countdown, { onCompleteCountdown(countdown.id) }, { onEditCountdown(countdown) }, { onDeleteCountdown(countdown.id) }, clock) }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: LearnListUiState,
    padding: PaddingValues,
    projects: List<ProjectEntity>,
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onExportDiagnostics: () -> Unit,
    onCheckForUpdate: () -> Unit,
    updateState: UpdateUiState,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onCancelUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    focusFeedbackMode: String,
    reminderFeedbackMode: String,
    countdownFeedbackMode: String,
    feedbackAudioName: String?,
    reviewBatchSize: Int,
    onReviewBatchSizeChange: (Int) -> Unit,
    focusAutoStartBreaks: Boolean,
    onFocusAutoStartBreaksChange: (Boolean) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onFocusFeedbackModeChange: (String) -> Unit,
    onReminderFeedbackModeChange: (String) -> Unit,
    onCountdownFeedbackModeChange: (String) -> Unit,
    onChooseFeedbackAudio: () -> Unit,
    onPreviewFeedbackAudio: () -> Unit,
    onClearFeedbackAudio: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onNewReminder: (String?, String, String, String, String, String, (Boolean) -> Unit) -> Unit,
    onUpdateReminder: (String, String?, String, String, String, String, String, (Boolean) -> Unit) -> Unit,
    onSetReminderEnabled: (String, Boolean) -> Unit,
    onDeleteReminder: (String) -> Unit,
    restDays: Set<DayOfWeek>,
    onSetRestDays: (Set<DayOfWeek>) -> Unit,
    onRestoreProject: (String) -> Unit,
    onRestoreDeletedProject: (String) -> Unit,
    onPermanentlyDeleteProject: (String) -> Unit,
    onRestoreDeletedTask: (String) -> Unit,
    onPermanentlyDeleteTask: (String) -> Unit,
    onRestoreDeletedReadingPlan: (String) -> Unit,
    onPermanentlyDeleteReadingPlan: (String) -> Unit,
    onRestoreDeletedTodo: (String) -> Unit,
    onPermanentlyDeleteTodo: (String) -> Unit,
    onRestoreDeletedGoal: (String) -> Unit,
    onPermanentlyDeleteGoal: (String) -> Unit,
    onRestoreDeletedCountdown: (String) -> Unit,
    onPermanentlyDeleteCountdown: (String) -> Unit,
    zoneId: ZoneId,
) {
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    var reminderToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var reminderToDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var firstUseExpanded by rememberSaveable { mutableStateOf(true) }
    var updateExpanded by rememberSaveable { mutableStateOf(true) }
    var reviewExpanded by rememberSaveable { mutableStateOf(false) }
    var remindersExpanded by rememberSaveable { mutableStateOf(true) }
    var feedbackExpanded by rememberSaveable { mutableStateOf(true) }
    var focusExpanded by rememberSaveable { mutableStateOf(false) }
    var safetyExpanded by rememberSaveable { mutableStateOf(true) }
    var streakExpanded by rememberSaveable { mutableStateOf(false) }
    var archivedExpanded by rememberSaveable { mutableStateOf(false) }
    var recycleExpanded by rememberSaveable { mutableStateOf(false) }
    var permanentDeleteTitle by remember { mutableStateOf<String?>(null) }
    var permanentDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var permanentDeleteAcknowledged by rememberSaveable { mutableStateOf(false) }
    val reminderToEdit = reminderToEditId?.let { id -> state.reminders.firstOrNull { it.id == id } }

    fun requestPermanentDelete(title: String, action: () -> Unit) {
        permanentDeleteTitle = title
        permanentDeleteAction = action
        permanentDeleteAcknowledged = false
    }

    fun dismissPermanentDelete() {
        permanentDeleteAction = null
        permanentDeleteTitle = null
        permanentDeleteAcknowledged = false
    }

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
        item { CollapsibleSectionHeader("第一次使用", "随时回看操作路线", firstUseExpanded) { firstUseExpanded = it } }
        if (firstUseExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("不确定从哪里开始？", fontWeight = FontWeight.Bold)
                        Text("用一分钟重新熟悉今日、学习、专注和提醒。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    TextButton(onClick = onReplayOnboarding) {
                        Text("重新查看使用引导")
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
        item { CollapsibleSectionHeader("更新中心", "每 24 小时自动检查一次，也可以现在手动检查", updateExpanded) { updateExpanded = it } }
        if (updateExpanded) item {
            UpdateCenterCard(updateState, onCheckForUpdate, onDownloadUpdate, onInstallUpdate, onCancelUpdate, onDismissUpdate, zoneId)
        }
        item { CollapsibleSectionHeader("复习节奏", "建议批次只影响提示，不会隐藏逾期内容", reviewExpanded) { reviewExpanded = it } }
        if (reviewExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("每日建议先完成多少项", fontWeight = FontWeight.Bold)
                    Text("当前建议 $reviewBatchSize 项。复习队列仍会完整展示，适合按精力调整。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10, 20, 30, 50).forEach { value ->
                            FilterChip(selected = reviewBatchSize == value, onClick = { onReviewBatchSizeChange(value) }, label = { Text("$value 项") })
                        }
                    }
                }
            }
        }
        item { CollapsibleSectionHeader("固定提醒", "在你习惯的时间，把今天拉回眼前", remindersExpanded) { remindersExpanded = it } }
        if (remindersExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text("每日进度和项目提醒", fontWeight = FontWeight.Bold); Text("支持多个固定时间、星期选择和安静时段", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { reminderToEditId = null; showReminderDialog = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("添加提醒") }
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
                                IconButton(onClick = { reminderToEditId = reminder.id; showReminderDialog = true }) {
                                    Icon(Icons.Default.Edit, "编辑提醒")
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
        item { CollapsibleSectionHeader("提醒反馈", "专注完成、固定提醒和倒计时提醒", feedbackExpanded) { feedbackExpanded = it } }
        if (feedbackExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("按你的场景提醒", fontWeight = FontWeight.Bold)
                            Text("可同时开启，也可以全部关闭", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        TagPill(feedbackModeLabel(soundEnabled, vibrationEnabled), MaterialTheme.colorScheme.secondary)
                    }
                    FeedbackToggleRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "声音提示",
                        subtitle = "使用系统通知提示音",
                        checked = soundEnabled,
                        onCheckedChange = onSoundEnabledChange,
                    )
                    FeedbackToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "振动提示",
                        subtitle = "使用两次短振，适合不方便开声音时",
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationEnabledChange,
                    )
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("提示音", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(feedbackAudioName ?: "当前使用系统通知音效", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                TextButton(onClick = onPreviewFeedbackAudio) { Text("试听") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = onChooseFeedbackAudio, modifier = Modifier.weight(1f)) { Text("导入本地音效") }
                                if (feedbackAudioName != null) {
                                    TextButton(onClick = onClearFeedbackAudio, modifier = Modifier.weight(1f)) { Text("恢复系统音效") }
                                }
                            }
                            Text("音频会复制到应用私有目录；文件缺失或无法播放时自动回退到系统音效。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Text("以上是全局默认；下面可以按场景覆盖。手机的静音、勿扰模式或系统通知设置仍可能抑制反馈。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    FeedbackModeChoiceRow("番茄阶段结束", focusFeedbackMode, onFocusFeedbackModeChange)
                    FeedbackModeChoiceRow("固定提醒", reminderFeedbackMode, onReminderFeedbackModeChange)
                    FeedbackModeChoiceRow("倒计时提醒", countdownFeedbackMode, onCountdownFeedbackModeChange)
                }
            }
        }
        item { CollapsibleSectionHeader("番茄循环", "默认 25 分钟专注 · 5 分钟短休 · 4 轮后 15 分钟长休", focusExpanded) { focusExpanded = it } }
        if (focusExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackToggleRow(
                        icon = Icons.Default.Timer,
                        title = "阶段结束后自动开始下一段",
                        subtitle = if (focusAutoStartBreaks) "专注、短休和长休会自动衔接" else "每一段结束后停下来，由你决定是否继续",
                        checked = focusAutoStartBreaks,
                        onCheckedChange = onFocusAutoStartBreaksChange,
                    )
                    Text("暂停、跳过和提前结束都不会自动算作完整专注；只有工作阶段会进入专注统计。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
        item { CollapsibleSectionHeader("数据安全", "备份、迁移和恢复都由你掌握", safetyExpanded) { safetyExpanded = it } }
        if (safetyExpanded) item {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("加密备份是默认选择。明文导出会在确认后执行，导入前可预览并选择合并或替换。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onBackup, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("导出") }
                        OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("导入") }
                    }
                    OutlinedButton(onClick = onExportDiagnostics, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.BugReport, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("导出脱敏诊断")
                    }
                    Text("诊断文件只包含版本、设备环境、记录数量和开关状态，不包含标题、笔记、来源、ID、路径或音频。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
        item { CollapsibleSectionHeader("连续打卡", "休息日不会打断你的节奏", streakExpanded) { streakExpanded = it } }
        if (streakExpanded) item {
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
            item { CollapsibleSectionHeader("已归档项目", "需要时可以恢复", archivedExpanded) { archivedExpanded = it } }
            if (archivedExpanded) items(state.archivedProjects, key = { "archived-${it.id}" }) { project ->
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(project.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { onRestoreProject(project.id) }) { Text("恢复"); Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(17.dp)) }
                    }
                }
            }
        }
        if (state.deletedProjects.isNotEmpty() || state.deletedTasks.isNotEmpty() || state.deletedReadingPlans.isNotEmpty() || state.deletedTodos.isNotEmpty() || state.deletedGoals.isNotEmpty() || state.deletedCountdowns.isNotEmpty()) {
            item { CollapsibleSectionHeader("回收站", "移入后可恢复；永久删除不可撤销", recycleExpanded) { recycleExpanded = it } }
            if (recycleExpanded) item {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.deletedProjects.forEach { item -> DeletedRow("项目 · ${item.title}", { onRestoreDeletedProject(item.id) }, { requestPermanentDelete("项目 · ${item.title}") { onPermanentlyDeleteProject(item.id) } }) }
                        state.deletedTasks.forEach { item -> DeletedRow("任务 · ${item.title}", { onRestoreDeletedTask(item.id) }, { requestPermanentDelete("任务 · ${item.title}") { onPermanentlyDeleteTask(item.id) } }) }
                        state.deletedReadingPlans.forEach { item -> DeletedRow("阅读 · ${item.title}", { onRestoreDeletedReadingPlan(item.id) }, { requestPermanentDelete("阅读 · ${item.title}") { onPermanentlyDeleteReadingPlan(item.id) } }) }
                        state.deletedTodos.forEach { item -> DeletedRow("待办 · ${item.title}", { onRestoreDeletedTodo(item.id) }, { requestPermanentDelete("待办 · ${item.title}") { onPermanentlyDeleteTodo(item.id) } }) }
                        state.deletedGoals.forEach { item -> DeletedRow("目标 · ${item.title}", { onRestoreDeletedGoal(item.id) }, { requestPermanentDelete("目标 · ${item.title}") { onPermanentlyDeleteGoal(item.id) } }) }
                        state.deletedCountdowns.forEach { item -> DeletedRow("倒计时 · ${item.title}", { onRestoreDeletedCountdown(item.id) }, { requestPermanentDelete("倒计时 · ${item.title}") { onPermanentlyDeleteCountdown(item.id) } }) }
                    }
                }
            }
        }
    }
    permanentDeleteAction?.let { action ->
        AlertDialog(
            onDismissRequest = ::dismissPermanentDelete,
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("永久删除？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${permanentDeleteTitle.orEmpty()} 将连同相关历史记录一起删除，且无法恢复。")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = permanentDeleteAcknowledged, onCheckedChange = { permanentDeleteAcknowledged = it })
                        Text("我知道永久删除后无法恢复", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    action()
                    dismissPermanentDelete()
                }, enabled = permanentDeleteAcknowledged) { Text("永久删除") }
            },
            dismissButton = { TextButton(onClick = ::dismissPermanentDelete) { Text("取消") } },
        )
    }
    if (showReminderDialog) {
        val editingReminder = reminderToEdit
        ReminderDialog(
            projects = projects,
            initialReminder = editingReminder,
            onDismiss = { showReminderDialog = false; reminderToEditId = null },
        ) { projectId, kind, time, quietStart, quietEnd, repeatDays ->
            val onResult: (Boolean) -> Unit = { success ->
                if (success) {
                    showReminderDialog = false
                    reminderToEditId = null
                }
            }
            if (editingReminder == null) {
                onNewReminder(projectId, kind, time, quietStart, quietEnd, repeatDays, onResult)
            } else {
                onUpdateReminder(editingReminder.id, projectId, kind, time, quietStart, quietEnd, repeatDays, onResult)
            }
        }
    }
    val reminderToDelete = reminderToDeleteId?.let { id -> state.reminders.firstOrNull { it.id == id } }
    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDeleteId = null },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("删除这条提醒？") },
            text = { Text("${reminderToDelete.timeMinutes / 60}:${(reminderToDelete.timeMinutes % 60).toString().padStart(2, '0')} · 删除后不会再自动触发。") },
            confirmButton = {
                Button(onClick = { onDeleteReminder(reminderToDelete.id); reminderToDeleteId = null }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { reminderToDeleteId = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun DeletedRow(title: String, onRestore: () -> Unit, onDeleteForever: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
        TextButton(onClick = onRestore) { Text("恢复") }
        IconButton(onClick = onDeleteForever, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, "永久删除", tint = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun FeedbackToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun UpdateCenterCard(updateState: UpdateUiState, onCheck: () -> Unit, onDownload: () -> Unit, onInstall: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit, zoneId: ZoneId = ZoneId.systemDefault()) {
    val available = updateState.available
    val canRetryInstall = available != null && updateState.phase == UpdatePhase.INSTALLING && !updateState.isDownloading
    val phaseLabel = when (updateState.phase) {
        UpdatePhase.CHECKING -> "正在连接 GitHub Release…"
        UpdatePhase.CONNECTING -> "正在连接更新服务器…"
        UpdatePhase.RESUMING -> "正在从上次进度继续下载…"
        UpdatePhase.DOWNLOADING -> "正在下载更新包…"
        UpdatePhase.VERIFYING -> "正在校验 SHA-256…"
        UpdatePhase.CERTIFICATE -> "正在校验版本与签名证书…"
        UpdatePhase.INSTALLING -> "安装包已就绪，等待系统安装器确认…"
        UpdatePhase.IDLE -> updateState.statusMessage ?: "稳定版更新来自 GitHub，数据不会上传"
    }
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = if (available != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, if (available != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (available == null) Icons.Default.CloudDownload else Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (available == null) "当前版本 v${BuildConfig.VERSION_NAME}" else "发现新版本 v${available.versionName}", fontWeight = FontWeight.Bold)
                    Text(
                        when {
                            updateState.errorMessage != null -> updateState.errorMessage
                            updateState.isDownloading || updateState.isChecking -> phaseLabel
                            available != null && updateState.phase == UpdatePhase.INSTALLING -> "安装包已校验，点击即可重新打开系统安装器"
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
            UpdateProgressFeedback(updateState)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("上次检查：${formatLastChecked(updateState.lastCheckedAtEpochMillis, zoneId)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
                if (available != null) {
                    Button(onClick = if (canRetryInstall) onInstall else onDownload, enabled = !updateState.isDownloading) {
                        Icon(if (canRetryInstall) Icons.AutoMirrored.Filled.OpenInNew else Icons.Default.CloudDownload, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (canRetryInstall) "重新打开安装器" else "下载并安装")
                    }
                } else {
                    OutlinedButton(onClick = onCheck, enabled = !updateState.isChecking && !updateState.isDownloading) { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text(if (updateState.isChecking) "检查中" else "检查更新") }
                }
            }
            if (updateState.isDownloading) {
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) { Text("暂停下载") }
            }
            Text("下载后会验证 SHA-256；不会静默安装，也不会覆盖你的本地数据。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun UpdateProgressFeedback(updateState: UpdateUiState) {
    if (!updateState.isDownloading) return
    val progress = updateState.downloadProgress?.coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (progress == null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        } else {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
        }
        Text(
            buildString {
                if (progress == null) append("下载进度：正在获取文件大小")
                else append("下载进度 ${((progress * 100).roundToInt())}%")
                append(" · 已下载 ")
                append(formatBytes(updateState.downloadedBytes))
                updateState.totalDownloadBytes?.let { append(" / "); append(formatBytes(it)) }
            },
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReviewTaskCard(
    task: LearningTaskEntity,
    onReview: () -> Unit,
    onInitial: () -> Unit,
    compact: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onCorrect: (() -> Unit)? = null,
) {
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
                if (onEdit != null || onDelete != null || onCorrect != null) {
                    var menuExpanded by remember(task.id) { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.MoreVert, "任务操作") }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            onEdit?.let { callback -> DropdownMenuItem(text = { Text("编辑") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuExpanded = false; callback() }) }
                            onCorrect?.let { callback -> DropdownMenuItem(text = { Text("纠正复习") }, leadingIcon = { Icon(Icons.Default.Refresh, null) }, onClick = { menuExpanded = false; callback() }) }
                            onDelete?.let { callback -> DropdownMenuItem(text = { Text("移入回收站") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menuExpanded = false; callback() }) }
                        }
                    }
                }
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
private fun ReadingPlanCard(
    plan: ReadingPlanEntity,
    pagesToday: Int,
    targetPages: Int,
    onLog: () -> Unit,
    onAdjust: (() -> Unit)? = null,
    onRebalance: () -> Unit,
    onAdjustTarget: (Int) -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
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
                if (onEdit != null || onDelete != null || onAdjust != null) {
                    var menuExpanded by remember(plan.id) { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.MoreVert, "阅读计划操作") }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            onEdit?.let { callback -> DropdownMenuItem(text = { Text("编辑计划") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuExpanded = false; callback() }) }
                            onAdjust?.let { callback -> DropdownMenuItem(text = { Text("纠正页数") }, leadingIcon = { Icon(Icons.Default.Refresh, null) }, onClick = { menuExpanded = false; callback() }) }
                            onDelete?.let { callback -> DropdownMenuItem(text = { Text("移入回收站") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menuExpanded = false; callback() }) }
                        }
                    }
                }
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
private fun TodoCard(todo: TodoEntity, today: LocalDate, onToggle: () -> Unit, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
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
            if (onEdit != null || onDelete != null) {
                var menuExpanded by remember(todo.id) { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.MoreVert, "待办操作") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        onEdit?.let { callback -> DropdownMenuItem(text = { Text("编辑") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuExpanded = false; callback() }) }
                        onDelete?.let { callback -> DropdownMenuItem(text = { Text("移入回收站") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menuExpanded = false; callback() }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, state: LearnListUiState, today: LocalDate, zoneId: ZoneId, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val current = goalCurrent(goal, state, today, zoneId)
    val percent = GoalProgressCalculator().calculate(current, goal.targetValue.coerceAtLeast(1)).percent
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(9.dp))
                Text(goal.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$percent%", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                if (onEdit != null || onDelete != null) {
                    var menuExpanded by remember(goal.id) { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.MoreVert, "目标操作") }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            onEdit?.let { callback -> DropdownMenuItem(text = { Text("编辑目标") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuExpanded = false; callback() }) }
                            onDelete?.let { callback -> DropdownMenuItem(text = { Text("移入回收站") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menuExpanded = false; callback() }) }
                        }
                    }
                }
            }
            LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.tertiaryContainer)
            Text("${metricLabel(goal.metric)}：$current / ${goal.targetValue} · ${periodLabel(goal.period)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CountdownCard(countdown: CountdownEntity, onComplete: () -> Unit, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null, clock: Clock) {
    var now by remember(countdown.id, countdown.isCompleted, countdown.eventAtEpochMillis, clock) { mutableLongStateOf(clock.millis()) }
    LaunchedEffect(countdown.id, countdown.isCompleted, countdown.eventAtEpochMillis, clock) {
        while (!countdown.isCompleted) {
            now = clock.millis()
            if (now >= countdown.eventAtEpochMillis) break
            delay(1000)
        }
        now = clock.millis()
    }
    val duration = Duration.ofMillis(countdown.eventAtEpochMillis - now)
    val text = when { countdown.isCompleted -> "已完成"; duration.isNegative -> "已到期"; else -> "${duration.toDays()}天 ${duration.toHours() % 24}小时 ${duration.toMinutes() % 60}分" }
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) { Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(19.dp)) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(countdown.title, fontWeight = FontWeight.SemiBold); Text(text, color = if (duration.isNegative && !countdown.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); if (countdown.note.isNotBlank()) Text(countdown.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!countdown.isCompleted) IconButton(onClick = onComplete) { Icon(Icons.Default.Check, "完成", tint = MaterialTheme.colorScheme.secondary) }
            if (onEdit != null || onDelete != null) {
                var menuExpanded by remember(countdown.id) { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.MoreVert, "倒计时操作") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        onEdit?.let { callback -> DropdownMenuItem(text = { Text("编辑倒计时") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuExpanded = false; callback() }) }
                        onDelete?.let { callback -> DropdownMenuItem(text = { Text("移入回收站") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menuExpanded = false; callback() }) }
                    }
                }
            }
        }
    }
}

private data class ActivityMetricSeries(val label: String, val unit: String, val color: Color, val values: List<Int>)

@Composable
private fun activityMetricSeries(state: LearnListUiState, today: LocalDate, days: Int, zoneId: ZoneId): List<ActivityMetricSeries> {
    val dates = (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    val todoValues = state.todos.map { todo ->
        todo.completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.toSet()
    }
    return listOf(
        ActivityMetricSeries("复习项", "项", MaterialTheme.colorScheme.primary, dates.map { date -> state.reviewLogs.count { it.reviewedOn == date.toString() } }),
        ActivityMetricSeries("阅读页", "页", MaterialTheme.colorScheme.secondary, dates.map { date ->
            (state.pageLogs.filter { it.localDate == date.toString() }.sumOf(PageLogEntity::pagesRead) + state.readingAdjustments.filter { it.localDate == date.toString() }.sumOf(ReadingAdjustmentEntity::deltaPages)).coerceAtLeast(0)
        }),
        ActivityMetricSeries("专注", "分", MaterialTheme.colorScheme.tertiary, dates.map { date -> state.focusSessions.filter { it.activityDate(zoneId) == date }.sumOf { it.actualSeconds / 60 } }),
        ActivityMetricSeries("待办", "项", MaterialTheme.colorScheme.error, dates.map { date -> todoValues.sumOf { completed -> if (date in completed) 1 else 0 } }),
    )
}

@Composable
private fun MetricHeatMap(state: LearnListUiState, today: LocalDate, zoneId: ZoneId) {
    val series = activityMetricSeries(state, today, 28, zoneId)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        series.forEach { metric ->
            val max = metric.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(metric.label, Modifier.width(42.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    metric.values.forEach { value ->
                        Box(
                            Modifier.weight(1f).height(22.dp).clip(RoundedCornerShape(5.dp)).background(metric.color.copy(alpha = 0.10f + 0.78f * value / max.toFloat())),
                        )
                    }
                }
            }
        }
        Text("颜色深浅只在同一行内比较；专注按分钟、阅读按页数统计。", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetricTrendChart(state: LearnListUiState, today: LocalDate, zoneId: ZoneId) {
    val series = activityMetricSeries(state, today, 7, zoneId)
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        series.forEach { metric ->
            val max = metric.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(metric.label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("${metric.values.sum()} ${metric.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                metric.values.forEachIndexed { index, value ->
                    val date = today.minusDays((6 - index).toLong())
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA), Modifier.width(25.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress = { value / max.toFloat() }, Modifier.weight(1f), color = metric.color, trackColor = metric.color.copy(alpha = 0.12f))
                        Text("$value", Modifier.width(28.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    }
                }
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
private fun CollapsibleSectionHeader(
    text: String,
    subtitle: String? = null,
    expanded: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    onExpandedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        stateDescription = if (expanded) "已展开，点击收起" else "已收起，点击展开"
                    }
                    .clickable { onExpandedChange(!expanded) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text, style = MaterialTheme.typography.titleMedium)
                    subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
                }
            }
            trailing?.invoke()
            IconButton(onClick = { onExpandedChange(!expanded) }) {
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, if (expanded) "收起$text" else "展开$text")
            }
        }
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

private fun readingPagesOn(
    pageLogs: List<PageLogEntity>,
    adjustments: List<ReadingAdjustmentEntity>,
    planId: String? = null,
    date: LocalDate,
): Int = (
    pageLogs.filter { (planId == null || it.planId == planId) && it.localDate == date.toString() }.sumOf(PageLogEntity::pagesRead) +
        adjustments.filter { (planId == null || it.planId == planId) && it.localDate == date.toString() }.sumOf(ReadingAdjustmentEntity::deltaPages)
    ).coerceAtLeast(0)

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

private fun TodoEntity.previousMissedOccurrence(today: LocalDate): LocalDate? {
    if (repeatRule == TodoRepeatRule.ONCE.name || missedPromptPolicy == "NEVER") return null
    val base = dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    for (offset in 1..366) {
        val date = today.minusDays(offset.toLong())
        if (date.isBefore(base)) break
        if (isDueOn(date) && !isCompletedOn(date)) return date
    }
    return null
}

internal fun shouldPromptMissedTodo(
    selectedDate: LocalDate,
    currentDate: LocalDate,
    isRestDay: Boolean,
    hasEligibleProject: Boolean,
    hasMissedOccurrence: Boolean,
): Boolean = selectedDate == currentDate && !isRestDay && hasEligibleProject && hasMissedOccurrence

private fun TodoEntity.isCompletedOn(date: LocalDate): Boolean = TodoCompletion.isCompleted(completedDates, date)

private fun repeatLabel(rule: String): String = when (rule) { "DAILY" -> "每天"; "WEEKLY" -> "每周"; "WORKDAYS" -> "工作日"; "CUSTOM" -> "自定义"; else -> "一次性" }

private fun calculateStreak(state: LearnListUiState, today: LocalDate, restDays: Set<DayOfWeek> = emptySet(), zoneId: ZoneId = ZoneId.systemDefault()): Int {
    val input = DailyProgressMapper.from(
        projects = state.projects + state.archivedProjects,
        tasks = state.tasks,
        reviewLogs = state.reviewLogs,
        readingPlans = state.readingPlans,
        readingTargets = state.readingTargets,
        pageLogs = state.pageLogs,
        readingAdjustments = state.readingAdjustments,
        todos = state.todos,
        zoneId = zoneId,
    )
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

private fun goalCurrent(goal: GoalEntity, state: LearnListUiState, today: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    val metric = GoalMetric.fromStorage(goal.metric) ?: return 0
    val period = GoalPeriod.fromStorage(goal.period) ?: return 0
    val startDate = runCatching { LocalDate.parse(goal.startDate) }.getOrDefault(today)
    val endDate = goal.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    return GoalProgressAggregator().current(GoalDefinition(metric, period, startDate, endDate, goal.projectId), today, goalActivities(state, zoneId))
}

private fun goalActivities(state: LearnListUiState, zoneId: ZoneId = ZoneId.systemDefault()): List<GoalActivity> = buildList {
    val planProjects = state.readingPlans.associate { it.id to it.projectId }
    val taskProjects = state.tasks.associate { it.id to it.projectId }
    state.pageLogs.forEach { log -> runCatching { LocalDate.parse(log.localDate) }.getOrNull()?.let { add(GoalActivity(GoalMetric.READING_PAGES, it, log.pagesRead, planProjects[log.planId])) } }
    state.readingAdjustments.forEach { adjustment -> runCatching { LocalDate.parse(adjustment.localDate) }.getOrNull()?.let { add(GoalActivity(GoalMetric.READING_PAGES, it, adjustment.deltaPages, planProjects[adjustment.planId])) } }
    state.reviewLogs.forEach { log -> runCatching { LocalDate.parse(log.reviewedOn) }.getOrNull()?.let { add(GoalActivity(GoalMetric.REVIEW_TASKS, it, 1, taskProjects[log.taskId])) } }
    state.todos.forEach { todo -> todo.completedDates.split(',').mapNotNull { token -> runCatching { LocalDate.parse(token) }.getOrNull() }.forEach { date -> add(GoalActivity(GoalMetric.TODO_DONE, date, 1, todo.projectId)) } }
    state.focusSessions.forEach { session -> add(GoalActivity(GoalMetric.FOCUS_MINUTES, session.activityDate(zoneId), session.actualSeconds / 60, session.projectId)) }
}

private fun FocusSessionEntity.activityDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate = (endedAt ?: startedAt).toLocalDate(zoneId)

private fun metricLabel(metric: String): String = when (metric) { "READING_PAGES" -> "阅读页数"; "REVIEW_TASKS" -> "复习项"; "TODO_DONE" -> "待办完成"; else -> "专注分钟" }
private fun periodLabel(period: String): String = when (period) { "WEEKLY" -> "本周"; "MONTHLY" -> "本月"; "CUSTOM" -> "自定义"; else -> "今天" }
private fun formatMinutes(value: Int?): String = value?.let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" } ?: "未设置"
private fun formatLastChecked(epoch: Long?, zoneId: ZoneId = ZoneId.systemDefault()): String = epoch?.let { Instant.ofEpochMilli(it).atZone(zoneId).format(DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA)) } ?: "尚未检查"
private fun feedbackModeLabel(soundEnabled: Boolean, vibrationEnabled: Boolean): String = when {
    soundEnabled && vibrationEnabled -> "声音 + 振动"
    soundEnabled -> "仅声音"
    vibrationEnabled -> "仅振动"
    else -> "静音"
}
private fun feedbackOverrideLabel(mode: String): String = when (mode) {
    "SOUND" -> "仅声音"
    "VIBRATION" -> "仅振动"
    "BOTH" -> "声音 + 振动"
    "OFF" -> "关闭"
    else -> "跟随全局"
}
private fun phaseLabel(phase: String): String = when (phase) {
    "SHORT_BREAK" -> "短休息"
    "LONG_BREAK" -> "长休息"
    else -> "专注进行中"
}
private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index += 1
    }
    return if (index == 0 || value >= 10.0 || value % 1.0 == 0.0) "${value.toInt()} ${units[index]}" else String.format(Locale.US, "%.1f %s", value, units[index])
}
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
private fun FocusBindingPicker(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember(label) { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: options.firstOrNull()?.second.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.78f), fontSize = 11.sp)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
            ) {
                Text(selectedLabel, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Default.ExpandMore, null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = { onSelected(id); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun ProjectDialog(initialProject: ProjectEntity? = null, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.title.orEmpty()) }; var type by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.type ?: "书籍") }; var description by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.description.orEmpty()) }; var tags by rememberSaveable(initialProject?.id) { mutableStateOf(initialProject?.tagCsv.orEmpty()) }
    FormDialog(if (initialProject == null) "新建学习项目" else "编辑学习项目", onDismiss, if (initialProject == null) "创建" else "保存", { OutlinedTextField(title, { title = it }, label = { Text("名称") }, singleLine = true); ChoiceRow("项目类型", type, listOf("书籍", "课程", "技能"), { it }) { type = it }; OutlinedTextField(description, { description = it }, label = { Text("简介（可选）") }); OutlinedTextField(tags, { tags = it }, label = { Text("标签，用逗号分隔") }, singleLine = true) }) { onSave(title, type, description, tags) }
}

@Composable
private fun TaskDialog(projects: List<ProjectEntity>, initialProjectId: String, initialTask: LearningTaskEntity? = null, onDismiss: () -> Unit, onSave: (String, String, String, String, String, Boolean) -> Unit) {
    var projectId by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.projectId ?: initialProjectId) }; var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }; var prompt by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.prompt.orEmpty()) }; var notes by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.notes.orEmpty()) }; var source by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.source.orEmpty()) }; var required by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.isRequired ?: true) }
    FormDialog(if (initialTask == null) "新建学习任务" else "编辑学习任务", onDismiss, if (initialTask == null) "加入" else "保存", { ProjectPicker(projects, projectId) { projectId = it }; OutlinedTextField(title, { title = it }, label = { Text("任务标题") }, singleLine = true); OutlinedTextField(prompt, { prompt = it }, label = { Text("回忆提示（可选）") }); OutlinedTextField(notes, { notes = it }, label = { Text("资料/笔记（复习时默认隐藏）") }); OutlinedTextField(source, { source = it }, label = { Text("来源（可选）") }, singleLine = true); FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做行动" else "可选行动") }) }) { onSave(projectId, title, prompt, notes, source, required) }
}

@Composable
private fun ReadingDialog(projects: List<ProjectEntity>, initialProjectId: String, initialPlan: ReadingPlanEntity? = null, today: LocalDate, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var projectId by rememberSaveable(initialPlan?.id) { mutableStateOf(initialPlan?.projectId ?: initialProjectId) }; var title by rememberSaveable(initialPlan?.id) { mutableStateOf(initialPlan?.title.orEmpty()) }; var total by rememberSaveable(initialPlan?.id) { mutableStateOf(initialPlan?.totalPages?.toString().orEmpty()) }; var target by rememberSaveable(initialPlan?.id) { mutableStateOf(initialPlan?.dailyTarget?.toString().orEmpty()) }; var deadline by rememberSaveable(initialPlan?.id) { mutableStateOf(initialPlan?.deadline.orEmpty()) }
    FormDialog(if (initialPlan == null) "新建阅读计划" else "编辑阅读计划", onDismiss, if (initialPlan == null) "创建" else "保存", { ProjectPicker(projects, projectId) { projectId = it }; OutlinedTextField(title, { title = it }, label = { Text("书名或资料名") }, singleLine = true); OutlinedTextField(total, { total = it }, label = { Text("总页数") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(target, { target = it }, label = { Text("每日必须看多少页") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); DateInputField(deadline, { deadline = it }, label = "截止日（可选）", allowClear = true, today = today); Text("设置截止日后，可将剩余页数一键均摊。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }) { onSave(projectId, title, total, target, deadline) }
}

@Composable
private fun TodoDialog(projects: List<ProjectEntity> = emptyList(), initialTodo: TodoEntity? = null, today: LocalDate, onDismiss: () -> Unit, onSave: (String, String, Boolean, String, String, String, String?) -> Unit) {
    var title by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.title.orEmpty()) }; var notes by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.notes.orEmpty()) }; var required by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.isRequired ?: true) }; var repeat by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.repeatRule ?: "ONCE") }; var custom by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.customRepeatDays.orEmpty()) }; var dueDate by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.dueDate ?: today.toString()) }; var projectId by rememberSaveable(initialTodo?.id) { mutableStateOf(initialTodo?.projectId.orEmpty()) }
    FormDialog(if (initialTodo == null) "新建待办" else "编辑待办", onDismiss, if (initialTodo == null) "添加" else "保存", { OutlinedTextField(title, { title = it }, label = { Text("待办内容") }, singleLine = true); OutlinedTextField(notes, { notes = it }, label = { Text("备注（可选）") }); if (projects.isNotEmpty()) ProjectPicker(projects, projectId) { projectId = it }; ChoiceRow("重复方式", repeat, listOf("ONCE", "DAILY", "WEEKLY", "WORKDAYS", "CUSTOM"), ::repeatLabel) { repeat = it }; if (repeat == "CUSTOM") OutlinedTextField(custom, { custom = it }, label = { Text("星期数字：1,3,5") }, singleLine = true); DateInputField(dueDate, { dueDate = it }, label = if (repeat == "ONCE") "到期日" else "开始日期", today = today); FilterChip(selected = required, onClick = { required = !required }, label = { Text(if (required) "必做" else "可选") }) }) { onSave(title, notes, required, repeat, custom, dueDate, projectId.takeIf(String::isNotBlank)) }
}

@Composable
private fun GoalDialog(projects: List<ProjectEntity> = emptyList(), initialGoal: GoalEntity? = null, today: LocalDate, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String?) -> Unit) {
    var title by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.title.orEmpty()) }; var metric by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.metric ?: "FOCUS_MINUTES") }; var target by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.targetValue?.toString().orEmpty()) }; var period by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.period ?: "DAILY") }; var endDate by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.endDate.orEmpty()) }; var projectId by rememberSaveable(initialGoal?.id) { mutableStateOf(initialGoal?.projectId.orEmpty()) }
    FormDialog(if (initialGoal == null) "新建量化目标" else "编辑量化目标", onDismiss, if (initialGoal == null) "创建" else "保存", { OutlinedTextField(title, { title = it }, label = { Text("目标名称") }, singleLine = true); if (projects.isNotEmpty()) ProjectPicker(projects, projectId) { projectId = it }; ChoiceRow("统计对象", metric, listOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE"), ::metricLabel) { metric = it }; OutlinedTextField(target, { target = it }, label = { Text("目标值") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); ChoiceRow("周期", period, listOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM"), ::periodLabel) { period = it }; if (period == "CUSTOM") DateInputField(endDate, { endDate = it }, label = "截止日", allowClear = true, today = today) }) { onSave(title, metric, target, period, endDate, projectId.takeIf(String::isNotBlank)) }
}

@Composable
private fun CountdownDialog(initialCountdown: CountdownEntity? = null, today: LocalDate, zoneId: ZoneId, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by rememberSaveable(initialCountdown?.id) { mutableStateOf(initialCountdown?.title.orEmpty()) }; var date by rememberSaveable(initialCountdown?.id) { mutableStateOf(initialCountdown?.let { Instant.ofEpochMilli(it.eventAtEpochMillis).atZone(zoneId).toLocalDate().toString() } ?: today.plusDays(7).toString()) }; var time by rememberSaveable(initialCountdown?.id) { mutableStateOf(initialCountdown?.let { Instant.ofEpochMilli(it.eventAtEpochMillis).atZone(zoneId).toLocalTime().withSecond(0).withNano(0).toString() } ?: "09:00") }; var note by rememberSaveable(initialCountdown?.id) { mutableStateOf(initialCountdown?.note.orEmpty()) }; var reminder by rememberSaveable(initialCountdown?.id) { mutableStateOf(initialCountdown?.reminderMinutesBefore?.toString().orEmpty()) }
    FormDialog(if (initialCountdown == null) "新建倒计时" else "编辑倒计时", onDismiss, if (initialCountdown == null) "创建" else "保存", { OutlinedTextField(title, { title = it }, label = { Text("事件名称") }, singleLine = true); DateInputField(date, { date = it }, label = "日期", today = today); TimeInputField(time, { time = it }, label = "时间"); OutlinedTextField(reminder, { reminder = it }, label = { Text("提前提醒分钟（可选）") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(note, { note = it }, label = { Text("备注（可选）") }) }) { onSave(title, date, time, note, reminder) }
}

@Composable
private fun ReviewDialog(task: LearningTaskEntity, onDismiss: () -> Unit, onReview: (RecallRating) -> Unit) {
    var showNotes by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text("复习：${task.title}") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { if (task.prompt.isNotBlank()) Text("回忆提示：${task.prompt}", fontWeight = FontWeight.SemiBold); Text("先在脑中回忆，再选择这次的状态。资料默认隐藏。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp); OutlinedButton(onClick = { showNotes = !showNotes }) { Icon(if (showNotes) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text(if (showNotes) "隐藏资料" else "查看资料") }; if (showNotes) { if (task.notes.isNotBlank()) Text(task.notes); if (task.source.isNotBlank()) Text("来源：${task.source}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }, confirmButton = { Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) { TextButton(onClick = { onReview(RecallRating.FORGOT) }) { Text("忘记") }; TextButton(onClick = { onReview(RecallRating.FUZZY) }) { Text("模糊") }; Button(onClick = { onReview(RecallRating.REMEMBERED) }) { Text("记得") } } }, dismissButton = { TextButton(onClick = { onReview(RecallRating.SNOOZE) }) { Text("稍后") } })
}

@Composable
private fun ReviewCorrectionDialog(
    task: LearningTaskEntity,
    onDismiss: () -> Unit,
    today: LocalDate,
    onSave: (stage: String, nextReviewDate: String, reason: String) -> Unit,
) {
    var stage by rememberSaveable(task.id) { mutableStateOf(task.stage.coerceIn(0, 7).toString()) }
    var nextReviewDate by rememberSaveable(task.id) { mutableStateOf(task.nextReviewDate ?: today.toString()) }
    var reason by rememberSaveable(task.id) { mutableStateOf("") }
    FormDialog(
        title = "纠正复习计划",
        onDismiss = onDismiss,
        confirmLabel = "保存纠正",
        content = {
            Text("只修正当前计划，不改写已有复习记录。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            OutlinedTextField(
                value = stage,
                onValueChange = { stage = it.filter(Char::isDigit).take(2) },
                label = { Text("记忆阶段（0—7）") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            DateInputField(
                value = nextReviewDate,
                onValueChange = { nextReviewDate = it },
                label = "下次复习日期",
                today = today,
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("纠正原因（可选）") },
                minLines = 2,
            )
        },
        onConfirm = { onSave(stage, nextReviewDate, reason) },
    )
}

@Composable
private fun PagesDialog(plan: ReadingPlanEntity, pagesToday: Int, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pages by rememberSaveable { mutableStateOf("") }
    FormDialog("记录阅读页数", onDismiss, "保存", { Text("${plan.title} · 今天已读 $pagesToday 页", fontWeight = FontWeight.SemiBold); OutlinedTextField(pages, { pages = it }, label = { Text("本次读了多少页") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); Text("当前页数会自动累加，最多不超过总页数。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }) { onSave(pages) }
}

@Composable
private fun ReadingAdjustmentDialog(plan: ReadingPlanEntity, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var delta by rememberSaveable(plan.id) { mutableStateOf("") }
    var reason by rememberSaveable(plan.id) { mutableStateOf("") }
    FormDialog(
        title = "纠正阅读页数",
        onDismiss = onDismiss,
        confirmLabel = "保存纠正",
        content = {
            Text("当前进度：${plan.currentPage} / ${plan.totalPages} 页", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = delta,
                onValueChange = { value ->
                    delta = if (value.startsWith("-")) "-${value.drop(1).filter(Char::isDigit)}" else value.filter(Char::isDigit)
                },
                label = { Text("调整页数（可正可负）") },
                supportingText = { Text("例如 -3 表示撤回 3 页；原始阅读日志不会被改写。") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("调整原因（可选）") },
                minLines = 2,
            )
        },
        onConfirm = { onSave(delta, reason) },
    )
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
private fun ReminderDialog(
    projects: List<ProjectEntity>,
    initialReminder: ReminderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, String, String, String) -> Unit,
) {
    val reminderKey = initialReminder?.id
    var kind by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.kind ?: "SUMMARY") }
    var projectId by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.projectId.orEmpty()) }
    var time by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.timeMinutes?.let(::formatMinutes) ?: "20:00") }
    var quietStart by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.quietStartMinutes?.let(::formatMinutes) ?: "22:00") }
    var quietEnd by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.quietEndMinutes?.let(::formatMinutes) ?: "07:00") }
    var repeatDays by rememberSaveable(reminderKey) { mutableStateOf(initialReminder?.repeatDays ?: "1,2,3,4,5,6,7") }
    val selectedDays = repeatDays.split(',').mapNotNull { it.toIntOrNull() }.toSet(); val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    FormDialog(
        title = if (initialReminder == null) "添加固定提醒" else "编辑固定提醒",
        onDismiss = onDismiss,
        confirmLabel = "保存",
        content = {
            ChoiceRow("提醒对象", kind, listOf("SUMMARY", "PROJECT"), { if (it == "SUMMARY") "每日进度" else "学习项目" }) {
                kind = it
                if (it == "SUMMARY") projectId = ""
                if (it == "PROJECT" && projectId.isBlank()) projectId = projects.firstOrNull()?.id.orEmpty()
            }
            if (kind == "PROJECT") ProjectPicker(projects, projectId) { projectId = it }
            TimeInputField(time, { time = it }, label = "提醒时间")
            Text("提醒日期", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..7).forEach { day ->
                    FilterChip(
                        selected = day in selectedDays,
                        onClick = { repeatDays = (if (day in selectedDays) selectedDays - day else selectedDays + day).sorted().joinToString(",") },
                        label = { Text(dayLabels[day - 1]) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeInputField(quietStart, { quietStart = it }, label = "安静开始", modifier = Modifier.weight(1f))
                TimeInputField(quietEnd, { quietEnd = it }, label = "安静结束", modifier = Modifier.weight(1f))
            }
            Text("安静时段内不会触发这条提醒；默认 22:00—07:00。", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        },
        onConfirm = { onSave(if (kind == "PROJECT") projectId.takeIf(String::isNotBlank) else null, kind, time, quietStart, quietEnd, repeatDays) },
    )
}

@Composable
private fun ChoiceRow(label: String, selected: String, options: List<String>, display: (String) -> String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { options.forEach { option -> FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(display(option)) }) } } }
}

@Composable
private fun FeedbackModeChoiceRow(label: String, selected: String, onSelect: (String) -> Unit) {
    ChoiceRow(
        label = label,
        selected = selected,
        options = listOf("GLOBAL", "SOUND", "VIBRATION", "BOTH", "OFF"),
        display = ::feedbackOverrideLabel,
        onSelect = onSelect,
    )
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, confirmLabel: String, content: @Composable ColumnScope.() -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, shape = MaterialTheme.shapes.large, containerColor = MaterialTheme.colorScheme.surface, title = { Text(title) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }, confirmButton = { Button(onClick = onConfirm) { Text(confirmLabel) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

