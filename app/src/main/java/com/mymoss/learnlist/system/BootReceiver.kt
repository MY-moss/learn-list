package com.mymoss.learnlist.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mymoss.learnlist.BuildConfig
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)) return
        val pendingResult = goAsync()
        val application = context.applicationContext as LearnListApplication
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                ReminderScheduler(context.applicationContext, application.repository).rescheduleAll()
                val settings = SettingsRepository(context.applicationContext).settings.first()
                if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED && settings.pendingUpdateVersionName == BuildConfig.VERSION_NAME) {
                    SettingsRepository(context.applicationContext).update {
                        it.copy(
                            pendingUpdateVersionName = null,
                            updateTransferActive = false,
                            updateTransferTagName = null,
                            updateTransferVersionName = null,
                            updateTransferDownloadUrl = null,
                            updateTransferSha256Url = null,
                            updateTransferReleaseNotes = "",
                            updateTransferStage = null,
                            updateTransferDownloadedBytes = 0L,
                            updateTransferTotalBytes = null,
                            updateTransferStatus = "v${BuildConfig.VERSION_NAME} 已安装完成",
                            updateTransferError = null,
                        )
                    }
                }
                settings.focusEndAtEpochMillis?.let { FocusTimerScheduler(context.applicationContext).schedule(it) }
                if (settings.focusStartedAtEpochMillis != null && settings.focusEndAtEpochMillis != null) {
                    FocusTimerService.sync(context.applicationContext)
                }
                if (settings.updateTransferActive) {
                    UpdateDownloadService.sync(context.applicationContext)
                }
            }
            pendingResult.finish()
        }
    }
}
