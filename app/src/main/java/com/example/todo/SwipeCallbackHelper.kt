package com.example.todo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun createSwipeCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    dao: TaskDao,
    lifecycleScope: CoroutineScope
): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val task = adapter.getTaskAt(position)

            lifecycleScope.launch {
                val updatedTask = task.copy(isDone = true)
                dao.updateTask(updatedTask)
                adapter.updateList(dao.getOngoingTasks())

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

fun createSwipeToDeleteCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    dao: TaskDao,
    lifecycleScope: CoroutineScope
): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val task = adapter.getTaskAt(position)

            lifecycleScope.launch {
                dao.deleteTask(task)
                adapter.updateList(dao.getCompletedTasks())

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

