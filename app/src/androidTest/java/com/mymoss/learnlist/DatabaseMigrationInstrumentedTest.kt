package com.mymoss.learnlist

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mymoss.learnlist.data.local.LearnListDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationInstrumentedTest {
    @Test
    fun migratesV1ToV3WithoutDroppingExistingTaskColumns() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "learn-list-migration-${System.nanoTime()}.db"
        val helper = createV1Helper(context, databaseName)
        try {
            val database = helper.writableDatabase
            database.execSQL("INSERT INTO learning_tasks (id, projectId, title, prompt, notes, source, isRequired, isArchived, hasLearned, stage, nextReviewDate, snoozedUntil, createdAt, updatedAt) VALUES ('task-1', 'project-1', '旧任务', '', '', '', 1, 0, 0, 0, NULL, NULL, 1, 1)")

            LearnListDatabase.MIGRATION_1_2.migrate(database)
            assertEquals(0, countRows(database, "reading_targets"))
            assertTrue(columnNames(database, "reading_targets").containsAll(setOf("id", "planId", "localDate", "targetPages", "updatedAt")))

            LearnListDatabase.MIGRATION_2_3.migrate(database)
            assertTrue(columnNames(database, "learning_tasks").contains("initialLearningDate"))
            assertEquals(1, countRows(database, "learning_tasks"))
        } finally {
            helper.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migratesV6ToV7WithRecurringTodoPromptFields() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "learn-list-migration-v7-${System.nanoTime()}.db"
        val helper = createV6Helper(context, databaseName)
        try {
            val database = helper.writableDatabase
            database.execSQL("INSERT INTO todos (id, title, notes, isRequired, repeatRule, customRepeatDays, dueDate, completedDates, isArchived, createdAt, updatedAt, projectId, deletedAt) VALUES ('todo-1', '旧重复待办', '', 1, 'DAILY', '', '2026-08-31', '', 0, 1, 1, NULL, NULL)")
            LearnListDatabase.MIGRATION_6_7.migrate(database)
            assertTrue(columnNames(database, "todos").containsAll(setOf("recurrenceSourceId", "missedPromptPolicy")))
            database.query("SELECT missedPromptPolicy FROM todos WHERE id = 'todo-1'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("ASK", cursor.getString(0))
            }
        } finally {
            helper.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun createV1Helper(context: Context, databaseName: String): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE learning_tasks (id TEXT NOT NULL, projectId TEXT NOT NULL, title TEXT NOT NULL, prompt TEXT NOT NULL, notes TEXT NOT NULL, source TEXT NOT NULL, isRequired INTEGER NOT NULL, isArchived INTEGER NOT NULL, hasLearned INTEGER NOT NULL, stage INTEGER NOT NULL, nextReviewDate TEXT, snoozedUntil TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))",
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build(),
        )

    private fun createV6Helper(context: Context, databaseName: String): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE todos (id TEXT NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL, isRequired INTEGER NOT NULL, repeatRule TEXT NOT NULL, customRepeatDays TEXT NOT NULL, dueDate TEXT, completedDates TEXT NOT NULL, isArchived INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, projectId TEXT, deletedAt INTEGER, PRIMARY KEY(id))")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build(),
        )

    private fun countRows(database: SupportSQLiteDatabase, table: String): Int =
        database.query("SELECT COUNT(*) FROM $table").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private fun columnNames(database: SupportSQLiteDatabase, table: String): Set<String> =
        database.query("PRAGMA table_info('$table')").use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow("name")
            buildSet {
                while (cursor.moveToNext()) add(cursor.getString(nameColumn))
            }
        }
}
