package com.mymoss.learnlist.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class EpochDateTest {
    @Test
    fun `focus activity date follows the supplied zone around midnight`() {
        val epochMillis = Instant.parse("2026-09-01T23:30:00Z").toEpochMilli()

        assertEquals(LocalDate.of(2026, 9, 1), epochMillis.toLocalDate(ZoneOffset.UTC))
        assertEquals(LocalDate.of(2026, 9, 2), epochMillis.toLocalDate(ZoneOffset.ofHours(8)))
    }
}
