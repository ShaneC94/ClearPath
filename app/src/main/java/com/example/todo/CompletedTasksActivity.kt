package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val filterButton = findViewById<ImageButton>(R.id.filterButton)

        //color filter popup
        filterButton.setOnClickListener {
            val popup = PopupMenu(this, filterButton)
            popup.menuInflater.inflate(R.menu.color_filter_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                val selectedColorResId = when (item.itemId) {
                    R.id.filter_all -> null
                    R.id.filter_blue -> R.color.task_blue
                    R.id.filter_yellow -> R.color.task_yellow
                    R.id.filter_pink -> R.color.task_pink
                    R.id.filter_orange -> R.color.task_orange
                    R.id.filter_default -> R.color.meadow_beige
                    else -> null
                }
                //queries and updates recyclerview based on color
                lifecycleScope.launch {
                    val tasks = if (selectedColorResId == null) {
                        dao.getCompletedTasks()
                    } else {
                        dao.getCompletedTasksByColor(selectedColorResId)
                    }
                    adapter.updateList(tasks)
                }

                true
            }

            popup.show()
        }

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

        adapter = TaskAdapter(emptyList()) { task ->
            lifecycleScope.launch {
                dao.updateTask(task)
                refreshTasks()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        //swipe to delete with undo
        val itemTouchHelper = ItemTouchHelper(
            createSwipeToDeleteCallback(adapter, recyclerView, dao, lifecycleScope)
        )
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