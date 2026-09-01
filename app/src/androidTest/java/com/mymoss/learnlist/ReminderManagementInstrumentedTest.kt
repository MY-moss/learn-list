package com.mymoss.learnlist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.local.LearnListDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderManagementInstrumentedTest {
    private lateinit var database: LearnListDatabase
    private lateinit var repository: LearnListRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LearnListDatabase::class.java).build()
        repository = LearnListRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun reminderCanBeDeletedAfterBeingCreated() = runBlocking {
        val project = repository.addProject("提醒测试", "书籍")
        val reminder = repository.addReminder(
            projectId = project.id,
            kind = "PROJECT",
            timeMinutes = 540,
            repeatDays = "1,2,3",
        )
        assertEquals(1, repository.snapshot().reminders.size)

        repository.deleteReminder(reminder.id)

        assertEquals(0, repository.snapshot().reminders.size)
    }

    @Test
    fun reminderCanBeEditedWithoutResettingItsEnabledState() = runBlocking {
        val project = repository.addProject("提醒编辑测试", "课程")
        val reminder = repository.addReminder(
            projectId = project.id,
            kind = "PROJECT",
            timeMinutes = 540,
            repeatDays = "1,2,3",
            quietStartMinutes = 1320,
            quietEndMinutes = 420,
        )
        repository.setReminderEnabled(reminder.id, false)

        val updated = repository.updateReminder(
            reminderId = reminder.id,
            projectId = null,
            kind = "SUMMARY",
            timeMinutes = 1_230,
            repeatDays = "7,1,7",
            quietStartMinutes = 1_380,
            quietEndMinutes = 360,
        )

        assertEquals(reminder.id, updated.id)
        assertEquals("SUMMARY", updated.kind)
        assertEquals(null, updated.projectId)
        assertEquals(1_230, updated.timeMinutes)
        assertEquals("1,7", updated.repeatDays)
        assertEquals(false, updated.enabled)
        assertEquals(reminder.createdAt, updated.createdAt)
        assertTrue(updated.updatedAt >= reminder.updatedAt)
        assertNotEquals(reminder.kind, updated.kind)
    }
}
