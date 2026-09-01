package com.mymoss.learnlist.system

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.mymoss.learnlist.MainActivity
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.AppSettings
import com.mymoss.learnlist.data.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Owns update downloads outside the Activity so a backgrounded screen does not cancel them. */
class UpdateDownloadService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private var downloadJob: Job? = null
    private var latestProgress: UpdateDownloadProgress? = null
    private var lastProgressPersistedAt = 0L
    private var lastProgressStage: UpdateDownloadStage? = null
    private var progressPersistJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(this)
        runCatching {
            // Keep the foreground promotion path minimal. The detailed progress
            // notification is posted once the service has started its work.
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildStartupNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                },
            )
        }.onFailure {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val info = intent.toUpdateInfo()
                if (info == null) {
                    serviceScope.launch { markFailure("更新信息不完整") ; stopService(startId) }
                } else {
                    startDownload(info, startId)
                }
            }
            ACTION_INSTALL -> {
                val info = intent.toUpdateInfo()
                if (info == null) {
                    serviceScope.launch { markFailure("更新信息不完整") ; stopService(startId) }
                } else {
                    startCachedInstall(info, startId)
                }
            }
            ACTION_PAUSE -> pauseDownload(startId)
            ACTION_SYNC, null -> recoverDownload(startId)
            else -> recoverDownload(startId)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        downloadJob?.cancel()
        serviceScope.cancel()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun startDownload(info: UpdateInfo, startId: Int) {
        if (downloadJob?.isActive == true) return
        latestProgress = null
        lastProgressPersistedAt = 0L
        lastProgressStage = null
        downloadJob = serviceScope.launch {
            persist(
                settingsRepository.settings.first().copy(
                    updateTransferActive = true,
                    updateTransferTagName = info.tagName,
                    updateTransferVersionName = info.versionName,
                    updateTransferDownloadUrl = info.downloadUrl,
                    updateTransferSha256Url = info.sha256Url,
                    updateTransferReleaseNotes = info.releaseNotes,
                    updateTransferStage = UpdateDownloadStage.CONNECTING.name,
                    updateTransferDownloadedBytes = 0L,
                    updateTransferTotalBytes = null,
                    updateTransferStatus = "正在连接 GitHub Release…",
                    updateTransferError = null,
                    pendingUpdateVersionName = null,
                ),
            )
            try {
                val installer = UpdateInstaller(applicationContext)
                val apk = installer.downloadAndVerify(info) { progress -> onProgress(info, progress) }
                progressPersistJob?.join()
                latestProgress?.let { progress -> persistProgress(info, progress) }
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = true,
                        updateTransferStage = UpdateDownloadStage.CERTIFICATE.name,
                        updateTransferStatus = "安装包已校验，正在打开系统安装器…",
                        updateTransferError = null,
                    ),
                )
                val installerOpened = withContext(Dispatchers.Main) { installer.install(apk) }
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = false,
                        updateTransferStage = UPDATE_PHASE_INSTALLING,
                        updateTransferStatus = if (installerOpened) "安装确认已打开，请按系统提示完成更新" else "请允许安装未知来源后重试",
                        updateTransferError = null,
                        pendingUpdateVersionName = if (installerOpened) info.versionName else null,
                    ),
                )
            } catch (error: CancellationException) {
                withContext(NonCancellable) {
                    progressPersistJob?.cancel()
                    progressPersistJob?.join()
                    latestProgress?.let { progress -> persistProgress(info, progress) }
                    persist(
                        settingsRepository.settings.first().copy(
                            updateTransferActive = false,
                            updateTransferStatus = "下载已暂停，再次点击将从断点继续",
                            updateTransferError = null,
                        ),
                    )
                }
                throw error
            } catch (error: Exception) {
                progressPersistJob?.cancel()
                progressPersistJob?.join()
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = false,
                        updateTransferStatus = null,
                        updateTransferError = error.message ?: "更新失败",
                    ),
                )
            } finally {
                downloadJob = null
                stopService(startId)
            }
        }
    }

    private fun startCachedInstall(info: UpdateInfo, startId: Int) {
        if (downloadJob?.isActive == true) return
        latestProgress = null
        lastProgressPersistedAt = 0L
        lastProgressStage = null
        downloadJob = serviceScope.launch {
            try {
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = true,
                        updateTransferTagName = info.tagName,
                        updateTransferVersionName = info.versionName,
                        updateTransferDownloadUrl = info.downloadUrl,
                        updateTransferSha256Url = info.sha256Url,
                        updateTransferReleaseNotes = info.releaseNotes,
                        updateTransferStage = UpdateDownloadStage.VERIFYING.name,
                        updateTransferDownloadedBytes = 0L,
                        updateTransferTotalBytes = null,
                        updateTransferStatus = "正在验证已下载的更新包…",
                        updateTransferError = null,
                        pendingUpdateVersionName = null,
                    ),
                )
                val installer = UpdateInstaller(applicationContext)
                val installerOpened = installer.verifyCachedAndInstall(info) { progress -> onProgress(info, progress) }
                progressPersistJob?.join()
                latestProgress?.let { progress -> persistProgress(info, progress) }
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = false,
                        updateTransferStage = UPDATE_PHASE_INSTALLING,
                        updateTransferStatus = if (installerOpened) "安装确认已打开，请按系统提示完成更新" else "请允许安装未知来源后重试",
                        updateTransferError = null,
                        pendingUpdateVersionName = if (installerOpened) info.versionName else null,
                    ),
                )
            } catch (error: CancellationException) {
                withContext(NonCancellable) {
                    progressPersistJob?.cancel()
                    progressPersistJob?.join()
                    latestProgress?.let { progress -> persistProgress(info, progress) }
                    persist(
                        settingsRepository.settings.first().copy(
                            updateTransferActive = false,
                            updateTransferStatus = "安装准备已暂停，再次点击将重新校验更新包",
                            updateTransferError = null,
                        ),
                    )
                }
                throw error
            } catch (error: Exception) {
                progressPersistJob?.cancel()
                progressPersistJob?.join()
                persist(
                    settingsRepository.settings.first().copy(
                        updateTransferActive = false,
                        updateTransferStage = null,
                        updateTransferStatus = "本地更新包不可用，请重新下载",
                        updateTransferError = error.message ?: "更新包校验失败",
                        pendingUpdateVersionName = null,
                    ),
                )
            } finally {
                downloadJob = null
                stopService(startId)
            }
        }
    }

    private fun pauseDownload(startId: Int) {
        if (downloadJob?.isActive == true) {
            downloadJob?.cancel()
        } else {
            serviceScope.launch {
                persist(settingsRepository.settings.first().copy(updateTransferActive = false, updateTransferStatus = "下载已暂停，再次点击将从断点继续", updateTransferError = null))
                stopService(startId)
            }
        }
    }

    private fun recoverDownload(startId: Int) {
        if (downloadJob?.isActive == true) return
        serviceScope.launch {
            val settings = settingsRepository.settings.first()
            val info = settings.toUpdateInfo()
            if (settings.updateTransferActive && info != null) {
                startDownload(info, startId)
            } else {
                stopService(startId)
            }
        }
    }

    private fun onProgress(info: UpdateInfo, progress: UpdateDownloadProgress) {
        latestProgress = progress
        val now = System.currentTimeMillis()
        val shouldPersist = progress.stage != lastProgressStage || now - lastProgressPersistedAt >= PROGRESS_PERSIST_INTERVAL_MS
        if (shouldPersist) {
            lastProgressStage = progress.stage
            lastProgressPersistedAt = now
            progressPersistJob?.cancel()
            progressPersistJob = serviceScope.launch { persistProgress(info, progress) }
        }
        postProgressNotification(progress)
    }

    private suspend fun persistProgress(info: UpdateInfo, progress: UpdateDownloadProgress) {
        val current = settingsRepository.settings.first()
        persist(
            current.copy(
                updateTransferActive = true,
                updateTransferTagName = info.tagName,
                updateTransferVersionName = info.versionName,
                updateTransferDownloadUrl = info.downloadUrl,
                updateTransferSha256Url = info.sha256Url,
                updateTransferReleaseNotes = info.releaseNotes,
                updateTransferStage = progress.stage.name,
                updateTransferDownloadedBytes = progress.downloadedBytes,
                updateTransferTotalBytes = progress.totalBytes,
                updateTransferStatus = progress.stage.statusText(info.versionName),
                updateTransferError = null,
            ),
        )
    }

    private suspend fun markFailure(message: String) {
        persist(settingsRepository.settings.first().copy(updateTransferActive = false, updateTransferError = message, updateTransferStatus = null))
    }

    private suspend fun persist(settings: AppSettings) {
        settingsRepository.update { settings }
        postProgressNotification(latestProgress)
    }

    @SuppressLint("MissingPermission")
    private fun postProgressNotification(progress: UpdateDownloadProgress?) {
        if (!canPostNotifications(this)) return
        val text = progress?.stage?.let {
            when (it) {
                UpdateDownloadStage.CONNECTING -> "正在连接更新服务器…"
                UpdateDownloadStage.RESUMING -> "正在从断点继续下载…"
                UpdateDownloadStage.DOWNLOADING -> "正在下载更新包…"
                UpdateDownloadStage.VERIFYING -> "正在校验 SHA-256…"
                UpdateDownloadStage.CERTIFICATE -> "正在校验版本与签名证书…"
            }
        } ?: "正在下载更新…"
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(text, progress))
        }
    }

    private fun buildNotification(text: String, progress: UpdateDownloadProgress?): Notification {
        val total = progress?.totalBytes?.takeIf { it > 0L }
        val downloaded = progress?.downloadedBytes?.coerceAtMost(total ?: Long.MAX_VALUE) ?: 0L
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Learn List 更新")
            .setContentText(text)
            .setProgress(total?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt() ?: 0, downloaded.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), total == null)
            .setContentIntent(PendingIntent.getActivity(this, NOTIFICATION_ID, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .addAction(NotificationCompat.Action(R.drawable.ic_notification, "暂停", PendingIntent.getService(this, NOTIFICATION_ID + 1, Intent(this, UpdateDownloadService::class.java).setAction(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    private fun buildStartupNotification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Learn List 更新")
        .setContentText("正在准备更新…")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setSilent(true)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .build()

    private fun stopService(startId: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelfResult(startId)
    }

    private fun Intent.toUpdateInfo(): UpdateInfo? = UpdateInfo(
        tagName = getStringExtra(EXTRA_TAG_NAME).orEmpty(),
        versionName = getStringExtra(EXTRA_VERSION_NAME).orEmpty(),
        downloadUrl = getStringExtra(EXTRA_DOWNLOAD_URL).orEmpty(),
        sha256Url = getStringExtra(EXTRA_SHA256_URL),
        releaseNotes = getStringExtra(EXTRA_RELEASE_NOTES).orEmpty(),
    ).takeIf { it.tagName.isNotBlank() && it.versionName.isNotBlank() && it.downloadUrl.isNotBlank() && it.sha256Url != null }

    private fun AppSettings.toUpdateInfo(): UpdateInfo? = UpdateInfo(
        tagName = updateTransferTagName.orEmpty(),
        versionName = updateTransferVersionName.orEmpty(),
        downloadUrl = updateTransferDownloadUrl.orEmpty(),
        sha256Url = updateTransferSha256Url,
        releaseNotes = updateTransferReleaseNotes,
    ).takeIf { it.tagName.isNotBlank() && it.versionName.isNotBlank() && it.downloadUrl.isNotBlank() && it.sha256Url != null }

    private fun UpdateDownloadStage.statusText(versionName: String): String = when (this) {
        UpdateDownloadStage.CONNECTING -> "正在连接 GitHub Release…"
        UpdateDownloadStage.RESUMING -> "正在从断点继续下载 v$versionName…"
        UpdateDownloadStage.DOWNLOADING -> "正在下载 v$versionName…"
        UpdateDownloadStage.VERIFYING -> "正在校验 SHA-256…"
        UpdateDownloadStage.CERTIFICATE -> "正在校验版本与签名证书…"
    }

    private fun canPostNotifications(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureNotificationChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "应用更新", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                enableVibration(false)
                description = "显示应用更新的下载进度"
            },
        )
    }

    companion object {
        const val ACTION_START = "com.mymoss.learnlist.action.UPDATE_START"
        const val ACTION_INSTALL = "com.mymoss.learnlist.action.UPDATE_INSTALL"
        const val ACTION_PAUSE = "com.mymoss.learnlist.action.UPDATE_PAUSE"
        const val ACTION_SYNC = "com.mymoss.learnlist.action.UPDATE_SYNC"
        const val EXTRA_TAG_NAME = "update_tag_name"
        const val EXTRA_VERSION_NAME = "update_version_name"
        const val EXTRA_DOWNLOAD_URL = "update_download_url"
        const val EXTRA_SHA256_URL = "update_sha256_url"
        const val EXTRA_RELEASE_NOTES = "update_release_notes"
        const val CHANNEL_ID = "app_update_download"
        const val NOTIFICATION_ID = 3001
        private const val PROGRESS_PERSIST_INTERVAL_MS = 500L
        private const val UPDATE_PHASE_INSTALLING = "INSTALLING"

        fun start(context: Context, info: UpdateInfo) {
            val intent = Intent(context.applicationContext, UpdateDownloadService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_TAG_NAME, info.tagName)
                .putExtra(EXTRA_VERSION_NAME, info.versionName)
                .putExtra(EXTRA_DOWNLOAD_URL, info.downloadUrl)
                .putExtra(EXTRA_SHA256_URL, info.sha256Url)
                .putExtra(EXTRA_RELEASE_NOTES, info.releaseNotes)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun pause(context: Context) {
            val intent = Intent(context.applicationContext, UpdateDownloadService::class.java).setAction(ACTION_PAUSE)
            runCatching { context.applicationContext.startService(intent) }
        }

        fun installCached(context: Context, info: UpdateInfo) {
            val intent = Intent(context.applicationContext, UpdateDownloadService::class.java)
                .setAction(ACTION_INSTALL)
                .putExtra(EXTRA_TAG_NAME, info.tagName)
                .putExtra(EXTRA_VERSION_NAME, info.versionName)
                .putExtra(EXTRA_DOWNLOAD_URL, info.downloadUrl)
                .putExtra(EXTRA_SHA256_URL, info.sha256Url)
                .putExtra(EXTRA_RELEASE_NOTES, info.releaseNotes)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun sync(context: Context) {
            val intent = Intent(context.applicationContext, UpdateDownloadService::class.java).setAction(ACTION_SYNC)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }
    }
}
