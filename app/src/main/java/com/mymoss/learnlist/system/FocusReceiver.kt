package com.mymoss.learnlist.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.domain.FocusPhaseType
import com.mymoss.learnlist.domain.PomodoroCycle
import com.mymoss.learnlist.domain.PomodoroPhase
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
                    if (settingsRepository.clearFocusTimerIfMatches(startedAt, endAt)) {
                        val actualSeconds = (settings.focusAccumulatedSeconds + plannedMinutes * 60).coerceIn(0, plannedMinutes * 60)
                        val phase = runCatching { FocusPhaseType.valueOf(settings.focusPhase) }.getOrDefault(FocusPhaseType.WORK)
                        if (PomodoroCycle.isCountedAsFocus(phase)) {
                            application.repository.recordFocusSessionIfNeeded(
                                plannedMinutes = plannedMinutes,
                                actualMinutes = actualSeconds / 60,
                                actualSeconds = actualSeconds,
                                projectId = settings.focusProjectId,
                                taskId = settings.focusTaskId,
                                startedAt = settings.focusSessionStartedAtEpochMillis ?: startedAt,
                                phase = phase.name,
                                round = settings.focusRound,
                            )
                        }
                        val next = PomodoroCycle.afterCompleted(PomodoroPhase(phase, settings.focusRound, plannedMinutes * 60))
                        val autoStart = settings.focusAutoStartBreaks
                        val nextStart = if (autoStart) System.currentTimeMillis() else null
                        val nextEnd = nextStart?.plus(next.totalSeconds * 1000L)
                        settingsRepository.update {
                            it.copy(
                                focusStartedAtEpochMillis = nextStart,
                                focusEndAtEpochMillis = nextEnd,
                                focusSessionStartedAtEpochMillis = if (next.type == FocusPhaseType.WORK && nextStart != null) nextStart else null,
                                focusPlannedMinutes = next.totalSeconds / 60,
                                focusRemainingSeconds = next.totalSeconds,
                                focusAccumulatedSeconds = 0,
                                focusPaused = false,
                                focusPhase = next.type.name,
                                focusRound = next.round,
                            )
                        }
                        if (nextEnd != null) {
                            FocusTimerScheduler(context.applicationContext).schedule(nextEnd)
                            FocusTimerService.start(context.applicationContext, nextStart, nextEnd, next.totalSeconds / 60, next.type.name, next.round)
                        }
                        FeedbackManager.play(context.applicationContext, settings, FeedbackManager.FeedbackContext.FOCUS)
                        FocusTimerService.postCompletionNotification(context.applicationContext, plannedMinutes, phase.name)
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
