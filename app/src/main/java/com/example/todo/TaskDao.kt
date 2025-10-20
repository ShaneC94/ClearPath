package com.example.todo

import androidx.room.*
@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY id DESC")
    suspend fun getOngoingTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 1 ORDER BY id DESC")
    suspend fun getCompletedTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY id DESC")
    suspend fun getTasksNow(): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 1 ORDER BY id DESC")
    suspend fun getCompletedTasksNow(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND (title LIKE :query OR description LIKE :query OR deadline LIKE :query)")
    suspend fun searchOngoingTasks(query: String): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 1 AND (title LIKE :query OR description LIKE :query OR deadline LIKE :query)")
    suspend fun searchCompletedTasks(query: String): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND colorResId = :colorResId")
    suspend fun getOngoingTasksByColor(colorResId: Int): List<Task>

    @Query("SELECT * FROM tasks WHERE isDone = 1 AND colorResId = :colorResId")
    suspend fun getCompletedTasksByColor(colorResId: Int): List<Task>
}