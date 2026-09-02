package com.mymoss.learnlist.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewQueueTest {
    private val today = LocalDate.of(2026, 9, 2)
    private val activeProjects = setOf("active")

    @Test
    fun `overdue learned items come before today's and new items`() {
        val items = listOf(
            ReviewQueueItem("new", "active", false, null, null),
            ReviewQueueItem("today", "active", true, today, null),
            ReviewQueueItem("overdue", "active", true, today.minusDays(3), null),
            ReviewQueueItem("tomorrow", "active", true, today.plusDays(1), null),
        )

        assertEquals(listOf("overdue", "today", "new"), ReviewQueue.order(items, today, activeProjects).map(ReviewQueueItem::id))
    }

    @Test
    fun `future snooze is not due but same-day snooze is due`() {
        val items = listOf(
            ReviewQueueItem("later", "active", true, today.minusDays(2), today.plusDays(1)),
            ReviewQueueItem("today", "active", true, today.minusDays(2), today),
        )

        assertEquals(listOf("today"), ReviewQueue.order(items, today, activeProjects).map(ReviewQueueItem::id))
    }

    @Test
    fun `recommended batch only includes learned items and keeps the queue visible`() {
        val items = (1..4).map { index ->
            ReviewQueueItem("task-$index", "active", index != 4, today.minusDays(index.toLong()), null)
        }

        val batch = ReviewQueue.recommendedBatch(items, today, activeProjects, batchSize = 2)

        assertEquals(listOf("task-3", "task-2"), batch.map(ReviewQueueItem::id))
        assertEquals(4, ReviewQueue.order(items, today, activeProjects).size)
    }

    @Test
    fun `items from paused or archived projects are excluded by active project set`() {
        val items = listOf(
            ReviewQueueItem("active", "active", true, today, null),
            ReviewQueueItem("hidden", "paused", true, today.minusDays(1), null),
        )

        assertEquals(listOf("active"), ReviewQueue.order(items, today, activeProjects).map(ReviewQueueItem::id))
    }
}

