package com.mymoss.learnlist.system

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderDeliveryPolicyTest {
    @Test
    fun disabledSummaryReminderDoesNotDeliver() {
        assertFalse(ReminderDeliveryPolicy.shouldDeliver("SUMMARY", reminderEnabled = false))
    }

    @Test
    fun archivedOrPausedProjectDoesNotDeliver() {
        assertFalse(
            ReminderDeliveryPolicy.shouldDeliver(
                kind = "PROJECT",
                reminderEnabled = true,
                projectActive = false,
                hasDueTask = true,
            ),
        )
    }

    @Test
    fun projectReminderWithoutDueTaskDoesNotDeliver() {
        assertFalse(
            ReminderDeliveryPolicy.shouldDeliver(
                kind = "PROJECT",
                reminderEnabled = true,
                projectActive = true,
                hasDueTask = false,
            ),
        )
    }

    @Test
    fun activeProjectReminderWithDueTaskDelivers() {
        assertTrue(
            ReminderDeliveryPolicy.shouldDeliver(
                kind = "PROJECT",
                reminderEnabled = true,
                projectActive = true,
                hasDueTask = true,
            ),
        )
    }

    @Test
    fun completedCountdownDoesNotDeliver() {
        assertFalse(ReminderDeliveryPolicy.shouldDeliver("COUNTDOWN", reminderEnabled = true, countdownActive = false))
    }
}
