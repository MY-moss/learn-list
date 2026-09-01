package com.mymoss.learnlist

import android.app.NotificationManager
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mymoss.learnlist.system.FocusTimerService
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FocusTimerServiceInstrumentedTest {
    @Test
    fun serviceCreatesPersistentFocusNotificationChannel() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, FocusTimerService::class.java)
            .setAction(FocusTimerService.ACTION_START)
            .putExtra(FocusTimerService.EXTRA_STARTED_AT, System.currentTimeMillis())
            .putExtra(FocusTimerService.EXTRA_END_AT, System.currentTimeMillis() + 60_000L)
            .putExtra(FocusTimerService.EXTRA_PLANNED_MINUTES, 1)
        try {
            ContextCompat.startForegroundService(context, intent)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            var channel = notificationManager.getNotificationChannel(FocusTimerService.CHANNEL_ID)
            val deadline = SystemClock.uptimeMillis() + 5_000L
            while (channel == null && SystemClock.uptimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                SystemClock.sleep(100L)
                channel = notificationManager.getNotificationChannel(FocusTimerService.CHANNEL_ID)
            }
            assertNotNull(
                channel,
            )
            // Channel creation happens immediately before the service promotes
            // itself. Do not stop the service in that small window on slower
            // API 26 emulators.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            SystemClock.sleep(750L)
        } finally {
            context.stopService(Intent(context, FocusTimerService::class.java))
            // Give the main process a chance to finish onDestroy before the next
            // Activity test starts; otherwise Android may report the old FGS
            // promotion deadline in the following test.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            SystemClock.sleep(250L)
        }
    }
}
