package com.mymoss.learnlist.data.backup

import android.util.Base64
import com.mymoss.learnlist.data.BackupSnapshot
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.AppSettings
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.data.local.CountdownEntity
import com.mymoss.learnlist.data.local.FocusSessionEntity
import com.mymoss.learnlist.data.local.GoalEntity
import com.mymoss.learnlist.data.local.LearningTaskEntity
import com.mymoss.learnlist.data.local.PageLogEntity
import com.mymoss.learnlist.data.local.ProjectEntity
import com.mymoss.learnlist.data.local.ReadingPlanEntity
import com.mymoss.learnlist.data.local.ReadingTargetEntity
import com.mymoss.learnlist.data.local.ReminderEntity
import com.mymoss.learnlist.data.local.ReviewLogEntity
import com.mymoss.learnlist.data.local.TodoEntity
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDate
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class BackupImportMode { MERGE, REPLACE }

data class BackupPreview(
    val encrypted: Boolean,
    val schemaVersion: Int?,
    val createdAt: Long?,
    val counts: Map<String, Int>,
)

data class PendingBackupImport(
    val bytes: ByteArray,
    val preview: BackupPreview,
    val password: String,
    val initialMode: BackupImportMode = BackupImportMode.MERGE,
)

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** JSON backup with optional AES-GCM encryption and PBKDF2 key derivation. */
class BackupService(
    private val repository: LearnListRepository,
    private val settingsRepository: SettingsRepository? = null,
) {
    suspend fun export(encrypted: Boolean, password: String = ""): ByteArray = withContext(Dispatchers.Default) {
        if (encrypted && password.length < MIN_PASSWORD_LENGTH) {
            throw BackupException("加密备份密码至少需要 8 位")
        }
        val settings = settingsRepository?.settings?.first()
        val plain = snapshotToJson(repository.snapshot(), settings).toString().toByteArray(StandardCharsets.UTF_8)
        if (!encrypted) return@withContext plain

        val salt = ByteArray(SALT_SIZE)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plain)
        JSONObject()
            .put("format", ENCRYPTED_FORMAT)
            .put("salt", encode(salt))
            .put("iv", encode(iv))
            .put("ciphertext", encode(ciphertext))
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
    }

    fun preview(bytes: ByteArray): BackupPreview {
        if (bytes.size > MAX_BACKUP_BYTES) throw BackupException("备份文件过大")
        val root = parseRoot(bytes)
        if (root.optString("format") == ENCRYPTED_FORMAT) {
            return BackupPreview(encrypted = true, schemaVersion = null, createdAt = null, counts = emptyMap())
        }
        return snapshotFromJson(root).preview
    }

    suspend fun preview(bytes: ByteArray, password: String): BackupPreview = withContext(Dispatchers.Default) {
        if (bytes.size > MAX_BACKUP_BYTES) throw BackupException("备份文件过大")
        val root = parseRoot(bytes)
        val plainRoot = if (root.optString("format") == ENCRYPTED_FORMAT) {
            if (password.length < MIN_PASSWORD_LENGTH) throw BackupException("请输入正确的备份密码")
            decryptRoot(root, password)
        } else root
        return@withContext snapshotFromJson(plainRoot).preview.copy(encrypted = root.optString("format") == ENCRYPTED_FORMAT)
    }

    suspend fun import(
        bytes: ByteArray,
        password: String = "",
        mode: BackupImportMode,
    ): BackupPreview = withContext(Dispatchers.Default) {
        if (bytes.size > MAX_BACKUP_BYTES) throw BackupException("备份文件过大")
        val root = parseRoot(bytes)
        val plainRoot = if (root.optString("format") == ENCRYPTED_FORMAT) {
            if (password.length < MIN_PASSWORD_LENGTH) throw BackupException("请输入正确的备份密码")
            decryptRoot(root, password)
        } else {
            root
        }
        val parsed = snapshotFromJson(plainRoot)
        when (mode) {
            BackupImportMode.MERGE -> repository.merge(parsed.snapshot)
            BackupImportMode.REPLACE -> repository.replaceAll(parsed.snapshot)
        }
        parsed.settings?.let { imported ->
            settingsRepository?.update { current ->
                current.copy(
                    reviewLimit = imported.reviewLimit,
                    summaryReminderEnabled = imported.summaryReminderEnabled,
                    summaryReminderMinutes = imported.summaryReminderMinutes,
                    quietStartMinutes = imported.quietStartMinutes,
                    quietEndMinutes = imported.quietEndMinutes,
                    restDaysCsv = imported.restDaysCsv,
                )
            }
        }
        parsed.preview.copy(encrypted = root.optString("format") == ENCRYPTED_FORMAT)
    }

    private fun parseRoot(bytes: ByteArray): JSONObject = try {
        JSONObject(String(bytes, StandardCharsets.UTF_8))
    } catch (error: Exception) {
        throw BackupException("备份文件格式无效", error)
    }

    private fun decryptRoot(root: JSONObject, password: String): JSONObject = try {
        val salt = decode(root.getString("salt"))
        val iv = decode(root.getString("iv"))
        val ciphertext = decode(root.getString("ciphertext"))
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), GCMParameterSpec(TAG_BITS, iv))
        JSONObject(String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8))
    } catch (error: Exception) {
        throw BackupException("密码错误或备份已损坏", error)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun snapshotToJson(snapshot: BackupSnapshot, settings: AppSettings?): JSONObject = JSONObject()
        .put("format", PLAIN_FORMAT)
        .put("schemaVersion", SCHEMA_VERSION)
        .put("createdAt", System.currentTimeMillis())
        .putNullable("settings", settings?.let(::settingsJson))
        .put("projects", JSONArray(snapshot.projects.map(::projectJson)))
        .put("tasks", JSONArray(snapshot.tasks.map(::taskJson)))
        .put("reviewLogs", JSONArray(snapshot.reviewLogs.map(::reviewLogJson)))
        .put("readingPlans", JSONArray(snapshot.readingPlans.map(::readingPlanJson)))
        .put("readingTargets", JSONArray(snapshot.readingTargets.map(::readingTargetJson)))
        .put("pageLogs", JSONArray(snapshot.pageLogs.map(::pageLogJson)))
        .put("todos", JSONArray(snapshot.todos.map(::todoJson)))
        .put("focusSessions", JSONArray(snapshot.focusSessions.map(::focusJson)))
        .put("goals", JSONArray(snapshot.goals.map(::goalJson)))
        .put("countdowns", JSONArray(snapshot.countdowns.map(::countdownJson)))
        .put("reminders", JSONArray(snapshot.reminders.map(::reminderJson)))

    private fun previewFromPlain(root: JSONObject): BackupPreview {
        if (root.optString("format") != PLAIN_FORMAT) throw BackupException("备份文件格式无效")
        val schema = root.optInt("schemaVersion", -1)
        if (schema != SCHEMA_VERSION) throw BackupException("不支持的备份版本：$schema")
        val names = listOf("projects", "tasks", "reviewLogs", "readingPlans", "readingTargets", "pageLogs", "todos", "focusSessions", "goals", "countdowns", "reminders", "settings")
        return BackupPreview(
            encrypted = false,
            schemaVersion = schema,
            createdAt = root.optLong("createdAt").takeIf { it > 0 },
            counts = names.associateWith { name ->
                if (name == "settings") {
                    if (root.optJSONObject(name) != null) 1 else 0
                } else {
                    root.optJSONArray(name)?.length() ?: 0
                }
            },
        )
    }

    private fun snapshotFromJson(root: JSONObject): ParsedBackup {
        val preview = previewFromPlain(root)
        fun array(name: String) = root.optJSONArray(name) ?: JSONArray()
        val snapshot = BackupSnapshot(
                projects = array("projects").objects(::parseProject),
                tasks = array("tasks").objects(::parseTask),
                reviewLogs = array("reviewLogs").objects(::parseReviewLog),
                readingPlans = array("readingPlans").objects(::parseReadingPlan),
                readingTargets = array("readingTargets").objects(::parseReadingTarget),
                pageLogs = array("pageLogs").objects(::parsePageLog),
                todos = array("todos").objects(::parseTodo),
                focusSessions = array("focusSessions").objects(::parseFocus),
                goals = array("goals").objects(::parseGoal),
                countdowns = array("countdowns").objects(::parseCountdown),
                reminders = array("reminders").objects(::parseReminder),
        )
        validateSnapshot(snapshot)
        return ParsedBackup(
            snapshot = snapshot,
            settings = parseSettings(root.optJSONObject("settings")),
            preview = preview,
        )
    }

    private fun validateSnapshot(snapshot: BackupSnapshot) {
        val projectIds = snapshot.projects.map(ProjectEntity::id).toSet().also { ids ->
            if (ids.size != snapshot.projects.size) invalid("项目 ID 重复")
        }
        val taskIds = snapshot.tasks.map(LearningTaskEntity::id).toSet().also { ids ->
            if (ids.size != snapshot.tasks.size) invalid("学习任务 ID 重复")
        }
        val planIds = snapshot.readingPlans.map(ReadingPlanEntity::id).toSet().also { ids ->
            if (ids.size != snapshot.readingPlans.size) invalid("阅读计划 ID 重复")
        }

        snapshot.tasks.forEach { task ->
            if (task.projectId !in projectIds) invalid("学习任务引用不存在的项目")
            if (task.stage !in 0..7) invalid("学习任务复习阶段无效")
            task.nextReviewDate?.let { validateDate(it, "nextReviewDate") }
            task.snoozedUntil?.let { validateDate(it, "snoozedUntil") }
        }
        snapshot.reviewLogs.forEach { log ->
            if (log.taskId !in taskIds) invalid("复习记录引用不存在的学习任务")
            if (log.rating !in setOf("REMEMBERED", "FUZZY", "FORGOT")) invalid("复习反馈无效")
            if (log.previousStage !in 0..7 || log.nextStage !in 0..7) invalid("复习记录阶段无效")
            validateDate(log.reviewedOn, "reviewedOn")
            validateDate(log.nextReviewDate, "nextReviewDate")
        }
        snapshot.readingPlans.forEach { plan ->
            if (plan.projectId !in projectIds) invalid("阅读计划引用不存在的项目")
            if (plan.totalPages <= 0 || plan.dailyTarget <= 0 || plan.currentPage !in 0..plan.totalPages) invalid("阅读计划页数无效")
            validateDate(plan.startDate, "startDate")
            plan.deadline?.let {
                validateDate(it, "deadline")
                if (LocalDate.parse(it).isBefore(LocalDate.parse(plan.startDate))) invalid("阅读截止日早于开始日")
            }
        }
        snapshot.readingTargets.forEach { target ->
            if (target.planId !in planIds || target.targetPages < 0) invalid("阅读日目标无效")
            validateDate(target.localDate, "localDate")
        }
        snapshot.pageLogs.forEach { log ->
            if (log.planId !in planIds || log.pagesRead <= 0) invalid("阅读日志无效")
            validateDate(log.localDate, "localDate")
        }
        snapshot.todos.forEach { todo ->
            if (todo.repeatRule !in setOf("ONCE", "DAILY", "WEEKLY", "WORKDAYS", "CUSTOM")) invalid("待办重复规则无效")
            if (todo.repeatRule == "ONCE" && todo.dueDate == null) invalid("一次性待办缺少日期")
            if (todo.repeatRule == "CUSTOM" && todo.customRepeatDays.split(',').none { token -> token.trim().toIntOrNull()?.let { it in 1..7 } == true }) invalid("自定义待办没有有效星期")
            todo.dueDate?.let { validateDate(it, "dueDate") }
        }
        snapshot.focusSessions.forEach { session ->
            if (session.projectId != null && session.projectId !in projectIds) invalid("专注记录引用不存在的项目")
            if (session.taskId != null && session.taskId !in taskIds) invalid("专注记录引用不存在的学习任务")
            if (session.plannedMinutes <= 0 || session.actualMinutes < 0) invalid("专注记录时长无效")
        }
        snapshot.goals.forEach { goal ->
            if (goal.metric !in setOf("FOCUS_MINUTES", "READING_PAGES", "REVIEW_TASKS", "TODO_DONE")) invalid("目标指标无效")
            if (goal.period !in setOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM")) invalid("目标周期无效")
            if (goal.targetValue <= 0) invalid("目标值无效")
            validateDate(goal.startDate, "startDate")
            goal.endDate?.let {
                validateDate(it, "endDate")
                if (LocalDate.parse(it).isBefore(LocalDate.parse(goal.startDate))) invalid("目标截止日早于开始日")
            }
            if (goal.projectId != null && goal.projectId !in projectIds) invalid("目标引用不存在的项目")
        }
        snapshot.countdowns.forEach { countdown ->
            if (countdown.eventAtEpochMillis <= 0) invalid("倒计时事件时间无效")
            if (countdown.reminderMinutesBefore != null && countdown.reminderMinutesBefore !in 0..43_200) invalid("倒计时提前提醒时间无效")
        }
        snapshot.reminders.forEach { reminder ->
            if (reminder.kind !in setOf("SUMMARY", "PROJECT")) invalid("提醒类型无效")
            if (reminder.kind == "PROJECT" && reminder.projectId == null) invalid("项目提醒缺少项目")
            if (reminder.kind == "SUMMARY" && reminder.projectId != null) invalid("每日进度提醒不能绑定项目")
            if (reminder.projectId != null && reminder.projectId !in projectIds) invalid("提醒引用不存在的项目")
            if (reminder.timeMinutes !in 0..1439) invalid("提醒时间无效")
            if (reminder.repeatDays.split(',').none { token -> token.trim().toIntOrNull()?.let { it in 1..7 } == true }) invalid("提醒没有有效星期")
            if (reminder.quietStartMinutes != null && reminder.quietStartMinutes !in 0..1439) invalid("安静开始时间无效")
            if (reminder.quietEndMinutes != null && reminder.quietEndMinutes !in 0..1439) invalid("安静结束时间无效")
        }
    }

    private fun validateDate(value: String, field: String) {
        if (runCatching { LocalDate.parse(value) }.isFailure) invalid("$field 日期无效")
    }

    private fun invalid(message: String): Nothing = throw BackupException("备份数据无效：$message")

    private data class ParsedBackup(
        val snapshot: BackupSnapshot,
        val settings: AppSettings?,
        val preview: BackupPreview,
    )

    private fun projectJson(item: ProjectEntity) = JSONObject().apply {
        put("id", item.id); put("title", item.title); put("type", item.type)
        put("description", item.description); put("tagCsv", item.tagCsv); put("colorHex", item.colorHex)
        put("isArchived", item.isArchived); put("isPaused", item.isPaused)
        put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun taskJson(item: LearningTaskEntity) = JSONObject().apply {
        put("id", item.id); put("projectId", item.projectId); put("title", item.title)
        put("prompt", item.prompt); put("notes", item.notes); put("source", item.source)
        put("isRequired", item.isRequired); put("isArchived", item.isArchived); put("hasLearned", item.hasLearned)
        put("stage", item.stage); putNullable("nextReviewDate", item.nextReviewDate); putNullable("snoozedUntil", item.snoozedUntil)
        put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun reviewLogJson(item: ReviewLogEntity) = JSONObject().apply {
        put("id", item.id); put("taskId", item.taskId); put("rating", item.rating); put("reviewedOn", item.reviewedOn)
        put("previousStage", item.previousStage); put("nextStage", item.nextStage); put("nextReviewDate", item.nextReviewDate); put("createdAt", item.createdAt)
    }

    private fun readingPlanJson(item: ReadingPlanEntity) = JSONObject().apply {
        put("id", item.id); put("projectId", item.projectId); put("title", item.title); put("totalPages", item.totalPages)
        put("dailyTarget", item.dailyTarget); put("currentPage", item.currentPage); put("startDate", item.startDate)
        putNullable("deadline", item.deadline); put("isPaused", item.isPaused); put("isArchived", item.isArchived)
        put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun readingTargetJson(item: ReadingTargetEntity) = JSONObject().apply {
        put("id", item.id); put("planId", item.planId); put("localDate", item.localDate)
        put("targetPages", item.targetPages); put("updatedAt", item.updatedAt)
    }

    private fun pageLogJson(item: PageLogEntity) = JSONObject().apply {
        put("id", item.id); put("planId", item.planId); put("localDate", item.localDate); put("pagesRead", item.pagesRead)
        putNullable("startPage", item.startPage); putNullable("endPage", item.endPage); put("createdAt", item.createdAt)
    }

    private fun todoJson(item: TodoEntity) = JSONObject().apply {
        put("id", item.id); put("title", item.title); put("notes", item.notes); put("isRequired", item.isRequired)
        put("repeatRule", item.repeatRule); put("customRepeatDays", item.customRepeatDays); putNullable("dueDate", item.dueDate)
        put("completedDates", item.completedDates); put("isArchived", item.isArchived); put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun focusJson(item: FocusSessionEntity) = JSONObject().apply {
        put("id", item.id); putNullable("projectId", item.projectId); putNullable("taskId", item.taskId)
        put("startedAt", item.startedAt); putNullable("endedAt", item.endedAt); put("plannedMinutes", item.plannedMinutes)
        put("actualMinutes", item.actualMinutes); put("status", item.status)
    }

    private fun goalJson(item: GoalEntity) = JSONObject().apply {
        put("id", item.id); put("title", item.title); put("metric", item.metric); put("targetValue", item.targetValue)
        put("period", item.period); put("startDate", item.startDate); putNullable("endDate", item.endDate); putNullable("projectId", item.projectId)
        put("isArchived", item.isArchived); put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun countdownJson(item: CountdownEntity) = JSONObject().apply {
        put("id", item.id); put("title", item.title); put("note", item.note); put("eventAtEpochMillis", item.eventAtEpochMillis)
        putNullable("reminderMinutesBefore", item.reminderMinutesBefore); put("isCompleted", item.isCompleted); put("isArchived", item.isArchived)
        put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun reminderJson(item: ReminderEntity) = JSONObject().apply {
        put("id", item.id); putNullable("projectId", item.projectId); put("kind", item.kind); put("timeMinutes", item.timeMinutes)
        put("repeatDays", item.repeatDays); put("enabled", item.enabled); putNullable("quietStartMinutes", item.quietStartMinutes); putNullable("quietEndMinutes", item.quietEndMinutes)
        put("createdAt", item.createdAt); put("updatedAt", item.updatedAt)
    }

    private fun settingsJson(item: AppSettings) = JSONObject().apply {
        put("reviewLimit", item.reviewLimit)
        put("summaryReminderEnabled", item.summaryReminderEnabled)
        put("summaryReminderMinutes", item.summaryReminderMinutes)
        put("quietStartMinutes", item.quietStartMinutes)
        put("quietEndMinutes", item.quietEndMinutes)
        put("restDaysCsv", item.restDaysCsv)
    }

    private fun parseProject(o: JSONObject) = ProjectEntity(
        id = o.requiredString("id"), title = o.requiredString("title"), type = o.optString("type", "技能"),
        description = o.optString("description", ""), tagCsv = o.optString("tagCsv", ""), colorHex = o.optString("colorHex", "#64D8CB"),
        isArchived = o.optBoolean("isArchived", false), isPaused = o.optBoolean("isPaused", false), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseTask(o: JSONObject) = LearningTaskEntity(
        id = o.requiredString("id"), projectId = o.requiredString("projectId"), title = o.requiredString("title"),
        prompt = o.optString("prompt", ""), notes = o.optString("notes", ""), source = o.optString("source", ""), isRequired = o.optBoolean("isRequired", true),
        isArchived = o.optBoolean("isArchived", false), hasLearned = o.optBoolean("hasLearned", false), stage = o.optInt("stage", 0),
        nextReviewDate = o.nullableString("nextReviewDate"), snoozedUntil = o.nullableString("snoozedUntil"), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseReviewLog(o: JSONObject) = ReviewLogEntity(
        id = o.requiredString("id"), taskId = o.requiredString("taskId"), rating = o.requiredString("rating"), reviewedOn = o.requiredString("reviewedOn"),
        previousStage = o.optInt("previousStage"), nextStage = o.optInt("nextStage"), nextReviewDate = o.requiredString("nextReviewDate"), createdAt = o.optLong("createdAt"),
    )

    private fun parseReadingPlan(o: JSONObject) = ReadingPlanEntity(
        id = o.requiredString("id"), projectId = o.requiredString("projectId"), title = o.requiredString("title"), totalPages = o.optInt("totalPages"), dailyTarget = o.optInt("dailyTarget"),
        currentPage = o.optInt("currentPage"), startDate = o.requiredString("startDate"), deadline = o.nullableString("deadline"), isPaused = o.optBoolean("isPaused"), isArchived = o.optBoolean("isArchived"),
        createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseReadingTarget(o: JSONObject) = ReadingTargetEntity(
        id = o.requiredString("id"), planId = o.requiredString("planId"),
        localDate = o.requiredString("localDate"), targetPages = o.optInt("targetPages"),
        updatedAt = o.optLong("updatedAt"),
    )

    private fun parsePageLog(o: JSONObject) = PageLogEntity(
        id = o.requiredString("id"), planId = o.requiredString("planId"), localDate = o.requiredString("localDate"), pagesRead = o.optInt("pagesRead"),
        startPage = o.nullableInt("startPage"), endPage = o.nullableInt("endPage"), createdAt = o.optLong("createdAt"),
    )

    private fun parseTodo(o: JSONObject) = TodoEntity(
        id = o.requiredString("id"), title = o.requiredString("title"), notes = o.optString("notes", ""), isRequired = o.optBoolean("isRequired", true),
        repeatRule = o.optString("repeatRule", "ONCE"), customRepeatDays = o.optString("customRepeatDays", ""), dueDate = o.nullableString("dueDate"),
        completedDates = o.optString("completedDates", ""), isArchived = o.optBoolean("isArchived", false), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseFocus(o: JSONObject) = FocusSessionEntity(
        id = o.requiredString("id"), projectId = o.nullableString("projectId"), taskId = o.nullableString("taskId"), startedAt = o.optLong("startedAt"), endedAt = o.nullableLong("endedAt"),
        plannedMinutes = o.optInt("plannedMinutes"), actualMinutes = o.optInt("actualMinutes"), status = o.optString("status", "COMPLETED"),
    )

    private fun parseGoal(o: JSONObject) = GoalEntity(
        id = o.requiredString("id"), title = o.requiredString("title"), metric = o.optString("metric", "FOCUS_MINUTES"), targetValue = o.optInt("targetValue"), period = o.optString("period", "DAILY"),
        startDate = o.requiredString("startDate"), endDate = o.nullableString("endDate"), projectId = o.nullableString("projectId"), isArchived = o.optBoolean("isArchived"), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseCountdown(o: JSONObject) = CountdownEntity(
        id = o.requiredString("id"), title = o.requiredString("title"), note = o.optString("note", ""), eventAtEpochMillis = o.optLong("eventAtEpochMillis"),
        reminderMinutesBefore = o.nullableInt("reminderMinutesBefore"), isCompleted = o.optBoolean("isCompleted"), isArchived = o.optBoolean("isArchived"), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseReminder(o: JSONObject) = ReminderEntity(
        id = o.requiredString("id"), projectId = o.nullableString("projectId"), kind = o.optString("kind", "SUMMARY"), timeMinutes = o.optInt("timeMinutes"),
        repeatDays = o.optString("repeatDays", "1,2,3,4,5,6,7"), enabled = o.optBoolean("enabled", true), quietStartMinutes = o.nullableInt("quietStartMinutes"), quietEndMinutes = o.nullableInt("quietEndMinutes"), createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt"),
    )

    private fun parseSettings(o: JSONObject?): AppSettings? {
        if (o == null) return null
        val restDays = o.optString("restDaysCsv", "").split(',')
            .mapNotNull { it.trim().toIntOrNull()?.takeIf { day -> day in 1..7 } }
            .distinct()
            .sorted()
            .joinToString(",")
        return AppSettings(
            reviewLimit = o.optInt("reviewLimit", 20).coerceIn(1, 1000),
            summaryReminderEnabled = o.optBoolean("summaryReminderEnabled", true),
            summaryReminderMinutes = o.optInt("summaryReminderMinutes", 20 * 60).coerceIn(0, 1439),
            quietStartMinutes = o.optInt("quietStartMinutes", 22 * 60).coerceIn(0, 1439),
            quietEndMinutes = o.optInt("quietEndMinutes", 7 * 60).coerceIn(0, 1439),
            restDaysCsv = restDays,
        )
    }

    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
        return this
    }
    private fun JSONObject.requiredString(key: String): String = optString(key).takeIf(String::isNotBlank) ?: throw BackupException("备份字段缺失：$key")
    private fun JSONObject.nullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)
    private fun JSONObject.nullableInt(key: String): Int? = if (isNull(key)) null else optInt(key)
    private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else optLong(key)
    private fun <T> JSONArray.objects(parser: (JSONObject) -> T): List<T> = (0 until length()).map { parser(getJSONObject(it)) }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val SCHEMA_VERSION = 1
        const val PLAIN_FORMAT = "learn-list-json-v1"
        const val ENCRYPTED_FORMAT = "learn-list-encrypted-v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PBKDF2_ITERATIONS = 210_000
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        const val SALT_SIZE = 16
        const val IV_SIZE = 12
        const val MAX_BACKUP_BYTES = 20 * 1024 * 1024
        const val MIN_PASSWORD_LENGTH = 8
    }
}
