package com.mymoss.learnlist.system

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mymoss.learnlist.LearnListApplication
import com.mymoss.learnlist.MainActivity
import com.mymoss.learnlist.R
import com.mymoss.learnlist.data.BackupSnapshot
import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.TodoEntity
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.TodoRecurrence
import com.mymoss.learnlist.domain.TodoRepeatRule
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val application = context.applicationContext as LearnListApplication
        val kind = intent.getStringExtra(EXTRA_KIND).orEmpty()
        when (intent.action) {
            ACTION_COMPLETE, ACTION_SNOOZE -> {
                intent.getIntExtra(EXTRA_NOTIFICATION_ID, Int.MIN_VALUE)
                    .takeUnless { it == Int.MIN_VALUE }
                    ?.let { NotificationManagerCompat.from(context).cancel(it) }
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        if (taskId != null) {
                            val task = application.repository.snapshot().tasks.firstOrNull { it.id == taskId }
                            if (task != null && !task.hasLearned) {
                                if (intent.action == ACTION_COMPLETE) {
                                    application.repository.completeInitialLearning(taskId)
                                } else {
                                    application.repository.snoozeTask(taskId)
                                }
                            } else {
                                application.repository.reviewTask(
                                    taskId = taskId,
                                    rating = if (intent.action == ACTION_COMPLETE) RecallRating.REMEMBERED else RecallRating.SNOOZE,
                                    snoozeUntil = if (intent.action == ACTION_SNOOZE) LocalDate.now().plusDays(1) else null,
                                )
                            }
                        }
                        ReminderScheduler(context.applicationContext, application.repository).rescheduleAll()
                    }
                    pendingResult.finish()
                }
                return
            }
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                createChannel(context)
                val snapshot = application.repository.snapshot()
                if (BuildPermission.canPost(context)) {
                    postNotification(context, intent, kind, snapshot)
                }
                if (kind != "COUNTDOWN") {
                    val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)
                    snapshot.reminders.firstOrNull { it.id == reminderId && it.enabled }?.let {
                        ReminderScheduler(context, application.repository).scheduleReminder(it)
                    }
                }
            }
            pendingResult.finish()
        }
    }

    private fun postNotification(context: Context, intent: Intent, kind: String, snapshot: BackupSnapshot) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: if (kind == "SUMMARY") "今日学习进度" else "该复习了"
        val text = if (kind == "COUNTDOWN") {
            "$title 即将到达"
        } else {
            val progress = calculateProgress(snapshot, intent.getStringExtra(EXTRA_PROJECT_ID))
            if (progress.total == 0) "今天还没有必做行动" else "今天已完成 ${progress.completed}/${progress.total} 项（${progress.percent}%）"
        }
        val openIntent = PendingIntent.getActivity(
            context, 1, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), pendingFlags(),
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode())
        val taskId = if (kind == "COUNTDOWN") {
            null
        } else {
            intent.getStringExtra(EXTRA_TASK_ID)
                ?: snapshot.tasks.firstOrNull { task ->
                    !task.isArchived &&
                        (intent.getStringExtra(EXTRA_PROJECT_ID) == null || task.projectId == intent.getStringExtra(EXTRA_PROJECT_ID)) &&
                        snapshot.projects.any { project ->
                            project.id == task.projectId && !project.isArchived && !project.isPaused
                        } &&
                        isTaskDue(task, LocalDate.now())
                }?.id
        }
        if (taskId != null) {
            val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)
            builder.addAction(NotificationCompat.Action(R.drawable.ic_notification, "完成", actionIntent(context, ACTION_COMPLETE, taskId, notificationId, reminderId)))
            builder.addAction(NotificationCompat.Action(R.drawable.ic_notification, "稍后", actionIntent(context, ACTION_SNOOZE, taskId, notificationId, reminderId)))
        }
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Notification permission can be revoked between the check and the post call.
        }
    }

    private data class Progress(val completed: Int, val total: Int) {
        val percent: Int get() = if (total == 0) 0 else completed * 100 / total
    }

    private fun calculateProgress(snapshot: BackupSnapshot, projectId: String?): Progress {
        val today = LocalDate.now()
        val activeProjects = snapshot.projects.filter { !it.isArchived && !it.isPaused }
            .filter { projectId == null || it.id == projectId }.map(ProjectEntity::id).toSet()
        val actions = mutableListOf<Boolean>()
        val requiredTasks = snapshot.tasks.filter { it.isRequired && !it.isArchived && it.projectId in activeProjects }
        requiredTasks.filter { task -> isTaskDue(task, today) || isInitialLearningCompleted(task, today) }.forEach { task ->
            val reviewed = snapshot.reviewLogs.any { it.taskId == task.id && it.reviewedOn == today.toString() }
            val initiallyLearned = isInitialLearningCompleted(task, today)
            actions += reviewed || initiallyLearned
        }
        val activePlans = snapshot.readingPlans.filter { plan ->
            val startsOnOrBeforeToday = runCatching { LocalDate.parse(plan.startDate) <= today }.getOrDefault(true)
            !plan.isArchived && !plan.isPaused && plan.projectId in activeProjects &&
                startsOnOrBeforeToday &&
                (plan.currentPage < plan.totalPages || snapshot.pageLogs.any { it.planId == plan.id && it.localDate == today.toString() })
        }
        activePlans.forEach { plan ->
            val target = snapshot.readingTargets.firstOrNull { it.planId == plan.id && it.localDate == today.toString() }?.targetPages ?: plan.dailyTarget
            val pages = snapshot.pageLogs.filter { it.planId == plan.id && it.localDate == today.toString() }.sumOf(PageLogEntity::pagesRead)
            actions += pages >= target
        }
        if (projectId == null) {
            snapshot.todos.filter { it.isRequired && !it.isArchived && isTodoDue(it, today) }.forEach { todo ->
                actions += todo.completedDates.split(',').any { it == today.toString() }
            }
        }
        return Progress(completed = actions.count { it }, total = actions.size)
    }

    private fun isTaskDue(task: com.mymoss.learnlist.data.local.LearningTaskEntity, date: LocalDate): Boolean {
        if (task.snoozedUntil?.let { runCatching { LocalDate.parse(it) }.getOrNull()?.isAfter(date) } == true) return false
        if (!task.hasLearned) return true
        return task.nextReviewDate?.let { runCatching { LocalDate.parse(it) <= date }.getOrDefault(true) } ?: true
    }

    private fun isInitialLearningCompleted(task: com.mymoss.learnlist.data.local.LearningTaskEntity, date: LocalDate): Boolean =
        task.hasLearned && task.updatedAt.toLocalDate() == date && task.nextReviewDate == date.plusDays(1).toString()

    private fun isTodoDue(todo: TodoEntity, date: LocalDate): Boolean {
        val rule = runCatching { TodoRepeatRule.valueOf(todo.repeatRule) }.getOrDefault(TodoRepeatRule.ONCE)
        val base = todo.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val custom = todo.customRepeatDays.split(',').mapNotNull { token ->
            token.trim().toIntOrNull()?.takeIf { it in 1..7 }?.let { runCatching { DayOfWeek.of(it) }.getOrNull() }
        }.toSet()
        val completed = todo.completedDates.split(',').mapNotNull { token ->
            runCatching { LocalDate.parse(token) }.getOrNull()
        }.toSet()
        return TodoRecurrence.isDue(rule, base, date, customDays = custom, completedDates = completed)
    }

    private fun Long.toLocalDate(): LocalDate = java.time.Instant.ofEpochMilli(this).atZone(java.time.ZoneId.systemDefault()).toLocalDate()

    private fun actionIntent(context: Context, action: String, taskId: String, notificationId: Int, reminderId: String?): PendingIntent = PendingIntent.getBroadcast(
        context,
        (action + taskId + notificationId).hashCode(),
        Intent(context, ReminderReceiver::class.java).setAction(action).putExtra(EXTRA_TASK_ID, taskId)
            .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(EXTRA_REMINDER_ID, reminderId),
        pendingFlags(),
    )

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "学习提醒", NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun pendingFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    companion object {
        const val ACTION_COMPLETE = "com.mymoss.learnlist.action.COMPLETE"
        const val ACTION_SNOOZE = "com.mymoss.learnlist.action.SNOOZE"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_COUNTDOWN_ID = "countdown_id"
        const val EXTRA_KIND = "kind"
        const val EXTRA_TITLE = "title"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val CHANNEL_ID = "study_reminders"
    }
}

private object BuildPermission {
    fun canPost(context: Context): Boolean = android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
