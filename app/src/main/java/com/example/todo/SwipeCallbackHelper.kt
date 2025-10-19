package com.example.todo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Swipe callback for marking tasks completed from the ongoing task list.
// Attached to RecyclerView in MainActivity
fun createSwipeCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    dao: TaskDao,
    lifecycleScope: CoroutineScope
): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        // No drag and drop - only swipes
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        // Triggered on left/right swipe
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val task = adapter.getTaskAt(position)

            // Marks the task as completed and updates the DB
            lifecycleScope.launch {
                val updatedTask = task.copy(isDone = true)
                dao.updateTask(updatedTask)
                adapter.updateList(dao.getOngoingTasks())

                // Show the Undo option
                Snackbar.make(recyclerView, "Task marked as completed", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        lifecycleScope.launch {
                            val undoneTask = task.copy(isDone = false)
                            dao.updateTask(undoneTask)
                            adapter.updateList(dao.getOngoingTasks())
                        }
                    }.show()
            }
        }
    }
}

// Swipe callback for deleting tasks from the completed task list.
// Attached to RecyclerView in CompletedTasksActivity
fun createSwipeToDeleteCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    dao: TaskDao,
    lifecycleScope: CoroutineScope
): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        // No drag and drop, only swipes
        override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

        // Left/right swipe to trigger
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val task = adapter.getTaskAt(position)

            // Delete the task from the DB
            lifecycleScope.launch {
                dao.deleteTask(task)
                adapter.updateList(dao.getCompletedTasks())

                //Show Undo option
                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        lifecycleScope.launch {
                            dao.insertTask(task)
                            adapter.updateList(dao.getCompletedTasks())
                        }
                    }.show()
            }
        }
    }
}

