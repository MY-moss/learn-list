package com.mymoss.learnlist

import android.app.NotificationManager
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mymoss.learnlist.system.UpdateDownloadService
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateDownloadServiceInstrumentedTest {
    @Test
    fun serviceCreatesPersistentDownloadNotificationChannel() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, UpdateDownloadService::class.java)
            .setAction(UpdateDownloadService.ACTION_PAUSE)
        try {
            ContextCompat.startForegroundService(context, intent)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            var channel = notificationManager.getNotificationChannel(UpdateDownloadService.CHANNEL_ID)
            val deadline = SystemClock.uptimeMillis() + 5_000L
            while (channel == null && SystemClock.uptimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                SystemClock.sleep(100L)
                channel = notificationManager.getNotificationChannel(UpdateDownloadService.CHANNEL_ID)
            }
            assertNotNull(channel)
        } finally {
            context.stopService(Intent(context, UpdateDownloadService::class.java))
        }
    }
}
