package com.mymoss.learnlist.system

import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReminderEntity

/** Keeps alarm eligibility independent from Android's AlarmManager implementation. */
object ReminderSchedulingPolicy {
    fun shouldSchedule(reminder: ReminderEntity, projects: List<ProjectEntity>): Boolean {
        if (!reminder.enabled) return false
        val projectId = reminder.projectId ?: return true
        return projects.any { project ->
            project.id == projectId &&
                !project.isArchived &&
                !project.isPaused &&
                project.deletedAt == null
        }
    }
}

