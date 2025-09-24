package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        //recyclerview setup
        adapter = TaskAdapter(emptyList(), dao)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //load all tasks from db
        lifecycleScope.launch {
            val tasks = dao.getAllTasks()
            adapter.updateList(tasks)
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
                        dao.getAllTasks()
                    } else {
                        dao.searchTasks("%$query%")
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

    }
    override fun onResume(){
        super.onResume()
        lifecycleScope.launch {
            val tasks = dao.getAllTasks()
            adapter.updateList(tasks)
        }
    }
}