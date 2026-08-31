package com.mymoss.learnlist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.SettingsRepository
import com.mymoss.learnlist.data.backup.BackupException
import com.mymoss.learnlist.data.backup.BackupImportMode
import com.mymoss.learnlist.data.backup.BackupService
import com.mymoss.learnlist.data.local.LearnListDatabase
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupServiceInstrumentedTest {
    @Test
    fun encryptedBackupPreviewsRejectsWrongPasswordAndRestoresSettings() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sourceDatabase = inMemoryDatabase(context)
        val sourceRepository = LearnListRepository(sourceDatabase)
        val settings = SettingsRepository(context)
        settings.update { it.copy(restDaysCsv = "6,7") }
        val project = sourceRepository.addProject("备份测试", "书籍")
        sourceRepository.addTask(project.id, "第一章", isRequired = true)
        val recoveredStart = 1_700_000_000_000L
        val firstSession = sourceRepository.recordFocusSessionIfNeeded(25, 25, startedAt = recoveredStart)
        val secondSession = sourceRepository.recordFocusSessionIfNeeded(25, 25, startedAt = recoveredStart)
        assertEquals(firstSession.id, secondSession.id)
        val service = BackupService(sourceRepository, settings)

        val encrypted = service.export(encrypted = true, password = "correct-password")
        val preview = service.preview(encrypted, "correct-password")
        assertTrue(preview.encrypted)
        assertEquals(1, preview.counts["projects"])
        assertEquals(1, preview.counts["tasks"])
        assertEquals(1, preview.counts["settings"])

        try {
            service.preview(encrypted, "wrong-password")
            throw AssertionError("错误密码应该拒绝备份")
        } catch (_: BackupException) {
            // Expected.
        }

        val corrupted = JSONObject(String(service.export(false), StandardCharsets.UTF_8))
            .put("tasks", JSONArray().put(JSONObject().put("id", "orphan").put("projectId", "missing-project").put("title", "损坏任务")))
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
        try {
            service.preview(corrupted, "")
            throw AssertionError("预览阶段应该拒绝孤立引用")
        } catch (_: BackupException) {
            // Expected.
        }
        try {
            service.import(corrupted, mode = BackupImportMode.MERGE)
            throw AssertionError("孤立引用应该拒绝导入")
        } catch (_: BackupException) {
            // Expected.
        }

        val targetDatabase = inMemoryDatabase(context)
        val targetRepository = LearnListRepository(targetDatabase)
        val targetSettings = SettingsRepository(context)
        targetSettings.update { it.copy(restDaysCsv = "1") }
        BackupService(targetRepository, targetSettings).import(
            bytes = encrypted,
            password = "correct-password",
            mode = BackupImportMode.REPLACE,
        )
        assertEquals(1, targetRepository.snapshot().projects.size)
        assertEquals(1, targetRepository.snapshot().tasks.size)
        assertEquals("6,7", targetSettings.settings.first().restDaysCsv)

        sourceDatabase.close()
        targetDatabase.close()
    }

    private fun inMemoryDatabase(context: Context): LearnListDatabase =
        Room.inMemoryDatabaseBuilder(context, LearnListDatabase::class.java).build()
}
