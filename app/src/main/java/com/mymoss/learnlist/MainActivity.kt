package com.mymoss.learnlist

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.content.Intent
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
import com.mymoss.learnlist.data.backup.BackupImportMode
import com.mymoss.learnlist.data.backup.PendingBackupImport
import com.mymoss.learnlist.data.backup.BackupService
import com.mymoss.learnlist.system.ReleaseChecker
import com.mymoss.learnlist.system.FeedbackManager
import com.mymoss.learnlist.system.FocusTimerScheduler
import com.mymoss.learnlist.system.ReminderScheduler
import com.mymoss.learnlist.system.UpdateDownloadStage
import com.mymoss.learnlist.system.UpdateInstaller
import com.mymoss.learnlist.ui.LearnListApp
import com.mymoss.learnlist.ui.UpdatePhase
import com.mymoss.learnlist.ui.UpdateUiState
import com.mymoss.learnlist.ui.theme.LearnListTheme
import java.io.ByteArrayOutputStream
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
    private val backupService by lazy { BackupService(app.repository, settingsRepository) }
    private val settingsRepository by lazy { SettingsRepository(this) }
    private val focusTimerScheduler by lazy { FocusTimerScheduler(this) }
    private var pendingExport: ByteArray? = null
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
                        onFocusCompleted = { settings -> FeedbackManager.play(feedbackContext, settings) },
                    ),
                )
                LearnListApp(
                    viewModel = viewModel,
                    onExportBackup = ::exportBackup,
                    onImportBackup = ::chooseImport,
                    onCheckForUpdate = ::checkForUpdate,
                    updateState = updateState.collectAsState().value,
                    onDownloadUpdate = ::downloadAvailableUpdate,
                    onDismissUpdate = { updateState.update { it.copy(available = null) } },
                    onRequestNotifications = ::requestNotificationPermission,
                    onRequestExactAlarms = ::requestExactAlarmPermission,
                    soundEnabled = appSettings?.soundEnabled ?: true,
                    vibrationEnabled = appSettings?.vibrationEnabled ?: true,
                    onSoundEnabledChange = { enabled ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(soundEnabled = enabled) } }
                    },
                    onVibrationEnabledChange = { enabled ->
                        lifecycleScope.launch { settingsRepository.update { it.copy(vibrationEnabled = enabled) } }
                    },
                    onboardingCompleted = appSettings?.hasCompletedOnboarding,
                    onCompleteOnboarding = {
                        lifecycleScope.launch {
                            settingsRepository.update { it.copy(hasCompletedOnboarding = true) }
                        }
                    },
                    pendingImport = pending,
                    onConfirmImport = ::confirmImport,
                    onCancelImport = { pendingBackupImport.value = null },
                )
            }
        }
        val reminderScheduler = ReminderScheduler(this, app.repository)
        lifecycleScope.launch {
            app.repository.observeReminders().combine(app.repository.observeCountdowns()) { _, _ -> Unit }.collectLatest {
                runCatching { reminderScheduler.rescheduleAll() }
            }
        }
        automaticUpdateJob = lifecycleScope.launch { checkAutomatically() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            runCatching { ReminderScheduler(this@MainActivity, app.repository).rescheduleAll() }
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
                settingsRepository.update { it.copy(lastUpdateCheckEpochMillis = checkedAt) }
                updateState.update {
                    it.copy(
                        isChecking = false,
                        phase = UpdatePhase.IDLE,
                        downloadProgress = null,
                        downloadedBytes = 0L,
                        totalDownloadBytes = null,
                        available = info,
                        statusMessage = if (info == null) "当前已是最新版本" else "发现 v${info.versionName}，可以下载更新",
                        errorMessage = null,
                        lastCheckedAtEpochMillis = checkedAt,
                    )
                }
                if (manual) showToast(if (info == null) "当前已是最新版本" else "发现新版本 v${info.versionName}，请在更新中心确认")
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
            updateState.update {
                it.copy(
                    isDownloading = true,
                    phase = UpdatePhase.CONNECTING,
                    downloadProgress = null,
                    downloadedBytes = 0L,
                    totalDownloadBytes = null,
                    errorMessage = null,
                    statusMessage = "正在连接 GitHub Release…",
                )
            }
            runCatching {
                val apk = UpdateInstaller(this@MainActivity).downloadAndVerify(info) { progress ->
                    updateState.update { current ->
                        val fraction = progress.totalBytes
                            ?.takeIf { total -> total > 0L }
                            ?.let { total -> (progress.downloadedBytes.toFloat() / total).coerceIn(0f, 1f) }
                        current.copy(
                            phase = when (progress.stage) {
                                UpdateDownloadStage.CONNECTING -> UpdatePhase.CONNECTING
                                UpdateDownloadStage.DOWNLOADING -> UpdatePhase.DOWNLOADING
                                UpdateDownloadStage.VERIFYING -> UpdatePhase.VERIFYING
                            },
                            downloadProgress = fraction,
                            downloadedBytes = progress.downloadedBytes,
                            totalDownloadBytes = progress.totalBytes,
                            statusMessage = when (progress.stage) {
                                UpdateDownloadStage.CONNECTING -> "正在连接 GitHub Release…"
                                UpdateDownloadStage.DOWNLOADING -> "正在下载 v${info.versionName}…"
                                UpdateDownloadStage.VERIFYING -> "正在校验 SHA-256…"
                            },
                        )
                    }
                }
                updateState.update {
                    it.copy(
                        phase = UpdatePhase.INSTALLING,
                        downloadProgress = 1f,
                        statusMessage = "安装包已校验，正在打开系统安装器…",
                    )
                }
                withContext(Dispatchers.Main) { UpdateInstaller(this@MainActivity).install(apk) }
            }.fold(
                onSuccess = { installerOpened ->
                    updateState.update {
                        it.copy(
                            isDownloading = false,
                            phase = UpdatePhase.IDLE,
                            downloadProgress = null,
                            statusMessage = if (installerOpened) "安装确认已打开，请按系统提示完成更新" else "请在系统设置中允许本应用安装未知来源",
                        )
                    }
                    showToast(if (installerOpened) "安装确认已打开" else "请允许安装未知来源后重试")
                },
                onFailure = { error ->
                    updateState.update {
                        it.copy(
                            isDownloading = false,
                            phase = UpdatePhase.IDLE,
                            downloadProgress = null,
                            errorMessage = error.message ?: "更新失败",
                            statusMessage = null,
                        )
                    }
                    showToast("更新失败：${error.message}")
                },
            )
        }
    }

    private suspend fun checkAutomatically() {
        val settings = settingsRepository.settings.first()
        val now = System.currentTimeMillis()
        updateState.update { it.copy(lastCheckedAtEpochMillis = settings.lastUpdateCheckEpochMillis.takeIf { value -> value > 0L }) }
        if (now - settings.lastUpdateCheckEpochMillis < Duration.ofHours(24).toMillis()) return
        performUpdateCheck(manual = false)
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

    private companion object { const val NOTIFICATION_REQUEST_CODE = 1001 }
}

