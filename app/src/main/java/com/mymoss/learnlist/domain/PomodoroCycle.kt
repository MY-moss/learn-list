package com.mymoss.learnlist.domain

enum class FocusPhaseType { WORK, SHORT_BREAK, LONG_BREAK }

data class PomodoroPhase(
    val type: FocusPhaseType,
    val round: Int,
    val totalSeconds: Int,
)

/** Standard four-round Pomodoro sequence. Breaks are suggestions unless auto-start is enabled. */
object PomodoroCycle {
    const val WORK_SECONDS = 25 * 60
    const val SHORT_BREAK_SECONDS = 5 * 60
    const val LONG_BREAK_SECONDS = 15 * 60
    const val ROUNDS_PER_CYCLE = 4

    fun initial(round: Int = 1): PomodoroPhase = PomodoroPhase(FocusPhaseType.WORK, round.coerceIn(1, ROUNDS_PER_CYCLE), WORK_SECONDS)

    fun afterCompleted(phase: PomodoroPhase): PomodoroPhase = when (phase.type) {
        FocusPhaseType.WORK -> if (phase.round >= ROUNDS_PER_CYCLE) {
            PomodoroPhase(FocusPhaseType.LONG_BREAK, ROUNDS_PER_CYCLE, LONG_BREAK_SECONDS)
        } else {
            PomodoroPhase(FocusPhaseType.SHORT_BREAK, phase.round, SHORT_BREAK_SECONDS)
        }
        FocusPhaseType.SHORT_BREAK -> PomodoroPhase(FocusPhaseType.WORK, (phase.round + 1).coerceAtMost(ROUNDS_PER_CYCLE), WORK_SECONDS)
        FocusPhaseType.LONG_BREAK -> initial(1)
    }

    fun skipped(phase: PomodoroPhase): PomodoroPhase = afterCompleted(phase)

    fun isCountedAsFocus(phase: FocusPhaseType): Boolean = phase == FocusPhaseType.WORK
}
