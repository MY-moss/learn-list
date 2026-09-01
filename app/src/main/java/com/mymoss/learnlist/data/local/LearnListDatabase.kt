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
        ReviewCorrectionEntity::class,
        ReadingPlanEntity::class,
        ReadingTargetEntity::class,
        PageLogEntity::class,
        ReadingAdjustmentEntity::class,
        TodoEntity::class,
        FocusSessionEntity::class,
        GoalEntity::class,
        CountdownEntity::class,
        ReminderEntity::class,
    ],
    version = 7,
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

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE learning_tasks ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE reading_plans ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE todos ADD COLUMN projectId TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE goals ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE countdowns ADD COLUMN deletedAt INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_deletedAt ON projects(deletedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_learning_tasks_deletedAt ON learning_tasks(deletedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_plans_deletedAt ON reading_plans(deletedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_projectId ON todos(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_deletedAt ON todos(deletedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_deletedAt ON goals(deletedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_countdowns_deletedAt ON countdowns(deletedAt)")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS review_corrections (id TEXT NOT NULL, taskId TEXT NOT NULL, correctedStage INTEGER NOT NULL, correctedNextReviewDate TEXT NOT NULL, reason TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_corrections_taskId ON review_corrections(taskId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_corrections_createdAt ON review_corrections(createdAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS reading_adjustments (id TEXT NOT NULL, planId TEXT NOT NULL, localDate TEXT NOT NULL, deltaPages INTEGER NOT NULL, reason TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_adjustments_planId ON reading_adjustments(planId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_adjustments_localDate ON reading_adjustments(localDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_adjustments_createdAt ON reading_adjustments(createdAt)")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN actualSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE focus_sessions SET actualSeconds = actualMinutes * 60")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN phase TEXT NOT NULL DEFAULT 'WORK'")
                db.execSQL("ALTER TABLE focus_sessions ADD COLUMN round INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todos ADD COLUMN recurrenceSourceId TEXT")
                db.execSQL("ALTER TABLE todos ADD COLUMN missedPromptPolicy TEXT NOT NULL DEFAULT 'ASK'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_recurrenceSourceId ON todos(recurrenceSourceId)")
            }
        }
    }
}
