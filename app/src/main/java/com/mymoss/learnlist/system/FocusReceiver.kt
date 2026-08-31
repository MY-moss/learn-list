package com.mymoss.learnlist.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.MainActivity
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val application = context.applicationContext as LearnListApplication
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val settingsRepository = SettingsRepository(context.applicationContext)
                val settings = settingsRepository.settings.first()
                val endAt = settings.focusEndAtEpochMillis
                val startedAt = settings.focusStartedAtEpochMillis
                if (endAt != null && startedAt != null && endAt <= System.currentTimeMillis()) {
                    val plannedMinutes = settings.focusPlannedMinutes.coerceIn(1, 180)
                    application.repository.recordFocusSessionIfNeeded(
                        plannedMinutes = plannedMinutes,
                        actualMinutes = plannedMinutes,
                        startedAt = startedAt,
                    )
                    settingsRepository.update { current ->
                        if (current.focusStartedAtEpochMillis == startedAt && current.focusEndAtEpochMillis == endAt) {
                            current.copy(focusStartedAtEpochMillis = null, focusEndAtEpochMillis = null)
                        } else {
                            current
                        }
                    }
                    postNotification(context.applicationContext, plannedMinutes)
                }
            }
            pendingResult.finish()
        }
    }

    private fun postNotification(context: Context, plannedMinutes: Int) {
        if (FocusBuildPermission.canPost(context)) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(ReminderReceiver.CHANNEL_ID, "学习提醒", NotificationManager.IMPORTANCE_DEFAULT),
            )
            val openIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, ReminderReceiver.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("专注完成")
                .setContentText("${plannedMinutes.coerceAtLeast(1)} 分钟专注已结束，休息一下吧")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
                // Notification permission can be revoked between the check and the post call.
            }
        }
    }

    private companion object {
        const val NOTIFICATION_ID = 2001
    }
}

private object FocusBuildPermission {
    fun canPost(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
