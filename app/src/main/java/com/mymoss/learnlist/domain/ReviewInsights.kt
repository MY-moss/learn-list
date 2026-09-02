package com.mymoss.learnlist.domain

import java.time.LocalDate

/** A review result enriched with the labels needed by the statistics screen. */
data class ReviewInsightRecord(
    val taskId: String,
    val taskTitle: String,
    val projectTitle: String?,
    val rating: RecallRating,
    val reviewedOn: LocalDate,
)

data class ReviewFeedbackSummary(
    val remembered: Int = 0,
    val fuzzy: Int = 0,
    val forgot: Int = 0,
) {
    val total: Int get() = remembered + fuzzy + forgot
    val negative: Int get() = fuzzy + forgot
    val recallPercent: Int
        get() = if (total == 0) 0 else (remembered * 100 / total).coerceIn(0, 100)
}

data class ReviewWeakPoint(
    val taskId: String,
    val taskTitle: String,
    val projectTitle: String?,
    val reviewCount: Int,
    val fuzzyCount: Int,
    val forgotCount: Int,
    val lastReviewedOn: LocalDate,
) {
    val negativeCount: Int get() = fuzzyCount + forgotCount
}

data class ReviewInsightsSummary(
    val from: LocalDate,
    val to: LocalDate,
    val feedback: ReviewFeedbackSummary,
    val reviewedTaskCount: Int,
    val weakPoints: List<ReviewWeakPoint>,
)

/** Builds explainable review feedback without mixing it with reading or focus units. */
object ReviewInsights {
    fun summarize(
        records: Iterable<ReviewInsightRecord>,
        today: LocalDate,
        windowDays: Long = 28,
        weakPointLimit: Int = 5,
    ): ReviewInsightsSummary {
        require(windowDays > 0) { "统计窗口需要大于 0 天" }

        val from = today.minusDays(windowDays - 1)
        val inWindow = records.filter { record ->
            record.rating != RecallRating.SNOOZE &&
                !record.reviewedOn.isBefore(from) &&
                !record.reviewedOn.isAfter(today)
        }
        val feedback = ReviewFeedbackSummary(
            remembered = inWindow.count { it.rating == RecallRating.REMEMBERED },
            fuzzy = inWindow.count { it.rating == RecallRating.FUZZY },
            forgot = inWindow.count { it.rating == RecallRating.FORGOT },
        )
        val weakPoints = inWindow
            .groupBy(ReviewInsightRecord::taskId)
            .values
            .map { taskRecords ->
                val latest = taskRecords.maxBy(ReviewInsightRecord::reviewedOn)
                ReviewWeakPoint(
                    taskId = latest.taskId,
                    taskTitle = latest.taskTitle.ifBlank { "未命名任务" },
                    projectTitle = latest.projectTitle?.takeIf(String::isNotBlank),
                    reviewCount = taskRecords.size,
                    fuzzyCount = taskRecords.count { it.rating == RecallRating.FUZZY },
                    forgotCount = taskRecords.count { it.rating == RecallRating.FORGOT },
                    lastReviewedOn = latest.reviewedOn,
                )
            }
            .filter { it.negativeCount > 0 }
            .sortedWith(
                compareByDescending<ReviewWeakPoint> { it.negativeCount }
                    .thenByDescending(ReviewWeakPoint::forgotCount)
                    .thenByDescending(ReviewWeakPoint::lastReviewedOn)
                    .thenBy(ReviewWeakPoint::taskTitle),
            )
            .take(weakPointLimit.coerceAtLeast(0))

        return ReviewInsightsSummary(
            from = from,
            to = today,
            feedback = feedback,
            reviewedTaskCount = inWindow.map(ReviewInsightRecord::taskId).distinct().count(),
            weakPoints = weakPoints,
        )
    }
}