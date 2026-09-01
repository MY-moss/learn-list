package com.mymoss.learnlist.system

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.MainActivity
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/** Keeps a running focus session visible and alive while the app is backgrounded. */
class FocusTimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(this)
        runCatching { startForeground(NOTIFICATION_ID, buildProgressNotification(null, null, 0)) }
            .onFailure { stopSelf() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val startedAt = intent.getLongExtra(EXTRA_STARTED_AT, Long.MIN_VALUE)
                val endAt = intent.getLongExtra(EXTRA_END_AT, Long.MIN_VALUE)
                val plannedMinutes = intent.getIntExtra(EXTRA_PLANNED_MINUTES, 0)
                if (startedAt > 0L && endAt > startedAt && plannedMinutes in 1..180) {
                    startTimer(startedAt, endAt, plannedMinutes)
                } else {
                    stopService(startId)
                }
            }

            ACTION_STOP -> stopCurrentTimer(startId)
            ACTION_SYNC, null -> recoverTimer(startId)
            else -> recoverTimer(startId)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        runCatching { NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID) }
        super.onDestroy()
    }

    private fun startTimer(startedAt: Long, endAt: Long, plannedMinutes: Int) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val remainingSeconds = ((endAt - System.currentTimeMillis()) / 1000L).toInt()
                if (remainingSeconds <= 0) {
                    completeTimer(startedAt, endAt, plannedMinutes)
                    return@launch
                }
                postProgressNotification(startedAt, endAt, plannedMinutes)
                delay(1000L)
            }
        }
    }

    private fun recoverTimer(startId: Int) {
        timerJob?.cancel()
        serviceScope.launch {
            val settings = SettingsRepository(applicationContext).settings.first()
            val startedAt = settings.focusStartedAtEpochMillis
            val endAt = settings.focusEndAtEpochMillis
            if (startedAt == null || endAt == null || endAt <= startedAt) {
                stopService(startId)
            } else {
                startTimer(startedAt, endAt, settings.focusPlannedMinutes.coerceIn(1, 180))
            }
        }
    }

    private fun stopCurrentTimer(startId: Int) {
        timerJob?.cancel()
        serviceScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val settings = settingsRepository.settings.first()
            val startedAt = settings.focusStartedAtEpochMillis
            val endAt = settings.focusEndAtEpochMillis
            if (startedAt != null && endAt != null && endAt > startedAt) {
                val actualMinutes = ((System.currentTimeMillis() - startedAt) / 60_000L).toInt().coerceAtLeast(0)
                (application as LearnListApplication).repository.recordFocusSessionIfNeeded(
                    plannedMinutes = settings.focusPlannedMinutes.coerceIn(1, 180),
                    actualMinutes = actualMinutes,
                    startedAt = startedAt,
                )
                if (settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
                    FocusTimerScheduler(applicationContext).cancel()
                }
            }
            stopService(startId)
        }
    }

    private suspend fun completeTimer(startedAt: Long, endAt: Long, plannedMinutes: Int) {
        val application = application as LearnListApplication
        val settingsRepository = SettingsRepository(applicationContext)
        val settings = settingsRepository.settings.first()
        if (settings.focusStartedAtEpochMillis != startedAt || settings.focusEndAtEpochMillis != endAt) {
            stopService()
            return
        }
        application.repository.recordFocusSessionIfNeeded(
            plannedMinutes = plannedMinutes,
            actualMinutes = plannedMinutes,
            startedAt = startedAt,
        )
        if (settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
            FocusTimerScheduler(applicationContext).cancel()
            FeedbackManager.play(applicationContext, settings)
            postCompletionNotification(applicationContext, plannedMinutes)
        }
        stopService()
    }

    private fun postProgressNotification(startedAt: Long, endAt: Long, plannedMinutes: Int) {
        val notification = buildProgressNotification(startedAt, endAt, plannedMinutes)
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // A notification permission change must not interrupt the timer; the alarm remains as a fallback.
        }
    }

    private fun buildProgressNotification(startedAt: Long?, endAt: Long?, plannedMinutes: Int): Notification {
        val totalSeconds = (plannedMinutes * 60).coerceAtLeast(1)
        val remainingSeconds = if (startedAt != null && endAt != null) {
            ((endAt - System.currentTimeMillis()) / 1000L).toInt().coerceIn(0, totalSeconds)
        } else {
            0
        }
        val contentText = if (startedAt != null && endAt != null) {
            "剩余 ${formatRemaining(remainingSeconds)} · ${plannedMinutes} 分钟专注"
        } else {
            "正在恢复专注计时…"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("专注进行中")
            .setContentText(contentText)
            .setProgress(totalSeconds, remainingSeconds, false)
            .setContentIntent(openAppPendingIntent(this))
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_notification,
                    "结束",
                    stopPendingIntent(this),
                ),
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun stopService(startId: Int? = null) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (startId == null) stopSelf() else stopSelfResult(startId)
    }

    companion object {
        const val ACTION_START = "com.mymoss.learnlist.action.FOCUS_START"
        const val ACTION_STOP = "com.mymoss.learnlist.action.FOCUS_STOP"
        const val ACTION_SYNC = "com.mymoss.learnlist.action.FOCUS_SYNC"
        const val EXTRA_STARTED_AT = "focus_started_at"
        const val EXTRA_END_AT = "focus_end_at"
        const val EXTRA_PLANNED_MINUTES = "focus_planned_minutes"
        const val CHANNEL_ID = "focus_timer_status"
        const val NOTIFICATION_ID = 2002
        private const val COMPLETION_NOTIFICATION_ID = 2001

        fun start(context: Context, startedAt: Long, endAt: Long, plannedMinutes: Int) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_STARTED_AT, startedAt)
                .putExtra(EXTRA_END_AT, endAt)
                .putExtra(EXTRA_PLANNED_MINUTES, plannedMinutes)
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun sync(context: Context) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java).setAction(ACTION_SYNC)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun stop(context: Context) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java).setAction(ACTION_STOP)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun formatRemaining(totalSeconds: Int): String {
            val safeSeconds = totalSeconds.coerceAtLeast(0)
            return String.format(Locale.getDefault(), "%02d:%02d", safeSeconds / 60, safeSeconds % 60)
        }

        fun postCompletionNotification(context: Context, plannedMinutes: Int) {
            if (!canPostNotifications(context)) return
            ReminderReceiver.ensureNotificationChannel(context)
            val openIntent = openAppPendingIntent(context)
            val notification = NotificationCompat.Builder(context, ReminderReceiver.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("专注完成")
                .setContentText("${plannedMinutes.coerceAtLeast(1)} 分钟专注已结束，休息一下吧")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(COMPLETION_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Notification permission can be revoked between the check and the post call.
            }
        }

        private fun openAppPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        private fun stopPendingIntent(context: Context): PendingIntent = PendingIntent.getService(
            context,
            NOTIFICATION_ID + 1,
            Intent(context, FocusTimerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        private fun canPostNotifications(context: Context): Boolean =
            android.os.Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        private fun ensureNotificationChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                android.app.NotificationChannel(CHANNEL_ID, "专注计时", NotificationManager.IMPORTANCE_LOW).apply {
                    setSound(null, null)
                    enableVibration(false)
                    description = "显示正在进行的番茄钟剩余时间"
                },
            )
        }
    }
}
