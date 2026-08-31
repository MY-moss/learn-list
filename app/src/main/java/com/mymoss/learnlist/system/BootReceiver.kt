package com.mymoss.learnlist.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
                settings.focusEndAtEpochMillis?.let { FocusTimerScheduler(context.applicationContext).schedule(it) }
            }
            pendingResult.finish()
        }
    }
}
