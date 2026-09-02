package com.mymoss.learnlist

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.data.DiagnosticsService
import com.mymoss.learnlist.data.backup.BackupImportMode
import com.mymoss.learnlist.data.backup.PendingBackupImport
import com.mymoss.learnlist.data.backup.BackupService
import com.mymoss.learnlist.system.ReleaseChecker
import com.mymoss.learnlist.system.FeedbackManager
import com.mymoss.learnlist.system.FeedbackAudioManager
import com.mymoss.learnlist.system.FocusTimerScheduler
import com.mymoss.learnlist.system.FocusTimerService
import com.mymoss.learnlist.system.ReminderScheduler
import com.mymoss.learnlist.system.UpdateDownloadService
import com.mymoss.learnlist.system.UpdateDownloadStage
import com.mymoss.learnlist.system.UpdateVisibilityPolicy
import com.mymoss.learnlist.ui.LearnListApp
import com.mymoss.learnlist.ui.UpdatePhase
import com.mymoss.learnlist.ui.UpdateUiState
import com.mymoss.learnlist.ui.theme.LearnListTheme
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val app: LearnListApplication get() = application as LearnListApplication
    private val appClock: Clock = Clock.systemDefaultZone()
    private val backupService by lazy {
        BackupService(
            app.repository,
            settingsRepository,
            java.io.File(filesDir, "backup-snapshots"),
            java.io.File(filesDir, "feedback-audio"),
        )
    }
    private val diagnosticsService by lazy { DiagnosticsService(this, app.repository, settingsRepository) }
    private val settingsRepository by lazy { SettingsRepository(this) }
    private val focusTimerScheduler by lazy { FocusTimerScheduler(this) }
    private var pendingExport: ByteArray? = null
    private var pendingDiagnostics: ByteArray? = null
    private var pendingImportPassword: String = ""
    private var pendingImportMode: BackupImportMode = BackupImportMode.MERGE
    private var automaticUpdateJob: Job? = null
    private val pendingBackupImport = kotlinx.coroutines.flow.MutableStateFlow<PendingBackupImport?>(null)
    private val updateState = MutableStateFlow(UpdateUiState())

    private val createBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@registerForActivityResult
        val bytes = pendingExport ?: return@registerForActivityResult
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { contentResolver.openOutputStream(uri)?.use { it.write(bytes) } }
                .onSuccess { showToast("备份已保存") }
                .onFailure { showToast("保存备份失败：${it.message}") }
        }
    }

    private val createDiagnostics = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        val bytes = pendingDiagnostics ?: return@registerForActivityResult
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { contentResolver.openOutputStream(uri)?.use { it.write(bytes) } }
                .onSuccess { pendingDiagnostics = null; showToast("脱敏诊断已保存") }
                .onFailure { showToast("保存诊断失败：${it.message}") }
        }
    }

    private val openBackup = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val bytes = readBackup(uri)
                val preview = backupService.preview(bytes, pendingImportPassword)
                pendingBackupImport.value = PendingBackupImport(bytes, preview, pendingImportPassword, pendingImportMode)
            }.onFailure { showToast("读取备份失败：${it.message}") }
        }
    }

    private val openFeedbackAudio = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val imported = FeedbackAudioManager.importToPrivateDirectory(applicationContext, uri)
                val previous = settingsRepository.settings.first().feedbackAudioPath
                settingsRepository.update {
                    it.copy(
                        feedbackAudioPath = imported.path,
                        feedbackAudioName = imported.displayName,
                        feedbackAudioUri = null,
                    )
                }
                FeedbackAudioManager.deleteIfOwned(applicationContext, previous)
                imported.displayName
            }.onSuccess { showToast("已导入音效：$it") }
                .onFailure { showToast("导入音效失败：${it.message}") }
        }
    }

    private val pickSystemRingtone = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = result.data?.pickedRingtoneUri() ?: return@registerForActivityResult
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                val previous = settingsRepository.settings.first().feedbackAudioPath
                val displayName = FeedbackAudioManager.displayName(applicationContext, uri)
                settingsRepository.update {
                    it.copy(
                        feedbackAudioPath = null,
                        feedbackAudioUri = uri.toString(),
                        feedbackAudioName = displayName,
                    )
                }
                FeedbackAudioManager.deleteIfOwned(applicationContext, previous)
                displayName
            }.onSuccess { showToast("已选择系统提示音：$it") }
                .onFailure { showToast("选择系统提示音失败：${it.message}") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val feedbackContext = applicationContext
        setContent {
            LearnListTheme {
                val pending by pendingBackupImport.collectAsState()
                val appSettings by settingsRepository.settings.collectAsState(initial = null)
                val viewModel: com.mymoss.learnlist.ui.LearnListViewModel = viewModel(
                    factory = com.mymoss.learnlist.ui.LearnListViewModel.factory(
                        repository = app.repository,
                        settingsRepository = settingsRepository,
                        focusTimerScheduler = focusTimerScheduler,
                        onFocusStarted = { startedAt, endAt, plannedMinutes, phase, round ->
                            FocusTimerService.start(applicationContext, startedAt, endAt, plannedMinutes, phase, round)
                        },
                        onFocusStopped = { FocusTimerService.stop(applicationContext) },
                        onFocusCompleted = { settings -> FeedbackManager.play(feedbackContext, settings) },
                        onFocusPaused = { FocusTimerService.pause(applicationContext) },
                        onFocusSkipped = { FocusTimerService.skip(applicationContext) },
                    ),
                )
                LearnListApp(
                    viewModel = viewModel,
                    onExportBackup = ::exportBackup,
                    onImportBackup = ::chooseImport,
                    onExportDiagnostics = ::exportDiagnostics,
                    onCheckForUpdate = ::checkForUpdate,
                    updateState = updateState.collectAsState().value,
                    onDownloadUpdate = ::downloadAvailableUpdate,
                    onInstallUpdate = ::installCachedUpdate,
                    onCancelUpdate = ::cancelUpdateDownload,
                    onDismissUpdate = ::dismissAvailableUpdate,
                    onRequestNotifications = ::requestNotificationPermission,
                    onRequestExactAlarms = ::requestExactAlarmPermission,
                    soundEnabled = appSettings?.soundEnabled ?: true,
                    vibrationEnabled = appSettings?.vibrationEnabled ?: true,
                    focusFeedbackMode = appSettings?.focusFeedbackMode ?: "GLOBAL",
                    reminderFeedbackMode = appSettings?.reminderFeedbackMode ?: "GLOBAL",
                    countdownFeedbackMode = appSettings?.countdownFeedbackMode ?: "GLOBAL",
                    feedbackAudioName = appSettings?.feedbackAudioName,
                    feedbackAudioUri = appSettings?.feedbackAudioUri,
                    reviewBatchSize = appSettings?.reviewLimit ?: 20,
                    onReviewBatchSizeChange = { size ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(reviewLimit = size) } }
                    },
                    focusAutoStartBreaks = appSettings?.focusAutoStartBreaks ?: false,
                    onFocusAutoStartBreaksChange = { enabled ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(focusAutoStartBreaks = enabled) } }
                    },
                    onSoundEnabledChange = { enabled ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(soundEnabled = enabled) } }
                    },
                    onVibrationEnabledChange = { enabled ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(vibrationEnabled = enabled) } }
                    },
                    onFocusFeedbackModeChange = { mode ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(focusFeedbackMode = mode) } }
                    },
                    onReminderFeedbackModeChange = { mode ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(reminderFeedbackMode = mode) } }
                    },
                    onCountdownFeedbackModeChange = { mode ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(countdownFeedbackMode = mode) } }
                    },
                    onChooseFeedbackAudio = ::chooseFeedbackAudio,
                    onChooseSystemRingtone = ::chooseSystemRingtone,
                    onPreviewFeedbackAudio = {
                        lifecycleScope.launch {
                            FeedbackAudioManager.preview(applicationContext, settingsRepository.settings.first())
                        }
                    },
                    onClearFeedbackAudio = ::clearFeedbackAudio,
                    onboardingCompleted = appSettings?.hasCompletedOnboarding,
                    onCompleteOnboarding = {
                        lifecycleScope.launch {
                            settingsRepository.update { it.copy(hasCompletedOnboarding = true) }
                        }
                    },
                    pendingImport = pending,
                    onConfirmImport = ::confirmImport,
                    onCancelImport = { pendingBackupImport.value = null },
                    appClock = appClock,
                )
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                settingsRepository.migrateLegacySummaryReminderSettings { legacy ->
                    if (app.repository.snapshot().reminders.none { it.kind == "SUMMARY" }) {
                        app.repository.addReminder(
                            projectId = null,
                            kind = "SUMMARY",
                            timeMinutes = legacy.timeMinutes,
                            quietStartMinutes = legacy.quietStartMinutes,
                            quietEndMinutes = legacy.quietEndMinutes,
                            enabled = legacy.enabled,
                        )
                    }
                }
            }.onFailure { error ->
                android.util.Log.w("LearnList", "无法迁移旧版每日进度提醒设置", error)
            }
        }
        val reminderScheduler = ReminderScheduler(this, app.repository, appClock)
        lifecycleScope.launch {
            app.repository
                .observeReminders()
                .combine(app.repository.observeCountdowns()) { _, _ -> Unit }
                .combine(app.repository.observeProjects()) { _, _ -> Unit }
                .collectLatest {
                    runCatching { reminderScheduler.rescheduleAll() }
                }
        }
        lifecycleScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                val info = settings.toUpdateInfoOrNull() ?: return@collectLatest
                val progress = settings.updateTransferTotalBytes
                    ?.takeIf { it > 0L }
                    ?.let { total -> (settings.updateTransferDownloadedBytes.toFloat() / total).coerceIn(0f, 1f) }
                updateState.update {
                    it.copy(
                        available = info,
                        isDownloading = settings.updateTransferActive,
                        phase = settings.updateTransferStage.toUpdatePhase(),
                        downloadProgress = progress,
                        downloadedBytes = settings.updateTransferDownloadedBytes,
                        totalDownloadBytes = settings.updateTransferTotalBytes,
                        statusMessage = settings.updateTransferStatus ?: it.statusMessage,
                        errorMessage = settings.updateTransferError,
                    )
                }
            }
        }
        lifecycleScope.launch {
            if (settingsRepository.settings.first().updateTransferActive) {
                UpdateDownloadService.sync(applicationContext)
            }
        }
        automaticUpdateJob = lifecycleScope.launch { checkAutomatically() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            checkInstallResult()
            runCatching { ReminderScheduler(this@MainActivity, app.repository, appClock).rescheduleAll() }
        }
        if (automaticUpdateJob?.isActive != true) {
            automaticUpdateJob = lifecycleScope.launch { checkAutomatically() }
        }
    }

    private fun exportBackup(encrypted: Boolean, password: String) {
        lifecycleScope.launch {
            runCatching {
                pendingExport = backupService.export(encrypted, password)
                createBackup.launch(if (encrypted) "learn-list-backup.llbackup" else "learn-list-backup.json")
            }.onFailure { showToast("导出失败：${it.message}") }
        }
    }

    private fun exportDiagnostics() {
        lifecycleScope.launch {
            runCatching {
                pendingDiagnostics = diagnosticsService.export()
                createDiagnostics.launch("learn-list-diagnostics.json")
            }.onFailure { showToast("生成诊断失败：${it.message}") }
        }
    }

    private fun chooseImport(password: String, mode: BackupImportMode) {
        pendingImportPassword = password
        pendingImportMode = mode
        openBackup.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
    }

    private fun confirmImport(mode: BackupImportMode) {
        val request = pendingBackupImport.value ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { backupService.import(request.bytes, request.password, mode) }
                .onSuccess { result ->
                    pendingBackupImport.value = null
                    showToast("导入完成：${result.counts.values.sum()} 条记录")
                }
                .onFailure { showToast("导入失败：${it.message}") }
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
        } else {
            showToast("通知权限已开启")
        }
    }

    private fun chooseFeedbackAudio() {
        openFeedbackAudio.launch(arrayOf("audio/*"))
    }

    private fun chooseSystemRingtone() {
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            val existing = settings.feedbackAudioUri?.let { value -> runCatching { Uri.parse(value) }.getOrNull() }
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择提醒音")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(
                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    existing ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                )
            }
            pickSystemRingtone.launch(intent)
        }
    }

    private fun clearFeedbackAudio() {
        lifecycleScope.launch(Dispatchers.IO) {
            val previous = settingsRepository.settings.first().feedbackAudioPath
            FeedbackAudioManager.deleteIfOwned(applicationContext, previous)
            settingsRepository.update { it.copy(feedbackAudioPath = null, feedbackAudioUri = null, feedbackAudioName = null) }
            showToast("已恢复应用内置提示音")
        }
    }

    private fun requestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            showToast("当前 Android 版本不需要单独的精确提醒权限")
            return
        }
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager.canScheduleExactAlarms()) {
            showToast("精确提醒权限已开启")
        } else {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:$packageName".toUri()))
        }
    }

    private fun checkForUpdate() {
        lifecycleScope.launch { performUpdateCheck(manual = true) }
    }

    private suspend fun performUpdateCheck(manual: Boolean) {
        if (updateState.value.isChecking || updateState.value.isDownloading) return
        updateState.update {
            it.copy(
                isChecking = true,
                phase = UpdatePhase.CHECKING,
                downloadProgress = null,
                downloadedBytes = 0L,
                totalDownloadBytes = null,
                errorMessage = null,
                statusMessage = if (manual) "正在检查 GitHub Release…" else "自动检查中…",
            )
        }
        val checkedAt = System.currentTimeMillis()
        val result = ReleaseChecker().checkLatest()
        result.fold(
            onSuccess = { info ->
                val settings = settingsRepository.settings.first()
                val visibleInfo = info.takeIf {
                    UpdateVisibilityPolicy.shouldShow(it, settings.dismissedUpdateVersionName, manual)
                }
                settingsRepository.update {
                    it.copy(
                        lastUpdateCheckEpochMillis = checkedAt,
                        dismissedUpdateVersionName = if (info != null && info.versionName != it.dismissedUpdateVersionName) null else it.dismissedUpdateVersionName,
                    )
                }
                updateState.update {
                    it.copy(
                        isChecking = false,
                        phase = UpdatePhase.IDLE,
                        downloadProgress = null,
                        downloadedBytes = 0L,
                        totalDownloadBytes = null,
                        available = visibleInfo,
                        statusMessage = UpdateVisibilityPolicy.statusMessage(info, settings.dismissedUpdateVersionName, manual),
                        errorMessage = null,
                        lastCheckedAtEpochMillis = checkedAt,
                    )
                }
                if (manual) showToast(
                    when {
                        visibleInfo != null -> "发现新版本 v${visibleInfo.versionName}，请在更新中心确认"
                        info != null -> "已暂不提醒 v${info.versionName}；有更新版本时会再次提示"
                        else -> "当前已是最新版本"
                    },
                )
            },
            onFailure = { error ->
                updateState.update {
                    it.copy(
                        isChecking = false,
                        phase = UpdatePhase.IDLE,
                        downloadProgress = null,
                        downloadedBytes = 0L,
                        totalDownloadBytes = null,
                        statusMessage = null,
                        errorMessage = error.message ?: "检查更新失败",
                        lastCheckedAtEpochMillis = checkedAt,
                    )
                }
                if (manual) showToast("检查更新失败：${error.message}")
            },
        )
    }

    private fun downloadAvailableUpdate() {
        val info = updateState.value.available ?: return
        lifecycleScope.launch {
            settingsRepository.update { settings ->
                if (settings.dismissedUpdateVersionName == info.versionName) {
                    settings.copy(dismissedUpdateVersionName = null)
                } else {
                    settings
                }
            }
        }
        updateState.update {
            it.copy(
                available = info,
                isDownloading = true,
                phase = UpdatePhase.CONNECTING,
                downloadProgress = null,
                downloadedBytes = 0L,
                totalDownloadBytes = null,
                errorMessage = null,
                statusMessage = "正在连接 GitHub Release…",
            )
        }
        UpdateDownloadService.start(applicationContext, info)
    }

    private fun installCachedUpdate() {
        val info = updateState.value.available ?: return
        lifecycleScope.launch {
            settingsRepository.update { settings ->
                if (settings.dismissedUpdateVersionName == info.versionName) {
                    settings.copy(dismissedUpdateVersionName = null)
                } else {
                    settings
                }
            }
        }
        updateState.update {
            it.copy(
                isDownloading = true,
                phase = UpdatePhase.VERIFYING,
                downloadProgress = null,
                downloadedBytes = 0L,
                totalDownloadBytes = null,
                errorMessage = null,
                statusMessage = "正在验证已下载的更新包…",
            )
        }
        UpdateDownloadService.installCached(applicationContext, info)
    }

    private fun cancelUpdateDownload() {
        UpdateDownloadService.pause(applicationContext)
        updateState.update {
            it.copy(
                isDownloading = false,
                phase = UpdatePhase.IDLE,
                downloadProgress = null,
                statusMessage = "下载已暂停，再次点击将从断点继续",
                errorMessage = null,
            )
        }
    }

    private fun dismissAvailableUpdate() {
        val info = updateState.value.available ?: return
        lifecycleScope.launch {
            settingsRepository.update { it.copy(dismissedUpdateVersionName = info.versionName) }
        }
        updateState.update {
            it.copy(
                available = null,
                statusMessage = "已暂不提醒 v${info.versionName}；有更新版本时会再次提示",
                errorMessage = null,
            )
        }
    }

    private fun com.mymoss.learnlist.data.AppSettings.toUpdateInfoOrNull(): com.mymoss.learnlist.system.UpdateInfo? =
        com.mymoss.learnlist.system.UpdateInfo(
            tagName = updateTransferTagName.orEmpty(),
            versionName = updateTransferVersionName.orEmpty(),
            downloadUrl = updateTransferDownloadUrl.orEmpty(),
            sha256Url = updateTransferSha256Url,
            releaseNotes = updateTransferReleaseNotes,
        ).takeIf { it.tagName.isNotBlank() && it.versionName.isNotBlank() && it.downloadUrl.isNotBlank() && it.sha256Url != null }

    private fun String?.toUpdatePhase(): UpdatePhase = when (this) {
        UpdateDownloadStage.CONNECTING.name -> UpdatePhase.CONNECTING
        UpdateDownloadStage.RESUMING.name -> UpdatePhase.RESUMING
        UpdateDownloadStage.DOWNLOADING.name -> UpdatePhase.DOWNLOADING
        UpdateDownloadStage.VERIFYING.name -> UpdatePhase.VERIFYING
        UpdateDownloadStage.CERTIFICATE.name -> UpdatePhase.CERTIFICATE
        "INSTALLING" -> UpdatePhase.INSTALLING
        else -> UpdatePhase.IDLE
    }

    private suspend fun checkAutomatically() {
        val settings = settingsRepository.settings.first()
        val now = System.currentTimeMillis()
        updateState.update { it.copy(lastCheckedAtEpochMillis = settings.lastUpdateCheckEpochMillis.takeIf { value -> value > 0L }) }
        if (now - settings.lastUpdateCheckEpochMillis < Duration.ofHours(24).toMillis()) return
        performUpdateCheck(manual = false)
    }

    private suspend fun checkInstallResult() {
        val expectedVersion = settingsRepository.settings.first().pendingUpdateVersionName ?: return
        if (BuildConfig.VERSION_NAME != expectedVersion) return
        settingsRepository.update {
            it.copy(
                pendingUpdateVersionName = null,
                dismissedUpdateVersionName = null,
                updateTransferActive = false,
                updateTransferTagName = null,
                updateTransferVersionName = null,
                updateTransferDownloadUrl = null,
                updateTransferSha256Url = null,
                updateTransferReleaseNotes = "",
                updateTransferStage = null,
                updateTransferDownloadedBytes = 0L,
                updateTransferTotalBytes = null,
                updateTransferStatus = null,
                updateTransferError = null,
            )
        }
        updateState.update {
            it.copy(
                available = null,
                isDownloading = false,
                phase = UpdatePhase.IDLE,
                downloadProgress = null,
                statusMessage = "v$expectedVersion 已安装完成",
                errorMessage = null,
            )
        }
        showToast("Learn List v$expectedVersion 已更新完成")
    }

    private fun readBackup(uri: android.net.Uri): ByteArray {
        val maxBytes = 20 * 1024 * 1024
        val input = contentResolver.openInputStream(uri) ?: error("无法读取文件")
        return input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(16 * 1024)
            var total = 0
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                total += read
                if (total > maxBytes) error("备份文件过大")
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    @Suppress("DEPRECATION")
    private fun Intent.pickedRingtoneUri(): Uri? = if (Build.VERSION.SDK_INT >= 33) {
        getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
    } else {
        getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
    }

    private companion object { const val NOTIFICATION_REQUEST_CODE = 1001 }
}


