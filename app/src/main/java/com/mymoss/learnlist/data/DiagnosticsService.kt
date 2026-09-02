package com.mymoss.learnlist.data

import android.content.Context
import android.os.Build
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Counts only; never pass entity titles, notes, IDs, paths, or free-form text here. */
internal data class DiagnosticsRecordCount(
    val total: Int,
    val archived: Int = 0,
    val deleted: Int = 0,
)

/**
 * Produces a user-triggered, local-only report for compatibility debugging.
 * The report intentionally contains aggregate metadata rather than a backup.
 */
class DiagnosticsService(
    private val context: Context,
    private val repository: LearnListRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun export(): ByteArray = withContext(Dispatchers.Default) {
        val snapshot = repository.snapshot()
        val settings = settingsRepository.settings.first()
        val records = linkedMapOf(
            "projects" to DiagnosticsRecordCount(
                total = snapshot.projects.size,
                archived = snapshot.projects.count { it.isArchived },
                deleted = snapshot.projects.count { it.deletedAt != null },
            ),
            "learningTasks" to DiagnosticsRecordCount(
                total = snapshot.tasks.size,
                archived = snapshot.tasks.count { it.isArchived },
                deleted = snapshot.tasks.count { it.deletedAt != null },
            ),
            "readingPlans" to DiagnosticsRecordCount(
                total = snapshot.readingPlans.size,
                archived = snapshot.readingPlans.count { it.isArchived },
                deleted = snapshot.readingPlans.count { it.deletedAt != null },
            ),
            "todos" to DiagnosticsRecordCount(
                total = snapshot.todos.size,
                archived = snapshot.todos.count { it.isArchived },
                deleted = snapshot.todos.count { it.deletedAt != null },
            ),
            "goals" to DiagnosticsRecordCount(
                total = snapshot.goals.size,
                archived = snapshot.goals.count { it.isArchived },
                deleted = snapshot.goals.count { it.deletedAt != null },
            ),
            "countdowns" to DiagnosticsRecordCount(
                total = snapshot.countdowns.size,
                archived = snapshot.countdowns.count { it.isArchived },
                deleted = snapshot.countdowns.count { it.deletedAt != null },
            ),
            "reminders" to DiagnosticsRecordCount(total = snapshot.reminders.size),
            "reviewLogs" to DiagnosticsRecordCount(total = snapshot.reviewLogs.size),
            "reviewCorrections" to DiagnosticsRecordCount(total = snapshot.reviewCorrections.size),
            "readingTargets" to DiagnosticsRecordCount(total = snapshot.readingTargets.size),
            "pageLogs" to DiagnosticsRecordCount(total = snapshot.pageLogs.size),
            "readingAdjustments" to DiagnosticsRecordCount(total = snapshot.readingAdjustments.size),
            "focusSessions" to DiagnosticsRecordCount(total = snapshot.focusSessions.size),
        )
        val activityFlags = linkedMapOf(
            "onboardingCompleted" to settings.hasCompletedOnboarding,
            "updateTransferActive" to settings.updateTransferActive,
            "soundEnabled" to settings.soundEnabled,
            "vibrationEnabled" to settings.vibrationEnabled,
            "feedbackAudioConfigured" to (settings.feedbackAudioPath != null || settings.feedbackAudioUri != null),
            "restDaysConfigured" to settings.restDaysCsv.isNotBlank(),
        )
        buildDiagnosticsJson(
            generatedAtEpochMillis = System.currentTimeMillis(),
            versionName = appVersionName(),
            apiLevel = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            records = records,
            activityFlags = activityFlags,
        ).toByteArray(StandardCharsets.UTF_8)
    }

    private fun appVersionName(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { "unknown" }
}

internal fun buildDiagnosticsJson(
    generatedAtEpochMillis: Long,
    versionName: String,
    apiLevel: Int,
    manufacturer: String,
    model: String,
    records: Map<String, DiagnosticsRecordCount>,
    activityFlags: Map<String, Boolean>,
): String {
    val recordFields = records.map { (name, count) ->
        name to jsonObject(
            "total" to count.total.coerceAtLeast(0).toString(),
            "archived" to count.archived.coerceAtLeast(0).toString(),
            "deleted" to count.deleted.coerceAtLeast(0).toString(),
        )
    }
    val flagFields = activityFlags.map { (name, enabled) -> name to enabled.toString() }
    return jsonObject(
        "format" to jsonString("learn-list-diagnostics"),
        "schemaVersion" to "1",
        "generatedAtEpochMillis" to generatedAtEpochMillis.toString(),
        "app" to jsonObject(
            "packageName" to jsonString("com.mymoss.learnlist"),
            "versionName" to jsonString(versionName.safeDiagnosticValue()),
        ),
        "device" to jsonObject(
            "androidApi" to apiLevel.toString(),
            "manufacturer" to jsonString(manufacturer.safeDiagnosticValue()),
            "model" to jsonString(model.safeDiagnosticValue()),
        ),
        "records" to jsonObject(recordFields),
        "activityFlags" to jsonObject(flagFields),
    )
}

private fun jsonObject(vararg fields: Pair<String, String>): String = jsonObject(fields.asList())

private fun jsonObject(fields: Iterable<Pair<String, String>>): String =
    fields.joinToString(prefix = "{", postfix = "}", separator = ",") { (key, value) ->
        "${jsonString(key)}:$value"
    }

private fun jsonString(value: String): String = buildString {
    append('"')
    value.forEach { character ->
        when (character) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> {
                if (character.code < 0x20) {
                    append("\\u")
                    append(character.code.toString(16).padStart(4, '0'))
                } else {
                    append(character)
                }
            }
        }
    }
    append('"')
}

private fun String.safeDiagnosticValue(): String =
    filterNot(Char::isISOControl).take(80).ifBlank { "unknown" }

