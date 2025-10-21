package com.todo.clearpath

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
    private lateinit var service: TaskService

    // Track current search and color filter state
    private var currentSearchQuery: String = ""
    private var currentColorFilter: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_tasks)

        // ----- Initialize TaskService (Controller) -----
        service = TaskService(this)

        // ----- Get UI references -----
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val backButton = findViewById<Button>(R.id.backToOngoingButton)
        val recyclerView = findViewById<RecyclerView>(R.id.completedRecyclerView)
        val filterButton = findViewById<ImageButton>(R.id.filterButton)

        // ----- Filter by color using popup menu -----
        filterButton.setOnClickListener {
            val popup = PopupMenu(this, filterButton)
            popup.menuInflater.inflate(R.menu.color_filter_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                currentColorFilter = when (item.itemId) {
                    R.id.filter_all -> null
                    R.id.filter_blue -> R.color.task_blue
                    R.id.filter_yellow -> R.color.task_yellow
                    R.id.filter_pink -> R.color.task_pink
                    R.id.filter_orange -> R.color.task_orange
                    R.id.filter_default -> R.color.meadow_beige
                    else -> null
                }
                applyCombinedFilters()
                true
            }

            popup.show()
        }

        // ----- Search bar input handler -----
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                currentSearchQuery = s.toString().trim()
                applyCombinedFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ----- Back button: return to main screen -----
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // ----- Initialize adapter and RecyclerView -----
        adapter = TaskAdapter(emptyList()) { task ->
            lifecycleScope.launch {
                service.updateTask(task)
                applyCombinedFilters()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ----- Swipe to delete with undo option -----
        val itemTouchHelper = ItemTouchHelper(
            createSwipeToDeleteCallback(adapter, recyclerView, service, lifecycleScope)
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Initial load
        applyCombinedFilters()
    }

    // ----- Refresh filtered list when resuming -----
    override fun onResume() {
        super.onResume()
        applyCombinedFilters()
    }

    // ----- Combined search + color filtering logic -----
    private fun applyCombinedFilters() {
        lifecycleScope.launch {
            val baseTasks = if (currentColorFilter == null) {
                service.getCompletedTasks()
            } else {
                service.getCompletedTasksByColor(currentColorFilter!!)
            }

            val filteredTasks = if (currentSearchQuery.isBlank()) {
                baseTasks
            } else {
                baseTasks.filter { task ->
                    task.title.contains(currentSearchQuery, ignoreCase = true) ||
                            task.description.contains(currentSearchQuery, ignoreCase = true)
                }
            }
            adapter.updateList(filteredTasks)
        }
    }
}
