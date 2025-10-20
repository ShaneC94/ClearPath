package com.example.todo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Swipe callback for marking tasks as completed (used in MainActivity)
fun createSwipeCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    service: TaskService,
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
                service.updateTask(updatedTask)
                adapter.updateList(service.getOngoingTasks())

                // Show the Undo option
                Snackbar.make(recyclerView, "Task marked as completed", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        lifecycleScope.launch {
                            val undoneTask = task.copy(isDone = false)
                            service.updateTask(undoneTask)
                            adapter.updateList(service.getOngoingTasks())
                        }
                    }.show()
            }
        }
    }
}

// Swipe callback for deleting tasks (used in CompletedTasksActivity)
fun createSwipeToDeleteCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    service: TaskService,
    lifecycleScope: CoroutineScope
): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            rv: RecyclerView,
            vh: RecyclerView.ViewHolder,
            t: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val task = adapter.getTaskAt(position)

            lifecycleScope.launch {
                service.deleteTask(task)
                adapter.updateList(service.getCompletedTasks())

                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        lifecycleScope.launch {
                            service.addTask(task)
                            adapter.updateList(service.getCompletedTasks())
                        }
                    }.show()
            }
        }
    }
}
