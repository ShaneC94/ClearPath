package com.todo.clearpath

import android.content.Intent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MainActivity swipe logic:
 * - Swipe LEFT → Delete task (with Undo)
 * - Swipe RIGHT → Edit task (opens TaskActivity)
 */
fun createMainSwipeCallback(
    adapter: TaskAdapter,
    recyclerView: RecyclerView,
    service: TaskService,
    lifecycleScope: CoroutineScope,
    activity: MainActivity
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

            when (direction) {

                // Swipe LEFT → Delete task
                ItemTouchHelper.LEFT -> {
                    lifecycleScope.launch {
                        service.deleteTask(task)
                        adapter.updateList(service.getOngoingTasks())

                        Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                lifecycleScope.launch {
                                    service.addTask(task)
                                    adapter.updateList(service.getOngoingTasks())
                                }
                            }.show()
                    }
                }

                // Swipe RIGHT → Edit task
                ItemTouchHelper.RIGHT -> {
                    val intent = Intent(activity, TaskActivity::class.java).apply {
                        putExtra("taskId", task.id)
                    }
                    activity.startActivity(intent)

                    // Reset swipe visual (so it doesn’t stay dismissed)
                    adapter.notifyItemChanged(position)
                }
            }
        }
    }
}

/**
 * CompletedTasksActivity swipe logic:
 * - Swipe LEFT → Permanently delete (with Undo)
 * - Swipe RIGHT → Return task to MainActivity (mark as incomplete)
 */
fun createCompletedSwipeCallback(
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

            when (direction) {

                // Swipe LEFT → Delete permanently
                ItemTouchHelper.LEFT -> {
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

                // Swipe RIGHT → Move back to ongoing (MainActivity) with Undo
                ItemTouchHelper.RIGHT -> {
                    // Temporarily remove the item from completed list
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isDone = false)
                        service.deleteTask(task)
                        adapter.updateList(service.getCompletedTasks())

                        val snackbar = Snackbar.make(
                            recyclerView,
                            "Task moved back to ongoing",
                            Snackbar.LENGTH_LONG
                        )

                        snackbar.setAction("UNDO") {
                            lifecycleScope.launch {
                                service.addTask(task.copy(isDone = true))
                                adapter.updateList(service.getCompletedTasks())
                            }
                        }

                        snackbar.addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                // Only commit if UNDO was not pressed
                                if (event != DISMISS_EVENT_ACTION) {
                                    lifecycleScope.launch {
                                        service.addTask(updatedTask)
                                    }
                                }
                            }
                        })

                        snackbar.show()
                    }
                }
            }
        }
    }
}

