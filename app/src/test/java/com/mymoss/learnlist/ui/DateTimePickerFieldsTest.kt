package com.mymoss.learnlist.ui

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateTimePickerFieldsTest {
    @Test
    fun dateFallbackAcceptsIsoDateAndRejectsAmbiguousText() {
        assertEquals(LocalDate.of(2026, 9, 1), " 2026-09-01 ".toLocalDateOrNull())
        assertNull("2026/09/01".toLocalDateOrNull())
        assertNull("not-a-date".toLocalDateOrNull())
    }

    @Test
    fun timeFallbackAccepts24HourIsoTime() {
        assertEquals(LocalTime.of(7, 5), "07:05".toLocalTimeOrNull())
        assertNull("25:00".toLocalTimeOrNull())
        assertNull("7:05".toLocalTimeOrNull())
    }

    @Test
    fun pickerSelectionIsDisplayedAsZeroPaddedTime() {
        assertEquals("07:05", formatTime(7, 5))
        assertEquals("23:40", formatTime(23, 40))
    }

    @Test
    fun datePickerUsesStableUtcMidnight() {
        assertEquals(
            1788220800000L,
            LocalDate.of(2026, 9, 1).toPickerMillis(),
        )
    }
}
