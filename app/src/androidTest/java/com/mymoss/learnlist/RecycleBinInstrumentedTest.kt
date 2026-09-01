package com.mymoss.learnlist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mymoss.learnlist.data.LearnListRepository
import com.mymoss.learnlist.data.local.LearnListDatabase
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecycleBinInstrumentedTest {
    private lateinit var database: LearnListDatabase
    private lateinit var repository: LearnListRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LearnListDatabase::class.java).build()
        repository = LearnListRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun editingTaskPreservesReviewProgressAndRecycleBinRestoresIt() = runBlocking {
        val project = repository.addProject("编辑项目", "书籍")
        val task = repository.addTask(project.id, "原始任务")
        repository.completeInitialLearning(task.id, LocalDate.of(2026, 9, 1))
        val learned = repository.snapshot().tasks.single()

        repository.updateTask(task.id, "修订后的任务", "提示", "资料", "来源", isRequired = false)
        val edited = repository.snapshot().tasks.single()
        assertEquals("修订后的任务", edited.title)
        assertEquals(learned.stage, edited.stage)
        assertEquals(learned.nextReviewDate, edited.nextReviewDate)
        assertEquals(false, edited.isRequired)

        repository.softDeleteTask(task.id)
        assertNotNull(repository.snapshot().tasks.single().deletedAt)
        repository.restoreTask(task.id)
        assertNull(repository.snapshot().tasks.single().deletedAt)
        repository.permanentlyDeleteTask(task.id)
        assertEquals(0, repository.snapshot().tasks.size)
    }

    @Test
    fun linkedTodoCanBeEditedDeletedAndPermanentlyRemoved() = runBlocking {
        val project = repository.addProject("待办项目", "技能")
        val todo = repository.addTodo("练习", projectId = project.id)
        repository.updateTodo(todo.id, "练习新版", "备注", true, "DAILY", "", LocalDate.now(), project.id)
        assertEquals(project.id, repository.snapshot().todos.single().projectId)
        assertEquals("练习新版", repository.snapshot().todos.single().title)
        repository.softDeleteTodo(todo.id)
        assertNotNull(repository.snapshot().todos.single().deletedAt)
        repository.restoreTodo(todo.id)
        assertNull(repository.snapshot().todos.single().deletedAt)
        repository.permanentlyDeleteTodo(todo.id)
        assertEquals(0, repository.snapshot().todos.size)
    }
}
