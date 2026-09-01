package com.mymoss.learnlist.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PomodoroCycleTest {
    @Test
    fun standardCycleUsesShortBreakThenLongBreakAfterFourRounds() {
        val first = PomodoroCycle.initial()
        val shortBreak = PomodoroCycle.afterCompleted(first)
        assertEquals(FocusPhaseType.SHORT_BREAK, shortBreak.type)
        assertEquals(5 * 60, shortBreak.totalSeconds)

        val fourth = PomodoroPhase(FocusPhaseType.WORK, 4, PomodoroCycle.WORK_SECONDS)
        val longBreak = PomodoroCycle.afterCompleted(fourth)
        assertEquals(FocusPhaseType.LONG_BREAK, longBreak.type)
        assertEquals(15 * 60, longBreak.totalSeconds)
    }

    @Test
    fun completingLongBreakStartsRoundOne() {
        val next = PomodoroCycle.afterCompleted(PomodoroPhase(FocusPhaseType.LONG_BREAK, 4, 15 * 60))
        assertEquals(PomodoroCycle.initial(), next)
    }

    @Test
    fun onlyWorkPhasesCountAsFocus() {
        assertTrue(PomodoroCycle.isCountedAsFocus(FocusPhaseType.WORK))
    }
}
