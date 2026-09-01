package com.mymoss.learnlist.system

import android.Manifest
import android.app.Notification
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
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.MainActivity
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.domain.FocusPhaseType
import com.mymoss.learnlist.domain.PomodoroCycle
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

/** Keeps a running Pomodoro phase visible and recoverable while the app is backgrounded. */
class FocusTimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(this)
        runCatching {
            // Promote the service with a deliberately small notification first. The
            // full notification creates several PendingIntents and is posted after
            // the service has met Android's foreground-start deadline.
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
                val startedAt = intent.getLongExtra(EXTRA_STARTED_AT, Long.MIN_VALUE)
                val endAt = intent.getLongExtra(EXTRA_END_AT, Long.MIN_VALUE)
                val plannedMinutes = intent.getIntExtra(EXTRA_PLANNED_MINUTES, 0)
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "WORK"
                val round = intent.getIntExtra(EXTRA_ROUND, 1)
                if (startedAt > 0L && endAt > startedAt && plannedMinutes in 1..180) {
                    startTimer(startedAt, endAt, plannedMinutes, phase, round)
                } else {
                    stopService(startId)
                }
            }
            ACTION_STOP -> stopCurrentTimer(startId)
            ACTION_PAUSE -> pauseCurrentTimer(startId)
            ACTION_SKIP -> skipCurrentPhase(startId)
            ACTION_RESUME -> resumeCurrentPhase(startId)
            ACTION_SYNC, null -> recoverTimer(startId)
            else -> recoverTimer(startId)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun startTimer(startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String, round: Int) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val remainingSeconds = ((endAt - System.currentTimeMillis()) / 1000L).toInt()
                if (remainingSeconds <= 0) {
                    completeTimer(startedAt, endAt, plannedMinutes, phase, round)
                    return@launch
                }
                postProgressNotification(startedAt, endAt, plannedMinutes, phase, round)
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
            if (settings.focusPaused || startedAt == null || endAt == null || endAt <= startedAt) {
                stopService(startId)
            } else {
                startTimer(
                    startedAt = startedAt,
                    endAt = endAt,
                    plannedMinutes = settings.focusPlannedMinutes.coerceIn(1, 180),
                    phase = settings.focusPhase,
                    round = settings.focusRound,
                )
            }
        }
    }

    private fun pauseCurrentTimer(startId: Int) {
        timerJob?.cancel()
        serviceScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val settings = settingsRepository.settings.first()
            val startedAt = settings.focusStartedAtEpochMillis
            val endAt = settings.focusEndAtEpochMillis
            if (startedAt != null && endAt != null && endAt > startedAt) {
                val now = System.currentTimeMillis()
                val elapsed = ((now - startedAt) / 1000L).toInt().coerceAtLeast(0)
                val remaining = ((endAt - now) / 1000L).toInt().coerceAtLeast(0)
                settingsRepository.update {
                    it.copy(
                        focusStartedAtEpochMillis = null,
                        focusEndAtEpochMillis = null,
                        focusSessionStartedAtEpochMillis = it.focusSessionStartedAtEpochMillis ?: startedAt,
                        focusRemainingSeconds = remaining,
                        focusAccumulatedSeconds = it.focusAccumulatedSeconds + elapsed,
                        focusPaused = true,
                    )
                }
                FocusTimerScheduler(applicationContext).cancel()
            }
            stopService(startId)
        }
    }

    private fun resumeCurrentPhase(startId: Int) {
        serviceScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val settings = settingsRepository.settings.first()
            if (!settings.focusPaused || settings.focusRemainingSeconds <= 0) {
                stopService(startId)
                return@launch
            }
            val startedAt = System.currentTimeMillis()
            val endAt = startedAt + settings.focusRemainingSeconds * 1000L
            settingsRepository.update {
                it.copy(
                    focusStartedAtEpochMillis = startedAt,
                    focusEndAtEpochMillis = endAt,
                    focusSessionStartedAtEpochMillis = it.focusSessionStartedAtEpochMillis ?: startedAt,
                    focusPaused = false,
                )
            }
            FocusTimerScheduler(applicationContext).schedule(endAt)
            startTimer(startedAt, endAt, settings.focusPlannedMinutes.coerceIn(1, 180), settings.focusPhase, settings.focusRound)
        }
    }

    private fun skipCurrentPhase(startId: Int) {
        timerJob?.cancel()
        serviceScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val settings = settingsRepository.settings.first()
            val current = PomodoroCyclePhase.from(settings.focusPhase, settings.focusRound, settings.focusRemainingSeconds)
            val next = PomodoroCycle.skipped(current)
            settingsRepository.update {
                it.copy(
                    focusStartedAtEpochMillis = null,
                    focusEndAtEpochMillis = null,
                    focusRemainingSeconds = next.totalSeconds,
                    focusAccumulatedSeconds = 0,
                    focusPaused = false,
                    focusPhase = next.type.name,
                    focusRound = next.round,
                )
            }
            FocusTimerScheduler(applicationContext).cancel()
            stopService(startId)
        }
    }

    private fun stopCurrentTimer(startId: Int) {
        timerJob?.cancel()
        serviceScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            val settings = settingsRepository.settings.first()
            val startedAt = settings.focusStartedAtEpochMillis
            val endAt = settings.focusEndAtEpochMillis
            if (startedAt != null && endAt != null && !settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
                stopService(startId)
                return@launch
            }
            val now = System.currentTimeMillis()
            val elapsed = if (startedAt != null && endAt != null) {
                ((now - startedAt) / 1000L).toInt().coerceAtLeast(0)
            } else 0
            val plannedMinutes = settings.focusPlannedMinutes.coerceIn(1, 180)
            val actualSeconds = (settings.focusAccumulatedSeconds + elapsed).coerceIn(0, plannedMinutes * 60)
            val sessionStartedAt = settings.focusSessionStartedAtEpochMillis
                ?: startedAt
                ?: (now - actualSeconds * 1000L).coerceAtLeast(0L)
            if (actualSeconds > 0 && settings.focusPhase == FocusPhaseType.WORK.name) {
                (application as LearnListApplication).repository.recordFocusSessionIfNeeded(
                    plannedMinutes = plannedMinutes,
                    actualMinutes = actualSeconds / 60,
                    actualSeconds = actualSeconds,
                    projectId = settings.focusProjectId,
                    taskId = settings.focusTaskId,
                    startedAt = sessionStartedAt,
                    phase = settings.focusPhase,
                    round = settings.focusRound,
                )
            }
            settingsRepository.update {
                it.copy(
                    focusStartedAtEpochMillis = null,
                    focusEndAtEpochMillis = null,
                    focusSessionStartedAtEpochMillis = null,
                    focusPlannedMinutes = PomodoroCycle.WORK_SECONDS / 60,
                    focusRemainingSeconds = PomodoroCycle.WORK_SECONDS,
                    focusAccumulatedSeconds = 0,
                    focusPaused = false,
                    focusPhase = FocusPhaseType.WORK.name,
                    focusRound = 1,
                )
            }
            FocusTimerScheduler(applicationContext).cancel()
            stopService(startId)
        }
    }

    private suspend fun completeTimer(startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String, round: Int) {
        val application = application as LearnListApplication
        val settingsRepository = SettingsRepository(applicationContext)
        val settings = settingsRepository.settings.first()
        if (settings.focusStartedAtEpochMillis != startedAt || settings.focusEndAtEpochMillis != endAt) {
            stopService()
            return
        }
        // AlarmManager and the foreground loop can observe the same deadline. Claim
        // the timer before recording or advancing the next phase so only one path
        // can emit feedback and mutate the Pomodoro state machine.
        if (!settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
            stopService()
            return
        }
        val elapsed = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L).toInt()
        val actualSeconds = (settings.focusAccumulatedSeconds + elapsed).coerceIn(0, plannedMinutes * 60)
        val phaseType = runCatching { FocusPhaseType.valueOf(phase) }.getOrDefault(FocusPhaseType.WORK)
        val sessionStartedAt = settings.focusSessionStartedAtEpochMillis ?: startedAt
        if (PomodoroCycle.isCountedAsFocus(phaseType)) {
            application.repository.recordFocusSessionIfNeeded(
                plannedMinutes = plannedMinutes,
                actualMinutes = actualSeconds / 60,
                actualSeconds = actualSeconds,
                projectId = settings.focusProjectId,
                taskId = settings.focusTaskId,
                startedAt = sessionStartedAt,
                phase = phase,
                round = round,
            )
        }
        val next = PomodoroCycle.afterCompleted(PomodoroCyclePhase.from(phase, round, plannedMinutes * 60))
        val autoStart = settings.focusAutoStartBreaks
        FeedbackManager.play(applicationContext, settings, FeedbackManager.FeedbackContext.FOCUS)
        postCompletionNotification(applicationContext, plannedMinutes, phase)
        if (autoStart) {
            val nextStart = System.currentTimeMillis()
            val nextEnd = nextStart + next.totalSeconds * 1000L
            settingsRepository.update {
                it.copy(
                    focusStartedAtEpochMillis = nextStart,
                    focusEndAtEpochMillis = nextEnd,
                    focusSessionStartedAtEpochMillis = if (next.type == FocusPhaseType.WORK) nextStart else null,
                    focusPlannedMinutes = next.totalSeconds / 60,
                    focusRemainingSeconds = next.totalSeconds,
                    focusAccumulatedSeconds = 0,
                    focusPaused = false,
                    focusPhase = next.type.name,
                    focusRound = next.round,
                )
            }
            FocusTimerScheduler(applicationContext).schedule(nextEnd)
            startTimer(nextStart, nextEnd, (next.totalSeconds / 60).coerceAtLeast(1), next.type.name, next.round)
        } else {
            settingsRepository.update {
                it.copy(
                    focusStartedAtEpochMillis = null,
                    focusEndAtEpochMillis = null,
                    focusSessionStartedAtEpochMillis = null,
                    focusPlannedMinutes = next.totalSeconds / 60,
                    focusRemainingSeconds = next.totalSeconds,
                    focusAccumulatedSeconds = 0,
                    focusPaused = false,
                    focusPhase = next.type.name,
                    focusRound = next.round,
                )
            }
            FocusTimerScheduler(applicationContext).cancel()
            stopService()
        }
    }

    private fun postProgressNotification(startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String, round: Int) {
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildProgressNotification(startedAt, endAt, plannedMinutes, phase, round))
        } catch (_: SecurityException) {
            // Notification permission changes must not interrupt the timer.
        }
    }

    private fun buildProgressNotification(startedAt: Long?, endAt: Long?, plannedMinutes: Int, phase: String, round: Int): Notification {
        val totalSeconds = (plannedMinutes * 60).coerceAtLeast(1)
        val remainingSeconds = if (startedAt != null && endAt != null) {
            ((endAt - System.currentTimeMillis()) / 1000L).toInt().coerceIn(0, totalSeconds)
        } else 0
        val phaseTitle = when (phase) {
            FocusPhaseType.SHORT_BREAK.name -> "短休息"
            FocusPhaseType.LONG_BREAK.name -> "长休息"
            else -> "专注进行中"
        }
        val contentText = if (startedAt != null && endAt != null) {
            "第 ${round.coerceIn(1, 4)} 轮 · 剩余 ${formatRemaining(remainingSeconds)}"
        } else "正在恢复计时…"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(phaseTitle)
            .setContentText(contentText)
            .setProgress(totalSeconds, remainingSeconds, false)
            .setContentIntent(openAppPendingIntent(this))
            .addAction(NotificationCompat.Action(R.drawable.ic_notification, "暂停", pausePendingIntent(this)))
            .addAction(NotificationCompat.Action(R.drawable.ic_notification, "结束", stopPendingIntent(this)))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun buildStartupNotification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("专注计时")
        .setContentText("正在准备番茄钟…")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setSilent(true)
        .setShowWhen(false)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()

    private fun stopService(startId: Int? = null) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (startId == null) stopSelf() else stopSelfResult(startId)
    }

    companion object {
        const val ACTION_START = "com.mymoss.learnlist.action.FOCUS_START"
        const val ACTION_STOP = "com.mymoss.learnlist.action.FOCUS_STOP"
        const val ACTION_PAUSE = "com.mymoss.learnlist.action.FOCUS_PAUSE"
        const val ACTION_RESUME = "com.mymoss.learnlist.action.FOCUS_RESUME"
        const val ACTION_SKIP = "com.mymoss.learnlist.action.FOCUS_SKIP"
        const val ACTION_SYNC = "com.mymoss.learnlist.action.FOCUS_SYNC"
        const val EXTRA_STARTED_AT = "focus_started_at"
        const val EXTRA_END_AT = "focus_end_at"
        const val EXTRA_PLANNED_MINUTES = "focus_planned_minutes"
        const val EXTRA_PHASE = "focus_phase"
        const val EXTRA_ROUND = "focus_round"
        const val CHANNEL_ID = "focus_timer_status"
        const val NOTIFICATION_ID = 2002
        private const val COMPLETION_NOTIFICATION_ID = 2001

        fun start(context: Context, startedAt: Long, endAt: Long, plannedMinutes: Int, phase: String = "WORK", round: Int = 1) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_STARTED_AT, startedAt)
                .putExtra(EXTRA_END_AT, endAt)
                .putExtra(EXTRA_PLANNED_MINUTES, plannedMinutes)
                .putExtra(EXTRA_PHASE, phase)
                .putExtra(EXTRA_ROUND, round)
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun sync(context: Context) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java).setAction(ACTION_SYNC)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun stop(context: Context) = sendRunningServiceAction(context, ACTION_STOP)
        fun pause(context: Context) = sendRunningServiceAction(context, ACTION_PAUSE)
        fun resume(context: Context) = sendForegroundServiceAction(context, ACTION_RESUME)
        fun skip(context: Context) = sendRunningServiceAction(context, ACTION_SKIP)

        private fun sendRunningServiceAction(context: Context, action: String) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java).setAction(action)
            runCatching { context.applicationContext.startService(intent) }
        }

        private fun sendForegroundServiceAction(context: Context, action: String) {
            val intent = Intent(context.applicationContext, FocusTimerService::class.java).setAction(action)
            runCatching { ContextCompat.startForegroundService(context.applicationContext, intent) }
        }

        fun formatRemaining(totalSeconds: Int): String {
            val safeSeconds = totalSeconds.coerceAtLeast(0)
            return String.format(Locale.getDefault(), "%02d:%02d", safeSeconds / 60, safeSeconds % 60)
        }

        fun postCompletionNotification(context: Context, plannedMinutes: Int, phase: String = "WORK") {
            if (!canPostNotifications(context)) return
            ReminderReceiver.ensureNotificationChannel(context)
            val title = when (phase) {
                FocusPhaseType.SHORT_BREAK.name -> "短休息结束"
                FocusPhaseType.LONG_BREAK.name -> "长休息结束"
                else -> "专注完成"
            }
            val message = if (phase == FocusPhaseType.WORK.name) {
                "${plannedMinutes.coerceAtLeast(1)} 分钟专注已结束，休息一下吧"
            } else "休息结束，准备好下一轮了吗？"
            val notification = NotificationCompat.Builder(context, ReminderReceiver.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(openAppPendingIntent(context))
                .setAutoCancel(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(COMPLETION_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Notification permission can be revoked between the check and the call.
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

        private fun pausePendingIntent(context: Context): PendingIntent = PendingIntent.getService(
            context,
            NOTIFICATION_ID + 2,
            Intent(context, FocusTimerService::class.java).setAction(ACTION_PAUSE),
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
                    description = "显示正在进行的番茄钟阶段和剩余时间"
                },
            )
        }
    }
}

private object PomodoroCyclePhase {
    fun from(phase: String, round: Int, remainingSeconds: Int) = com.mymoss.learnlist.domain.PomodoroPhase(
        type = runCatching { FocusPhaseType.valueOf(phase) }.getOrDefault(FocusPhaseType.WORK),
        round = round.coerceIn(1, PomodoroCycle.ROUNDS_PER_CYCLE),
        totalSeconds = remainingSeconds.coerceAtLeast(1),
    )
}
