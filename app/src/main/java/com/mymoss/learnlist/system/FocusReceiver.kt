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
                    if (settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
                        FeedbackManager.play(context.applicationContext, settings)
                        FocusTimerService.postCompletionNotification(context.applicationContext, plannedMinutes)
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
