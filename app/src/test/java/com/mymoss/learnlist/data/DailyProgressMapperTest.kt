package com.mymoss.learnlist.data

import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyProgressMapperTest {
    @Test
    fun `task creation date follows the supplied zone`() {
        val createdAt = Instant.parse("2026-09-01T23:30:00Z").toEpochMilli()
        val task = LearningTaskEntity(
            id = "task-1",
            projectId = "project-1",
            title = "跨时区任务",
            prompt = "",
            notes = "",
            source = "",
            isRequired = true,
            isArchived = false,
            hasLearned = false,
            initialLearningDate = null,
            stage = 0,
            nextReviewDate = null,
            snoozedUntil = null,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

        fun map(zoneOffset: ZoneOffset): LocalDate? = DailyProgressMapper.from(
            projects = listOf(
                ProjectEntity(
                    id = "project-1",
                    title = "项目",
                    type = "SKILL",
                    description = "",
                    tagCsv = "",
                    colorHex = "#000000",
                    isArchived = false,
                    isPaused = false,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
            ),
            tasks = listOf(task),
            reviewLogs = emptyList(),
            readingPlans = emptyList(),
            readingTargets = emptyList(),
            pageLogs = emptyList(),
            todos = emptyList(),
            zoneId = zoneOffset,
        ).tasks.single().createdOn

        assertEquals(LocalDate.of(2026, 9, 1), map(ZoneOffset.UTC))
        assertEquals(LocalDate.of(2026, 9, 2), map(ZoneOffset.ofHours(8)))
    }
}
