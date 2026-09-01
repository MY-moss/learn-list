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
import com.mymoss.learnlist.data.DailyProgressMapper
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.domain.RecallRating
import com.mymoss.learnlist.domain.DailyProgressCalculator
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
                            val snapshot = application.repository.snapshot()
                            val task = snapshot.tasks.firstOrNull { it.id == taskId }
                            val taskProjectActive = task?.let { taskItem ->
                                snapshot.projects.any { project ->
                                    project.id == taskItem.projectId && !project.isArchived && !project.isPaused && project.deletedAt == null
                                }
                            } == true
                            if (task == null || task.deletedAt != null || task.isArchived || !taskProjectActive) {
                                // A notification can outlive an archive/delete operation; stale actions are no-ops.
                            } else if (!task.hasLearned) {
                                if (intent.action == ACTION_COMPLETE) {
                                    application.repository.completeInitialLearning(taskId)
                                } else {
                                    application.repository.snoozeTask(taskId)
                                }
                            } else {
                                application.repository.reviewTask(
                                    taskId = taskId,
                                    rating = if (intent.action == ACTION_COMPLETE) RecallRating.REMEMBERED else RecallRating.SNOOZE,
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
                ensureNotificationChannel(context)
                val snapshot = application.repository.snapshot()
                val settings = SettingsRepository(context.applicationContext).settings.first()
                val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)
                val reminder = snapshot.reminders.firstOrNull { it.id == reminderId }
                val project = intent.getStringExtra(EXTRA_PROJECT_ID)?.let { projectId ->
                    snapshot.projects.firstOrNull { it.id == projectId }
                }
                val dueTask = if (kind == "PROJECT") {
                    snapshot.tasks.firstOrNull { task ->
                        !task.isArchived && task.deletedAt == null &&
                            (project?.id == null || task.projectId == project.id) &&
                            snapshot.projects.any { item ->
                                item.id == task.projectId && !item.isArchived && !item.isPaused && item.deletedAt == null
                            } &&
                            isTaskDue(task, LocalDate.now())
                    }
                } else {
                    null
                }
                val countdownActive = if (kind == "COUNTDOWN") {
                    val countdownId = intent.getStringExtra(EXTRA_COUNTDOWN_ID)
                    snapshot.countdowns.any { it.id == countdownId && !it.isCompleted && !it.isArchived }
                } else {
                    true
                }
                val canDeliver = ReminderDeliveryPolicy.shouldDeliver(
                    kind = kind,
                    reminderEnabled = if (kind == "COUNTDOWN") true else reminder?.enabled == true,
                    projectActive = project != null && !project.isArchived && !project.isPaused && project.deletedAt == null,
                    hasDueTask = dueTask != null,
                    countdownActive = countdownActive,
                )
                if (canDeliver) {
                    FeedbackManager.play(
                        context.applicationContext,
                        settings,
                        if (kind == "COUNTDOWN") FeedbackManager.FeedbackContext.COUNTDOWN else FeedbackManager.FeedbackContext.REMINDER,
                    )
                    if (BuildPermission.canPost(context)) {
                        postNotification(context, intent, kind, snapshot, dueTask?.id)
                    }
                }
                reminder?.takeIf { it.enabled && (it.kind != "PROJECT" || (project != null && !project.isArchived && !project.isPaused && project.deletedAt == null)) }?.let {
                    ReminderScheduler(context, application.repository).scheduleReminder(it)
                }
            }
            pendingResult.finish()
        }
    }

    private fun postNotification(
        context: Context,
        intent: Intent,
        kind: String,
        snapshot: BackupSnapshot,
        resolvedTaskId: String? = null,
    ) {
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
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode())
        val taskId = if (kind != "PROJECT") {
            null
        } else {
            resolvedTaskId ?: intent.getStringExtra(EXTRA_TASK_ID)
                ?: snapshot.tasks.firstOrNull { task ->
                    !task.isArchived && task.deletedAt == null &&
                        (intent.getStringExtra(EXTRA_PROJECT_ID) == null || task.projectId == intent.getStringExtra(EXTRA_PROJECT_ID)) &&
                        snapshot.projects.any { project ->
                            project.id == task.projectId && !project.isArchived && !project.isPaused && project.deletedAt == null
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
        val summary = DailyProgressCalculator().calculate(
            input = DailyProgressMapper.from(snapshot),
            date = LocalDate.now(),
            projectId = projectId,
        )
        return Progress(completed = summary.completedRequired, total = summary.totalRequired)
    }

    private fun isTaskDue(task: com.mymoss.learnlist.data.local.LearningTaskEntity, date: LocalDate): Boolean {
        if (task.snoozedUntil?.let { runCatching { LocalDate.parse(it) }.getOrNull()?.isAfter(date) } == true) return false
        if (!task.hasLearned) return true
        return task.nextReviewDate?.let { runCatching { LocalDate.parse(it) <= date }.getOrDefault(true) } ?: true
    }

    private fun actionIntent(context: Context, action: String, taskId: String, notificationId: Int, reminderId: String?): PendingIntent = PendingIntent.getBroadcast(
        context,
        (action + taskId + notificationId).hashCode(),
        Intent(context, ReminderReceiver::class.java).setAction(action).putExtra(EXTRA_TASK_ID, taskId)
            .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(EXTRA_REMINDER_ID, reminderId),
        pendingFlags(),
    )

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
        const val CHANNEL_ID = "study_reminders_feedback"

        fun ensureNotificationChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "学习提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setSound(null, null)
                    enableVibration(false)
                },
            )
        }
    }
}

private object BuildPermission {
    fun canPost(context: Context): Boolean = android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
