package com.mymoss.learnlist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnListDao {
    @Query("SELECT * FROM projects WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isArchived = 1 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeArchivedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProject(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("SELECT * FROM learning_tasks WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeTasks(): Flow<List<LearningTaskEntity>>

    @Query("SELECT * FROM learning_tasks WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedTasks(): Flow<List<LearningTaskEntity>>

    @Query("SELECT * FROM learning_tasks ORDER BY updatedAt DESC")
    suspend fun getAllTasks(): List<LearningTaskEntity>

    @Query("SELECT * FROM learning_tasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: String): LearningTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: LearningTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<LearningTaskEntity>)

    @Update
    suspend fun updateTask(task: LearningTaskEntity)

    @Query("SELECT * FROM review_logs ORDER BY reviewedOn DESC, createdAt DESC")
    fun observeReviewLogs(): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs ORDER BY reviewedOn DESC, createdAt DESC")
    suspend fun getAllReviewLogs(): List<ReviewLogEntity>

    @Query("SELECT * FROM review_corrections ORDER BY createdAt DESC")
    fun observeReviewCorrections(): Flow<List<ReviewCorrectionEntity>>

    @Query("SELECT * FROM review_corrections ORDER BY createdAt DESC")
    suspend fun getAllReviewCorrections(): List<ReviewCorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLogs(logs: List<ReviewLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewCorrection(correction: ReviewCorrectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewCorrections(corrections: List<ReviewCorrectionEntity>)

    @Query("SELECT * FROM reading_plans WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeReadingPlans(): Flow<List<ReadingPlanEntity>>

    @Query("SELECT * FROM reading_plans WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedReadingPlans(): Flow<List<ReadingPlanEntity>>

    @Query("SELECT * FROM reading_plans ORDER BY updatedAt DESC")
    suspend fun getAllReadingPlans(): List<ReadingPlanEntity>

    @Query("SELECT * FROM reading_targets ORDER BY localDate ASC")
    fun observeReadingTargets(): Flow<List<ReadingTargetEntity>>

    @Query("SELECT * FROM reading_targets ORDER BY localDate ASC")
    suspend fun getAllReadingTargets(): List<ReadingTargetEntity>

    @Query("SELECT * FROM reading_plans WHERE id = :id LIMIT 1")
    suspend fun getReadingPlan(id: String): ReadingPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingPlan(plan: ReadingPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingPlans(plans: List<ReadingPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingTarget(target: ReadingTargetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingTargets(targets: List<ReadingTargetEntity>)

    @Update
    suspend fun updateReadingPlan(plan: ReadingPlanEntity)

    @Query("SELECT * FROM page_logs ORDER BY localDate DESC, createdAt DESC")
    fun observePageLogs(): Flow<List<PageLogEntity>>

    @Query("SELECT * FROM page_logs ORDER BY localDate DESC, createdAt DESC")
    suspend fun getAllPageLogs(): List<PageLogEntity>

    @Query("SELECT * FROM reading_adjustments ORDER BY localDate DESC, createdAt DESC")
    fun observeReadingAdjustments(): Flow<List<ReadingAdjustmentEntity>>

    @Query("SELECT * FROM reading_adjustments ORDER BY localDate DESC, createdAt DESC")
    suspend fun getAllReadingAdjustments(): List<ReadingAdjustmentEntity>

    @Query("SELECT COALESCE(SUM(pagesRead), 0) FROM page_logs WHERE planId = :planId AND localDate = :localDate")
    suspend fun pagesReadOn(planId: String, localDate: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageLog(log: PageLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPageLogs(logs: List<PageLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingAdjustment(adjustment: ReadingAdjustmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingAdjustments(adjustments: List<ReadingAdjustmentEntity>)

    @Query("SELECT * FROM todos WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos ORDER BY updatedAt DESC")
    suspend fun getAllTodos(): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodo(id: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    suspend fun getAllFocusSessions(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE startedAt = :startedAt LIMIT 1")
    suspend fun getFocusSessionByStartedAt(startedAt: Long): FocusSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSessions(sessions: List<FocusSessionEntity>)

    @Query("SELECT * FROM goals WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY updatedAt DESC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY updatedAt DESC")
    suspend fun getAllGoals(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("SELECT * FROM countdowns WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY eventAtEpochMillis ASC")
    fun observeCountdowns(): Flow<List<CountdownEntity>>

    @Query("SELECT * FROM countdowns WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedCountdowns(): Flow<List<CountdownEntity>>

    @Query("SELECT * FROM countdowns ORDER BY eventAtEpochMillis ASC")
    suspend fun getAllCountdowns(): List<CountdownEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdown(countdown: CountdownEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdowns(countdowns: List<CountdownEntity>)

    @Update
    suspend fun updateCountdown(countdown: CountdownEntity)

    @Query("SELECT * FROM reminders ORDER BY timeMinutes ASC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders")
    suspend fun clearReminders()

    @Query("DELETE FROM countdowns")
    suspend fun clearCountdowns()

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()

    @Query("DELETE FROM todos")
    suspend fun clearTodos()

    @Query("DELETE FROM page_logs")
    suspend fun clearPageLogs()

    @Query("DELETE FROM reading_plans")
    suspend fun clearReadingPlans()

    @Query("DELETE FROM reading_targets")
    suspend fun clearReadingTargets()

    @Query("DELETE FROM review_logs")
    suspend fun clearReviewLogs()

    @Query("DELETE FROM review_corrections")
    suspend fun clearReviewCorrections()

    @Query("DELETE FROM learning_tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM reading_adjustments")
    suspend fun clearReadingAdjustments()

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    @Query("DELETE FROM learning_tasks WHERE projectId = :projectId")
    suspend fun deleteTasksForProject(projectId: String)

    @Query("DELETE FROM reading_plans WHERE projectId = :projectId")
    suspend fun deleteReadingPlansForProject(projectId: String)

    @Query("DELETE FROM todos WHERE projectId = :projectId")
    suspend fun deleteTodosForProject(projectId: String)

    @Query("DELETE FROM goals WHERE projectId = :projectId")
    suspend fun deleteGoalsForProject(projectId: String)

    @Query("DELETE FROM reminders WHERE projectId = :projectId")
    suspend fun deleteRemindersForProject(projectId: String)

    @Query("DELETE FROM learning_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM review_logs WHERE taskId = :taskId")
    suspend fun deleteReviewLogsForTask(taskId: String)

    @Query("DELETE FROM review_corrections WHERE taskId = :taskId")
    suspend fun deleteReviewCorrectionsForTask(taskId: String)

    @Query("DELETE FROM reading_plans WHERE id = :id")
    suspend fun deleteReadingPlan(id: String)

    @Query("DELETE FROM reading_targets WHERE planId = :planId")
    suspend fun deleteReadingTargetsForPlan(planId: String)

    @Query("DELETE FROM page_logs WHERE planId = :planId")
    suspend fun deletePageLogsForPlan(planId: String)

    @Query("DELETE FROM reading_adjustments WHERE planId = :planId")
    suspend fun deleteReadingAdjustmentsForPlan(planId: String)

    @Query("DELETE FROM focus_sessions WHERE projectId = :projectId")
    suspend fun deleteFocusSessionsForProject(projectId: String)

    @Query("DELETE FROM focus_sessions WHERE taskId = :taskId")
    suspend fun deleteFocusSessionsForTask(taskId: String)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodo(id: String)

    @Query("DELETE FROM todos WHERE recurrenceSourceId = :sourceId")
    suspend fun deleteTodoInstancesForSource(sourceId: String)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("DELETE FROM countdowns WHERE id = :id")
    suspend fun deleteCountdown(id: String)
}
