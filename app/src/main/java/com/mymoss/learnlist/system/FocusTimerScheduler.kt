package com.mymoss.learnlist.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Clock

/** Schedules one durable alarm for the currently running focus session. */
class FocusTimerScheduler(context: Context, private val clock: Clock = Clock.systemDefaultZone()) {
    private val context = context.applicationContext
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(endAtEpochMillis: Long) {
        if (endAtEpochMillis <= clock.millis()) {
            cancel()
            return
        }
        cancel()
        val pendingIntent = createPendingIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtEpochMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtEpochMillis, pendingIntent)
        }
    }

    fun cancel() {
        existingPendingIntent()?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun createPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(context, REQUEST_CODE, intent(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun existingPendingIntent(): PendingIntent? =
        PendingIntent.getBroadcast(context, REQUEST_CODE, intent(), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)

    private fun intent(): Intent = Intent(context, FocusReceiver::class.java)

    private companion object {
        const val REQUEST_CODE = 0x4C4C46
    }
}
