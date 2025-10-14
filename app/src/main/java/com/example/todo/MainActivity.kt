package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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

        //load all tasks from db
        adapter = TaskAdapter(emptyList(), dao) { updatedTask ->
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
        completedButton.setOnClickListener {
            startActivity(Intent(this, CompletedTasksActivity::class.java))
        }
        //swipe to complete - with undo function
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val task = adapter.getTaskAt(position)  // use helper function instead of currentList

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
        })
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