package com.mymoss.learnlist.domain

import java.time.LocalDate
import kotlin.math.ceil

/** The fixed spaced-repetition intervals, expressed in days. */
object ReviewIntervals {
    val days: List<Long> = listOf(1L, 2L, 4L, 7L, 15L, 30L, 60L, 90L)
}

enum class RecallRating {
    REMEMBERED,
    FUZZY,
    FORGOT,
    SNOOZE,
}

data class ReviewState(
    val stage: Int = 0,
    val nextReviewDate: LocalDate? = null,
)

data class ReviewLog(
    val rating: RecallRating,
    val reviewedOn: LocalDate,
    val previousStage: Int,
    val nextStage: Int,
    val nextReviewDate: LocalDate,
)

data class ReviewDecision(
    val state: ReviewState,
    val log: ReviewLog?,
)

interface ReviewScheduler {
    fun completeInitialLearning(completedDate: LocalDate): ReviewState

    fun review(
        state: ReviewState,
        rating: RecallRating,
        completedDate: LocalDate,
        snoozeUntil: LocalDate? = null,
    ): ReviewDecision
}

class DefaultReviewScheduler : ReviewScheduler {
    override fun completeInitialLearning(completedDate: LocalDate): ReviewState =
        ReviewState(stage = 0, nextReviewDate = completedDate.plusDays(ReviewIntervals.days.first()))

    override fun review(
        state: ReviewState,
        rating: RecallRating,
        completedDate: LocalDate,
        snoozeUntil: LocalDate?,
    ): ReviewDecision {
        val previousStage = state.stage.coerceIn(0, ReviewIntervals.days.lastIndex)

        if (rating == RecallRating.SNOOZE) {
            val reminderDate = snoozeUntil ?: completedDate.plusDays(1)
            return ReviewDecision(
                state = state.copy(stage = previousStage, nextReviewDate = reminderDate),
                log = null,
            )
        }

        val nextStage: Int
        val intervalDays: Long
        when (rating) {
            RecallRating.REMEMBERED -> {
                nextStage = (previousStage + 1).coerceAtMost(ReviewIntervals.days.lastIndex)
                intervalDays = ReviewIntervals.days[nextStage]
            }

            RecallRating.FUZZY -> {
                nextStage = previousStage
                intervalDays = ceil(ReviewIntervals.days[previousStage] / 2.0).toLong().coerceAtLeast(1)
            }

            RecallRating.FORGOT -> {
                nextStage = 0
                intervalDays = ReviewIntervals.days.first()
            }

            RecallRating.SNOOZE -> error("Snooze is handled before scheduling a review")
        }

        val nextReviewDate = completedDate.plusDays(intervalDays)
        return ReviewDecision(
            state = ReviewState(stage = nextStage, nextReviewDate = nextReviewDate),
            log = ReviewLog(
                rating = rating,
                reviewedOn = completedDate,
                previousStage = previousStage,
                nextStage = nextStage,
                nextReviewDate = nextReviewDate,
            ),
        )
    }
}
