package com.mymoss.learnlist.system

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimerServiceTest {
    @Test
    fun formatRemaining_keepsMinutesAndSecondsReadable() {
        assertEquals("25:00", FocusTimerService.formatRemaining(25 * 60))
        assertEquals("01:05", FocusTimerService.formatRemaining(65))
        assertEquals("00:00", FocusTimerService.formatRemaining(-1))
    }
}
