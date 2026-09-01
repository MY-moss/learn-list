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
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val lastUpdateCheckEpochMillis: Long = 0L,
    val focusStartedAtEpochMillis: Long? = null,
    val focusEndAtEpochMillis: Long? = null,
    val focusPlannedMinutes: Int = 25,
    val hasCompletedOnboarding: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val reviewLimit = intPreferencesKey("review_limit")
        val summaryEnabled = booleanPreferencesKey("summary_enabled")
        val summaryMinutes = intPreferencesKey("summary_minutes")
        val quietStart = intPreferencesKey("quiet_start")
        val quietEnd = intPreferencesKey("quiet_end")
        val restDays = stringPreferencesKey("rest_days")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val lastUpdateCheck = longPreferencesKey("last_update_check")
        val focusStartedAt = longPreferencesKey("focus_started_at")
        val focusEndAt = longPreferencesKey("focus_end_at")
        val focusPlannedMinutes = intPreferencesKey("focus_planned_minutes")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<AppSettings> = context.learnListDataStore.data.map { values ->
        AppSettings(
            reviewLimit = values[Keys.reviewLimit] ?: 20,
            summaryReminderEnabled = values[Keys.summaryEnabled] ?: true,
            summaryReminderMinutes = values[Keys.summaryMinutes] ?: 20 * 60,
            quietStartMinutes = values[Keys.quietStart] ?: 22 * 60,
            quietEndMinutes = values[Keys.quietEnd] ?: 7 * 60,
            restDaysCsv = values[Keys.restDays] ?: "",
            soundEnabled = values[Keys.soundEnabled] ?: true,
            vibrationEnabled = values[Keys.vibrationEnabled] ?: true,
            lastUpdateCheckEpochMillis = values[Keys.lastUpdateCheck] ?: 0L,
            focusStartedAtEpochMillis = values[Keys.focusStartedAt]?.takeIf { it > 0L },
            focusEndAtEpochMillis = values[Keys.focusEndAt]?.takeIf { it > 0L },
            focusPlannedMinutes = values[Keys.focusPlannedMinutes] ?: 25,
            hasCompletedOnboarding = values[Keys.onboardingCompleted] ?: false,
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
                soundEnabled = values[Keys.soundEnabled] ?: true,
                vibrationEnabled = values[Keys.vibrationEnabled] ?: true,
                lastUpdateCheckEpochMillis = values[Keys.lastUpdateCheck] ?: 0L,
                focusStartedAtEpochMillis = values[Keys.focusStartedAt]?.takeIf { it > 0L },
                focusEndAtEpochMillis = values[Keys.focusEndAt]?.takeIf { it > 0L },
                focusPlannedMinutes = values[Keys.focusPlannedMinutes] ?: 25,
                hasCompletedOnboarding = values[Keys.onboardingCompleted] ?: false,
            )
            val next = transform(current)
            values[Keys.reviewLimit] = next.reviewLimit.coerceIn(1, 1000)
            values[Keys.summaryEnabled] = next.summaryReminderEnabled
            values[Keys.summaryMinutes] = next.summaryReminderMinutes.coerceIn(0, 1439)
            values[Keys.quietStart] = next.quietStartMinutes.coerceIn(0, 1439)
            values[Keys.quietEnd] = next.quietEndMinutes.coerceIn(0, 1439)
            values[Keys.restDays] = next.restDaysCsv
            values[Keys.soundEnabled] = next.soundEnabled
            values[Keys.vibrationEnabled] = next.vibrationEnabled
            values[Keys.lastUpdateCheck] = next.lastUpdateCheckEpochMillis.coerceAtLeast(0)
            next.focusStartedAtEpochMillis?.let { values[Keys.focusStartedAt] = it } ?: values.remove(Keys.focusStartedAt)
            next.focusEndAtEpochMillis?.let { values[Keys.focusEndAt] = it } ?: values.remove(Keys.focusEndAt)
            values[Keys.focusPlannedMinutes] = next.focusPlannedMinutes.coerceIn(1, 180)
            values[Keys.onboardingCompleted] = next.hasCompletedOnboarding
        }
    }

    /** Atomically claims a completed timer so foreground and background paths cannot both notify. */
    suspend fun clearFocusTimerIfMatches(startedAt: Long, endAt: Long): Boolean {
        var claimed = false
        context.learnListDataStore.edit { values ->
            if (values[Keys.focusStartedAt] == startedAt && values[Keys.focusEndAt] == endAt) {
                values.remove(Keys.focusStartedAt)
                values.remove(Keys.focusEndAt)
                claimed = true
            }
        }
        return claimed
    }
}

