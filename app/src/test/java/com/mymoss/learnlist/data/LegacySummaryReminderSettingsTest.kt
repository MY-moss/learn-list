package com.mymoss.learnlist.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacySummaryReminderSettingsTest {
    @Test
    fun absentLegacyValuesDoNotCreateReminder() {
        assertNull(
            LegacySummaryReminderSettings.from(
                enabled = null,
                timeMinutes = null,
                quietStartMinutes = null,
                quietEndMinutes = null,
            ),
        )
    }

    @Test
    fun partialLegacyValuesUseSafeDefaultsAndClampTimes() {
        val settings = LegacySummaryReminderSettings.from(
            enabled = false,
            timeMinutes = 2_000,
            quietStartMinutes = -1,
            quietEndMinutes = null,
        )

        requireNotNull(settings)
        assertEquals(false, settings.enabled)
        assertEquals(1_439, settings.timeMinutes)
        assertEquals(0, settings.quietStartMinutes)
        assertEquals(7 * 60, settings.quietEndMinutes)
    }
}
