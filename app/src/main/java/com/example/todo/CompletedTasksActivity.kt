package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CompletedTasksActivity : AppCompatActivity() {
    private lateinit var adapter: TaskAdapter
    private lateinit var dao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_tasks)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val backButton = findViewById<Button>(R.id.backToOngoingButton)
        val db = TaskDatabase.getDatabase(this)
        dao = db.taskDao()

        val recyclerView = findViewById<RecyclerView>(R.id.completedRecyclerView)

        //handle search input (filter db)
        searchInput.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {}
            override fun onTextChanged(s: CharSequence?,
                                       start: Int,
                                       count: Int,
                                       after: Int) {
                val query = s.toString().trim()
                lifecycleScope.launch {
                    val tasks = if (query.isEmpty()) {
                        dao.getCompletedTasks()
                    } else {
                        dao.searchCompletedTasks("%$query%")
                    }
                    adapter.updateList(tasks)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        adapter = TaskAdapter(emptyList(), dao) { task ->
            lifecycleScope.launch {
                dao.updateTask(task)
                refreshTasks()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

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
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        refreshTasks()
    }

    private fun refreshTasks() {
        lifecycleScope.launch {
            val completedTasks = dao.getCompletedTasks()
            adapter.updateList(completedTasks)
        }
    }

    override fun onResume(){
        super.onResume()
        refreshTasks()
    }
}