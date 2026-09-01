package com.mymoss.learnlist.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.learnListDataStore by preferencesDataStore(name = "learn_list_settings")

data class AppSettings(
    val reviewLimit: Int = 20,
    val summaryReminderEnabled: Boolean = true,
    val summaryReminderMinutes: Int = 20 * 60,
    val quietStartMinutes: Int = 22 * 60,
    val quietEndMinutes: Int = 7 * 60,
    val restDaysCsv: String = "",
    val lastUpdateCheckEpochMillis: Long = 0L,
    /** Latest release the user chose to temporarily dismiss from automatic checks. */
    val dismissedUpdateVersionName: String? = null,
    val pendingUpdateVersionName: String? = null,
    val updateTransferActive: Boolean = false,
    val updateTransferTagName: String? = null,
    val updateTransferVersionName: String? = null,
    val updateTransferDownloadUrl: String? = null,
    val updateTransferSha256Url: String? = null,
    val updateTransferReleaseNotes: String = "",
    val updateTransferStage: String? = null,
    val updateTransferDownloadedBytes: Long = 0L,
    val updateTransferTotalBytes: Long? = null,
    val updateTransferStatus: String? = null,
    val updateTransferError: String? = null,
    val focusStartedAtEpochMillis: Long? = null,
    val focusEndAtEpochMillis: Long? = null,
    /** Original start of the current Pomodoro work item, retained across pauses/resumes. */
    val focusSessionStartedAtEpochMillis: Long? = null,
    val focusPlannedMinutes: Int = 25,
    val focusRemainingSeconds: Int = 25 * 60,
    val focusAccumulatedSeconds: Int = 0,
    val focusPaused: Boolean = false,
    val focusPhase: String = "WORK",
    val focusRound: Int = 1,
    val focusAutoStartBreaks: Boolean = false,
    val focusProjectId: String? = null,
    val focusTaskId: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    /** Per-context override: GLOBAL, SOUND, VIBRATION, BOTH or OFF. */
    val focusFeedbackMode: String = "GLOBAL",
    val reminderFeedbackMode: String = "GLOBAL",
    val countdownFeedbackMode: String = "GLOBAL",
    val feedbackAudioPath: String? = null,
    val feedbackAudioName: String? = null,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val reviewLimit = intPreferencesKey("review_limit")
        val summaryEnabled = booleanPreferencesKey("summary_enabled")
        val summaryMinutes = intPreferencesKey("summary_minutes")
        val quietStart = intPreferencesKey("quiet_start")
        val quietEnd = intPreferencesKey("quiet_end")
        val restDays = stringPreferencesKey("rest_days")
        val lastUpdateCheck = longPreferencesKey("last_update_check")
        val dismissedUpdateVersion = stringPreferencesKey("dismissed_update_version")
        val pendingUpdateVersion = stringPreferencesKey("pending_update_version")
        val updateTransferActive = booleanPreferencesKey("update_transfer_active")
        val updateTransferTagName = stringPreferencesKey("update_transfer_tag")
        val updateTransferVersionName = stringPreferencesKey("update_transfer_version")
        val updateTransferDownloadUrl = stringPreferencesKey("update_transfer_download_url")
        val updateTransferSha256Url = stringPreferencesKey("update_transfer_sha256_url")
        val updateTransferReleaseNotes = stringPreferencesKey("update_transfer_release_notes")
        val updateTransferStage = stringPreferencesKey("update_transfer_stage")
        val updateTransferDownloadedBytes = longPreferencesKey("update_transfer_downloaded_bytes")
        val updateTransferTotalBytes = longPreferencesKey("update_transfer_total_bytes")
        val updateTransferStatus = stringPreferencesKey("update_transfer_status")
        val updateTransferError = stringPreferencesKey("update_transfer_error")
        val focusStartedAt = longPreferencesKey("focus_started_at")
        val focusEndAt = longPreferencesKey("focus_end_at")
        val focusSessionStartedAt = longPreferencesKey("focus_session_started_at")
        val focusPlannedMinutes = intPreferencesKey("focus_planned_minutes")
        val focusRemainingSeconds = intPreferencesKey("focus_remaining_seconds")
        val focusAccumulatedSeconds = intPreferencesKey("focus_accumulated_seconds")
        val focusPaused = booleanPreferencesKey("focus_paused")
        val focusPhase = stringPreferencesKey("focus_phase")
        val focusRound = intPreferencesKey("focus_round")
        val focusAutoStartBreaks = booleanPreferencesKey("focus_auto_start_breaks")
        val focusProjectId = stringPreferencesKey("focus_project_id")
        val focusTaskId = stringPreferencesKey("focus_task_id")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val focusFeedbackMode = stringPreferencesKey("focus_feedback_mode")
        val reminderFeedbackMode = stringPreferencesKey("reminder_feedback_mode")
        val countdownFeedbackMode = stringPreferencesKey("countdown_feedback_mode")
        val feedbackAudioPath = stringPreferencesKey("feedback_audio_path")
        val feedbackAudioName = stringPreferencesKey("feedback_audio_name")
    }

    val settings: Flow<AppSettings> = context.learnListDataStore.data.map { values ->
        AppSettings(
            reviewLimit = values[Keys.reviewLimit] ?: 20,
            summaryReminderEnabled = values[Keys.summaryEnabled] ?: true,
            summaryReminderMinutes = values[Keys.summaryMinutes] ?: 20 * 60,
            quietStartMinutes = values[Keys.quietStart] ?: 22 * 60,
            quietEndMinutes = values[Keys.quietEnd] ?: 7 * 60,
            restDaysCsv = values[Keys.restDays] ?: "",
            lastUpdateCheckEpochMillis = values[Keys.lastUpdateCheck] ?: 0L,
            dismissedUpdateVersionName = values[Keys.dismissedUpdateVersion]?.takeIf(String::isNotBlank),
            pendingUpdateVersionName = values[Keys.pendingUpdateVersion]?.takeIf(String::isNotBlank),
            updateTransferActive = values[Keys.updateTransferActive] ?: false,
            updateTransferTagName = values[Keys.updateTransferTagName]?.takeIf(String::isNotBlank),
            updateTransferVersionName = values[Keys.updateTransferVersionName]?.takeIf(String::isNotBlank),
            updateTransferDownloadUrl = values[Keys.updateTransferDownloadUrl]?.takeIf(String::isNotBlank),
            updateTransferSha256Url = values[Keys.updateTransferSha256Url]?.takeIf(String::isNotBlank),
            updateTransferReleaseNotes = values[Keys.updateTransferReleaseNotes] ?: "",
            updateTransferStage = values[Keys.updateTransferStage]?.takeIf(String::isNotBlank),
            updateTransferDownloadedBytes = values[Keys.updateTransferDownloadedBytes] ?: 0L,
            updateTransferTotalBytes = values[Keys.updateTransferTotalBytes]?.takeIf { it > 0L },
            updateTransferStatus = values[Keys.updateTransferStatus]?.takeIf(String::isNotBlank),
            updateTransferError = values[Keys.updateTransferError]?.takeIf(String::isNotBlank),
            focusStartedAtEpochMillis = values[Keys.focusStartedAt]?.takeIf { it > 0L },
            focusEndAtEpochMillis = values[Keys.focusEndAt]?.takeIf { it > 0L },
            focusSessionStartedAtEpochMillis = values[Keys.focusSessionStartedAt]?.takeIf { it > 0L },
            focusPlannedMinutes = values[Keys.focusPlannedMinutes] ?: 25,
            focusRemainingSeconds = values[Keys.focusRemainingSeconds] ?: 25 * 60,
            focusAccumulatedSeconds = values[Keys.focusAccumulatedSeconds] ?: 0,
            focusPaused = values[Keys.focusPaused] ?: false,
            focusPhase = values[Keys.focusPhase] ?: "WORK",
            focusRound = values[Keys.focusRound] ?: 1,
            focusAutoStartBreaks = values[Keys.focusAutoStartBreaks] ?: false,
            focusProjectId = values[Keys.focusProjectId]?.takeIf(String::isNotBlank),
            focusTaskId = values[Keys.focusTaskId]?.takeIf(String::isNotBlank),
            hasCompletedOnboarding = values[Keys.onboardingCompleted] ?: false,
            soundEnabled = values[Keys.soundEnabled] ?: true,
            vibrationEnabled = values[Keys.vibrationEnabled] ?: true,
            focusFeedbackMode = values[Keys.focusFeedbackMode].normalizedFeedbackMode(),
            reminderFeedbackMode = values[Keys.reminderFeedbackMode].normalizedFeedbackMode(),
            countdownFeedbackMode = values[Keys.countdownFeedbackMode].normalizedFeedbackMode(),
            feedbackAudioPath = values[Keys.feedbackAudioPath]?.takeIf(String::isNotBlank),
            feedbackAudioName = values[Keys.feedbackAudioName]?.takeIf(String::isNotBlank),
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.learnListDataStore.edit { values ->
            val current = AppSettings(
                reviewLimit = values[Keys.reviewLimit] ?: 20,
                summaryReminderEnabled = values[Keys.summaryEnabled] ?: true,
                summaryReminderMinutes = values[Keys.summaryMinutes] ?: 20 * 60,
                quietStartMinutes = values[Keys.quietStart] ?: 22 * 60,
                quietEndMinutes = values[Keys.quietEnd] ?: 7 * 60,
                restDaysCsv = values[Keys.restDays] ?: "",
                lastUpdateCheckEpochMillis = values[Keys.lastUpdateCheck] ?: 0L,
                dismissedUpdateVersionName = values[Keys.dismissedUpdateVersion]?.takeIf(String::isNotBlank),
                pendingUpdateVersionName = values[Keys.pendingUpdateVersion]?.takeIf(String::isNotBlank),
                updateTransferActive = values[Keys.updateTransferActive] ?: false,
                updateTransferTagName = values[Keys.updateTransferTagName]?.takeIf(String::isNotBlank),
                updateTransferVersionName = values[Keys.updateTransferVersionName]?.takeIf(String::isNotBlank),
                updateTransferDownloadUrl = values[Keys.updateTransferDownloadUrl]?.takeIf(String::isNotBlank),
                updateTransferSha256Url = values[Keys.updateTransferSha256Url]?.takeIf(String::isNotBlank),
                updateTransferReleaseNotes = values[Keys.updateTransferReleaseNotes] ?: "",
                updateTransferStage = values[Keys.updateTransferStage]?.takeIf(String::isNotBlank),
                updateTransferDownloadedBytes = values[Keys.updateTransferDownloadedBytes] ?: 0L,
                updateTransferTotalBytes = values[Keys.updateTransferTotalBytes]?.takeIf { it > 0L },
                updateTransferStatus = values[Keys.updateTransferStatus]?.takeIf(String::isNotBlank),
                updateTransferError = values[Keys.updateTransferError]?.takeIf(String::isNotBlank),
                focusStartedAtEpochMillis = values[Keys.focusStartedAt]?.takeIf { it > 0L },
                focusEndAtEpochMillis = values[Keys.focusEndAt]?.takeIf { it > 0L },
                focusSessionStartedAtEpochMillis = values[Keys.focusSessionStartedAt]?.takeIf { it > 0L },
                focusPlannedMinutes = values[Keys.focusPlannedMinutes] ?: 25,
                focusRemainingSeconds = values[Keys.focusRemainingSeconds] ?: 25 * 60,
                focusAccumulatedSeconds = values[Keys.focusAccumulatedSeconds] ?: 0,
                focusPaused = values[Keys.focusPaused] ?: false,
                focusPhase = values[Keys.focusPhase] ?: "WORK",
                focusRound = values[Keys.focusRound] ?: 1,
                focusAutoStartBreaks = values[Keys.focusAutoStartBreaks] ?: false,
                focusProjectId = values[Keys.focusProjectId]?.takeIf(String::isNotBlank),
                focusTaskId = values[Keys.focusTaskId]?.takeIf(String::isNotBlank),
                hasCompletedOnboarding = values[Keys.onboardingCompleted] ?: false,
                soundEnabled = values[Keys.soundEnabled] ?: true,
                vibrationEnabled = values[Keys.vibrationEnabled] ?: true,
                focusFeedbackMode = values[Keys.focusFeedbackMode].normalizedFeedbackMode(),
                reminderFeedbackMode = values[Keys.reminderFeedbackMode].normalizedFeedbackMode(),
                countdownFeedbackMode = values[Keys.countdownFeedbackMode].normalizedFeedbackMode(),
                feedbackAudioPath = values[Keys.feedbackAudioPath]?.takeIf(String::isNotBlank),
                feedbackAudioName = values[Keys.feedbackAudioName]?.takeIf(String::isNotBlank),
            )
            val next = transform(current)
            values[Keys.reviewLimit] = next.reviewLimit.coerceIn(1, 1000)
            values[Keys.summaryEnabled] = next.summaryReminderEnabled
            values[Keys.summaryMinutes] = next.summaryReminderMinutes.coerceIn(0, 1439)
            values[Keys.quietStart] = next.quietStartMinutes.coerceIn(0, 1439)
            values[Keys.quietEnd] = next.quietEndMinutes.coerceIn(0, 1439)
            values[Keys.restDays] = next.restDaysCsv
            values[Keys.lastUpdateCheck] = next.lastUpdateCheckEpochMillis.coerceAtLeast(0)
            next.dismissedUpdateVersionName?.takeIf(String::isNotBlank)?.let { values[Keys.dismissedUpdateVersion] = it }
                ?: values.remove(Keys.dismissedUpdateVersion)
            next.pendingUpdateVersionName?.takeIf(String::isNotBlank)?.let { values[Keys.pendingUpdateVersion] = it }
                ?: values.remove(Keys.pendingUpdateVersion)
            values[Keys.updateTransferActive] = next.updateTransferActive
            next.updateTransferTagName?.takeIf(String::isNotBlank)?.let { values[Keys.updateTransferTagName] = it } ?: values.remove(Keys.updateTransferTagName)
            next.updateTransferVersionName?.takeIf(String::isNotBlank)?.let { values[Keys.updateTransferVersionName] = it } ?: values.remove(Keys.updateTransferVersionName)
            next.updateTransferDownloadUrl?.takeIf(String::isNotBlank)?.let { values[Keys.updateTransferDownloadUrl] = it } ?: values.remove(Keys.updateTransferDownloadUrl)
            next.updateTransferSha256Url?.takeIf(String::isNotBlank)?.let { values[Keys.updateTransferSha256Url] = it } ?: values.remove(Keys.updateTransferSha256Url)
            values[Keys.updateTransferReleaseNotes] = next.updateTransferReleaseNotes
            next.updateTransferStage?.takeIf(String::isNotBlank)?.let { values[Keys.updateTransferStage] = it } ?: values.remove(Keys.updateTransferStage)
            values[Keys.updateTransferDownloadedBytes] = next.updateTransferDownloadedBytes.coerceAtLeast(0L)
            next.updateTransferTotalBytes?.takeIf { it > 0L }?.let { values[Keys.updateTransferTotalBytes] = it } ?: values.remove(Keys.updateTransferTotalBytes)
            next.updateTransferStatus?.let { values[Keys.updateTransferStatus] = it } ?: values.remove(Keys.updateTransferStatus)
            next.updateTransferError?.let { values[Keys.updateTransferError] = it } ?: values.remove(Keys.updateTransferError)
            next.focusStartedAtEpochMillis?.let { values[Keys.focusStartedAt] = it } ?: values.remove(Keys.focusStartedAt)
            next.focusEndAtEpochMillis?.let { values[Keys.focusEndAt] = it } ?: values.remove(Keys.focusEndAt)
            next.focusSessionStartedAtEpochMillis?.let { values[Keys.focusSessionStartedAt] = it } ?: values.remove(Keys.focusSessionStartedAt)
            values[Keys.focusPlannedMinutes] = next.focusPlannedMinutes.coerceIn(1, 180)
            values[Keys.focusRemainingSeconds] = next.focusRemainingSeconds.coerceIn(0, 180 * 60)
            values[Keys.focusAccumulatedSeconds] = next.focusAccumulatedSeconds.coerceIn(0, 24 * 60 * 60)
            values[Keys.focusPaused] = next.focusPaused
            values[Keys.focusPhase] = next.focusPhase.takeIf { it in setOf("WORK", "SHORT_BREAK", "LONG_BREAK") } ?: "WORK"
            values[Keys.focusRound] = next.focusRound.coerceIn(1, 4)
            values[Keys.focusAutoStartBreaks] = next.focusAutoStartBreaks
            next.focusProjectId?.takeIf(String::isNotBlank)?.let { values[Keys.focusProjectId] = it } ?: values.remove(Keys.focusProjectId)
            next.focusTaskId?.takeIf(String::isNotBlank)?.let { values[Keys.focusTaskId] = it } ?: values.remove(Keys.focusTaskId)
            values[Keys.onboardingCompleted] = next.hasCompletedOnboarding
            values[Keys.soundEnabled] = next.soundEnabled
            values[Keys.vibrationEnabled] = next.vibrationEnabled
            values[Keys.focusFeedbackMode] = next.focusFeedbackMode.normalizedFeedbackMode()
            values[Keys.reminderFeedbackMode] = next.reminderFeedbackMode.normalizedFeedbackMode()
            values[Keys.countdownFeedbackMode] = next.countdownFeedbackMode.normalizedFeedbackMode()
            next.feedbackAudioPath?.takeIf(String::isNotBlank)?.let { values[Keys.feedbackAudioPath] = it } ?: values.remove(Keys.feedbackAudioPath)
            next.feedbackAudioName?.takeIf(String::isNotBlank)?.let { values[Keys.feedbackAudioName] = it } ?: values.remove(Keys.feedbackAudioName)
        }
    }

    private fun String?.normalizedFeedbackMode(): String = when (this) {
        "GLOBAL", "SOUND", "VIBRATION", "BOTH", "OFF" -> this
        else -> "GLOBAL"
    }

    /** Atomically claims a completed timer so foreground and background paths cannot both notify. */
    suspend fun clearFocusTimerIfMatches(startedAt: Long, endAt: Long): Boolean {
        var claimed = false
        context.learnListDataStore.edit { values ->
            if (values[Keys.focusStartedAt] == startedAt && values[Keys.focusEndAt] == endAt) {
                values.remove(Keys.focusStartedAt)
                values.remove(Keys.focusEndAt)
                values.remove(Keys.focusSessionStartedAt)
                claimed = true
            }
        }
        return claimed
    }
}
