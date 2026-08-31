package com.mymoss.learnlist.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProjectEntity::class,
        LearningTaskEntity::class,
        ReviewLogEntity::class,
        ReadingPlanEntity::class,
        ReadingTargetEntity::class,
        PageLogEntity::class,
        TodoEntity::class,
        FocusSessionEntity::class,
        GoalEntity::class,
        CountdownEntity::class,
        ReminderEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class LearnListDatabase : RoomDatabase() {
    abstract fun dao(): LearnListDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS reading_targets (id TEXT NOT NULL, planId TEXT NOT NULL, localDate TEXT NOT NULL, targetPages INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_reading_targets_planId_localDate ON reading_targets(planId, localDate)")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE learning_tasks ADD COLUMN initialLearningDate TEXT")
            }
        }
    }
}

