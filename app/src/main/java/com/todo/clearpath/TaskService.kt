package com.todo.clearpath

import android.content.Context

class TaskService(context: Context) {
    private val dao: TaskDao = TaskDatabase.getDatabase(context).taskDao()

    // ----- CRUD Operations -----
    suspend fun addTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun getTaskById(id: Int) = dao.getTaskById(id)


    // ----- Filters & Queries -----
    suspend fun getOngoingTasks() = dao.getOngoingTasks()
    suspend fun getCompletedTasks() = dao.getCompletedTasks()

    suspend fun getOngoingTasksByColor(colorResId: Int) =
        dao.getOngoingTasksByColor(colorResId)
    suspend fun getCompletedTasksByColor(colorResId: Int) =
        dao.getCompletedTasksByColor(colorResId)

    @Suppress("unused")
    suspend fun searchOngoingTasks(query: String) = dao.searchOngoingTasks("%$query%")

    @Suppress("unused")
    suspend fun searchCompletedTasks(query: String) = dao.searchCompletedTasks("%$query%")

}
