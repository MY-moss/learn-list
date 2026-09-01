package com.mymoss.learnlist.system

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.local.CountdownEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.Clock
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context,
    private val repository: LearnListRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun rescheduleAll() {
        val snapshot = repository.snapshot()
        snapshot.reminders.forEach { cancelReminder(it.id) }
        snapshot.countdowns.forEach { cancelCountdown(it.id) }
        snapshot.reminders
            .filter(ReminderEntity::enabled)
            .filter { reminder -> reminder.projectId == null || snapshot.projects.any { project -> project.id == reminder.projectId && !project.isArchived && !project.isPaused && project.deletedAt == null } }
            .forEach(::scheduleReminder)
        snapshot.countdowns.filter { !it.isCompleted && it.deletedAt == null }.forEach(::scheduleCountdown)
    }

    fun cancelReminder(id: String) {
        cancelPending(reminderRequestCode(id))
        cancelPending(stableCode(id)) // Legacy request code used before namespacing.
    }

    fun cancelCountdown(id: String) {
        cancelPending(countdownRequestCode(id))
        cancelPending(stableCode(id)) // Legacy request code used before namespacing.
    }

    fun scheduleReminder(reminder: ReminderEntity) {
        val trigger = nextReminderTime(reminder) ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_KIND, reminder.kind)
            putExtra(ReminderReceiver.EXTRA_PROJECT_ID, reminder.projectId)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, stableCode("notification:${reminder.id}"))
        }
        schedule(trigger, PendingIntent.getBroadcast(context, reminderRequestCode(reminder.id), intent, pendingFlags()))
    }

    fun scheduleCountdown(countdown: CountdownEntity) {
        if (countdown.deletedAt != null || countdown.isCompleted) return
        val reminderMinutes = countdown.reminderMinutesBefore ?: 0
        val trigger = countdown.eventAtEpochMillis - reminderMinutes * 60_000L
        if (trigger <= clock.millis()) return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_KIND, "COUNTDOWN")
            putExtra(ReminderReceiver.EXTRA_COUNTDOWN_ID, countdown.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, countdown.title)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, stableCode("notification:${countdown.id}"))
        }
        schedule(trigger, PendingIntent.getBroadcast(context, countdownRequestCode(countdown.id), intent, pendingFlags()))
    }

    private fun schedule(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun nextReminderTime(reminder: ReminderEntity): Long? {
        val allowedDays = reminder.repeatDays.split(',').mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }.toSet()
        if (allowedDays.isEmpty()) return null
        val now = java.time.ZonedDateTime.now(clock)
        for (offset in 0..8) {
            val date = LocalDate.now(clock).plusDays(offset.toLong())
            if (date.dayOfWeek.value !in allowedDays) continue
            val hour = reminder.timeMinutes / 60
            val minute = reminder.timeMinutes % 60
            val candidate = date.atTime(hour, minute).atZone(clock.zone)
            if (!candidate.isAfter(now)) continue
            if (isQuiet(reminder, reminder.timeMinutes)) continue
            return candidate.toInstant().toEpochMilli()
        }
        return null
    }

    private fun isQuiet(reminder: ReminderEntity, timeMinutes: Int): Boolean {
        val start = reminder.quietStartMinutes ?: return false
        val end = reminder.quietEndMinutes ?: return false
        return if (start <= end) timeMinutes in start..end else timeMinutes >= start || timeMinutes <= end
    }

    private fun stableCode(id: String): Int = id.hashCode() and Int.MAX_VALUE
    private fun reminderRequestCode(id: String): Int = stableCode("reminder:$id")
    private fun countdownRequestCode(id: String): Int = stableCode("countdown:$id")
    private fun pendingFlags(): Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    private fun cancelPending(requestCode: Int) { alarmManager.cancel(pendingIntent(requestCode)) }

    private fun pendingIntent(requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
    ) ?: PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, ReminderReceiver::class.java),
        pendingFlags(),
    )
}
