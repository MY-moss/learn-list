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
}
