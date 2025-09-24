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
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
//    acts like in-memory storage - making shared task list accessible
    companion object {
        val tasks = mutableListOf<Task>()
    }

    private lateinit var adapter: TaskAdapter //initialized later in onCreate

    //init, set layout, UI configuration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //connect xml views to kotlin var
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)

        //recyclerview setup
        adapter = TaskAdapter(tasks)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        //handles typing in the search bar
        //if empty - show all tasks, else filter
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
                val filtered = if (query.isEmpty()) {
                    tasks
                } else {
                    tasks.filter {it.title.contains(query, ignoreCase = true)}
                }
                adapter.updateList(filtered)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //handles the FAB click
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onResume(){
        super.onResume()
        adapter.updateList(tasks) //shows all the tasks by default
    }
}