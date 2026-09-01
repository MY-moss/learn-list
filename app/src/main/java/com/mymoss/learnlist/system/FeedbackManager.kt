package com.mymoss.learnlist.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.mymoss.learnlist.data.AppSettings

/** Plays the user's local completion feedback without requiring notification permission. */
object FeedbackManager {
    enum class FeedbackContext { FOCUS, REMINDER, COUNTDOWN }

    fun play(context: Context, settings: AppSettings, feedbackContext: FeedbackContext = FeedbackContext.FOCUS) {
        val mode = when (feedbackContext) {
            FeedbackContext.FOCUS -> settings.focusFeedbackMode
            FeedbackContext.REMINDER -> settings.reminderFeedbackMode
            FeedbackContext.COUNTDOWN -> settings.countdownFeedbackMode
        }
        val sound = when (mode) {
            "SOUND", "BOTH" -> true
            "VIBRATION", "OFF" -> false
            else -> settings.soundEnabled
        }
        val vibration = when (mode) {
            "VIBRATION", "BOTH" -> true
            "SOUND", "OFF" -> false
            else -> settings.vibrationEnabled
        }
        if (sound) FeedbackAudioManager.play(context, settings)
        if (vibration) vibrate(context)
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        } ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 180L, 90L, 180L), -1))
    }
}
