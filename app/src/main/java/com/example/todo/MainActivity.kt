package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter //initialized later in onCreate
    private lateinit var db: TaskDatabase
    private lateinit var dao: TaskDao

    //init, set layout, UI configuration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //init db
        db = TaskDatabase.getDatabase(this)
        dao = db.taskDao()

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)
        val completedButton = findViewById<Button>(R.id.completedTasksButton)
        val filterButton = findViewById<ImageButton>(R.id.filterButton)

        //load all tasks from db
        adapter = TaskAdapter(emptyList()) { updatedTask ->
            lifecycleScope.launch {
                dao.updateTask(updatedTask)
                val tasks = dao.getOngoingTasks()
                adapter.updateList(tasks)
            }
        }

        //recyclerview setup
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //handle search input (filter db)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                val query = s.toString().trim()
                lifecycleScope.launch {
                    val tasks = if (query.isEmpty()) {
                        dao.getOngoingTasks()
                    } else {
                        dao.searchOngoingTasks("%$query%")
                    }
                    adapter.updateList(tasks)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //handles the FAB clicker to create new task
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
        //navigate to the completed tasks
        completedButton.setOnClickListener {
            startActivity(Intent(this, CompletedTasksActivity::class.java))
        }
        //filters by color popup
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

                lifecycleScope.launch {
                    val tasks = if (selectedColorResId == null) {
                        dao.getOngoingTasks()
                    } else {
                        dao.getOngoingTasksByColor(selectedColorResId)
                    }
                    adapter.updateList(tasks)
                }

                true
            }

            popup.show()
        }


        //swipe to complete - with undo function
        val itemTouchHelper = ItemTouchHelper(
            createSwipeCallback(adapter, recyclerView, dao, lifecycleScope)
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

        override fun onResume(){
        super.onResume()
        lifecycleScope.launch {
            val tasks = dao.getOngoingTasks()
            adapter.updateList(tasks)
        }
    }
}