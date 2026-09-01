package com.mymoss.learnlist

import android.app.NotificationManager
import android.content.Intent
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
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertNotNull(
                context.getSystemService(NotificationManager::class.java)
                    .getNotificationChannel(FocusTimerService.CHANNEL_ID),
            )
        } finally {
            context.stopService(Intent(context, FocusTimerService::class.java))
        }
    }
}
