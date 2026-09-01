package com.mymoss.learnlist.system

import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulingPolicyTest {
    @Test
    fun summaryReminderCanBeScheduledWithoutAProject() {
        assertTrue(ReminderSchedulingPolicy.shouldSchedule(reminder(), emptyList()))
    }

    @Test
    fun disabledReminderIsNotScheduled() {
        assertFalse(ReminderSchedulingPolicy.shouldSchedule(reminder(enabled = false), emptyList()))
    }

    @Test
    fun projectReminderNeedsAnActiveProject() {
        val project = project()
        assertTrue(ReminderSchedulingPolicy.shouldSchedule(reminder(project.id), listOf(project)))
        assertFalse(ReminderSchedulingPolicy.shouldSchedule(reminder(project.id), listOf(project.copy(isPaused = true))))
        assertFalse(ReminderSchedulingPolicy.shouldSchedule(reminder(project.id), listOf(project.copy(isArchived = true))))
        assertFalse(ReminderSchedulingPolicy.shouldSchedule(reminder(project.id), listOf(project.copy(deletedAt = 123L))))
        assertFalse(ReminderSchedulingPolicy.shouldSchedule(reminder(project.id), emptyList()))
    }

    private fun reminder(projectId: String? = null, enabled: Boolean = true) = ReminderEntity(
        id = "reminder-1",
        projectId = projectId,
        kind = if (projectId == null) "SUMMARY" else "PROJECT",
        timeMinutes = 9 * 60,
        repeatDays = "1,2,3,4,5,6,7",
        enabled = enabled,
        quietStartMinutes = 22 * 60,
        quietEndMinutes = 7 * 60,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun project() = ProjectEntity(
        id = "project-1",
        title = "项目",
        type = "课程",
        description = "",
        tagCsv = "",
        colorHex = "#64D8CB",
        isArchived = false,
        isPaused = false,
        createdAt = 1L,
        updatedAt = 1L,
    )
}

