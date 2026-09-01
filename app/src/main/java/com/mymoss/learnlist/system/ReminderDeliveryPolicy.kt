package com.mymoss.learnlist.system

/** Pure delivery rules used before a scheduled alarm is allowed to notify. */
object ReminderDeliveryPolicy {
    fun shouldDeliver(
        kind: String,
        reminderEnabled: Boolean,
        projectActive: Boolean = true,
        hasDueTask: Boolean = true,
        countdownActive: Boolean = true,
    ): Boolean = when (kind) {
        "SUMMARY" -> reminderEnabled
        "PROJECT" -> reminderEnabled && projectActive && hasDueTask
        "COUNTDOWN" -> countdownActive
        else -> false
    }
}
