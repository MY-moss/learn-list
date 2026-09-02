package com.mymoss.learnlist.domain

import java.time.LocalDate

/** The small projection needed to order today's spaced-repetition queue. */
data class ReviewQueueItem(
    val id: String,
    val projectId: String,
    val hasLearned: Boolean,
    val nextReviewDate: LocalDate?,
    val snoozedUntil: LocalDate?,
)

/**
 * Keeps queue policy out of the Compose screen so overdue priority and batch
 * selection have one testable definition.
 */
object ReviewQueue {
    fun order(
        items: Iterable<ReviewQueueItem>,
        today: LocalDate,
        activeProjectIds: Set<String>,
    ): List<ReviewQueueItem> = items
        .filter { item ->
            item.projectId in activeProjectIds && isDue(item, today)
        }
        .sortedWith(
            compareByDescending<ReviewQueueItem> { item ->
                item.nextReviewDate?.isBefore(today) == true
            }.thenByDescending(ReviewQueueItem::hasLearned)
                .thenBy { item -> item.nextReviewDate ?: today },
        )

    /** Returns a suggested batch without hiding the remaining due items. */
    fun recommendedBatch(
        items: Iterable<ReviewQueueItem>,
        today: LocalDate,
        activeProjectIds: Set<String>,
        batchSize: Int,
    ): List<ReviewQueueItem> = order(items, today, activeProjectIds)
        .filter(ReviewQueueItem::hasLearned)
        .take(batchSize.coerceAtLeast(1))

    private fun isDue(item: ReviewQueueItem, today: LocalDate): Boolean {
        if (item.snoozedUntil?.isAfter(today) == true) return false
        if (!item.hasLearned) return true
        return item.nextReviewDate == null || !item.nextReviewDate.isAfter(today)
    }
}
