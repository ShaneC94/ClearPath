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

    private lateinit var adapter: TaskAdapter
    private lateinit var service: TaskService

    // Track current search, color, and sort state
    private var currentSearchQuery: String = ""
    private var currentColorFilter: Int? = null
    private var currentSortOrder: SortOrder = SortOrder.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ----- Initialize TaskService -----
        service = TaskService(this)

        // ----- Get view references -----
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val fab = findViewById<FloatingActionButton>(R.id.taskFab)
        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)
        val completedButton = findViewById<Button>(R.id.completedTasksButton)
        val filterButton = findViewById<ImageButton>(R.id.filterButton)

        // ----- Set up RecyclerView with adapter -----
        adapter = TaskAdapter(emptyList()) { updatedTask ->
            lifecycleScope.launch {
                service.updateTask(updatedTask)
                applyCombinedFilters()
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ----- Handle live search input -----
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                currentSearchQuery = s.toString().trim()
                applyCombinedFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ----- Floating Action Button → Add Task -----
        fab.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        // ----- Navigate to completed tasks screen -----
        completedButton.setOnClickListener {
            startActivity(Intent(this, CompletedTasksActivity::class.java))
        }

        // ----- Filter and sort popup menu -----
        filterButton.setOnClickListener {
            val popup = PopupMenu(this, filterButton)
            popup.menuInflater.inflate(R.menu.color_filter_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Color filtering options
                    R.id.filter_all -> currentColorFilter = null
                    R.id.filter_blue -> currentColorFilter = R.color.task_blue
                    R.id.filter_yellow -> currentColorFilter = R.color.task_yellow
                    R.id.filter_pink -> currentColorFilter = R.color.task_pink
                    R.id.filter_orange -> currentColorFilter = R.color.task_orange
                    R.id.filter_default -> currentColorFilter = R.color.meadow_beige

                    // Deadline sorting options
                    R.id.sort_deadline_asc -> currentSortOrder = SortOrder.ASCENDING
                    R.id.sort_deadline_desc -> currentSortOrder = SortOrder.DESCENDING
                    R.id.sort_deadline_none -> currentSortOrder = SortOrder.NONE
                }

                // Refresh the list with the selected filters/sort order
                applyCombinedFilters()
                true
            }

            popup.show()
        }

        // ----- Swipe to mark task as completed -----
        val itemTouchHelper = ItemTouchHelper(
            createSwipeCallback(adapter, recyclerView, service, lifecycleScope)
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Load tasks initially
        applyCombinedFilters()
    }

    // ----- Refresh tasks when returning from TaskActivity -----
    override fun onResume() {
        super.onResume()
        applyCombinedFilters()
    }

    // ----- Combine search, color filter, and sorting -----
    private fun applyCombinedFilters() {
        lifecycleScope.launch {
            // Apply color filter
            val baseTasks = if (currentColorFilter == null) {
                service.getOngoingTasks()
            } else {
                service.getOngoingTasksByColor(currentColorFilter!!)
            }

            // Apply search filter
            val filteredTasks = if (currentSearchQuery.isBlank()) {
                baseTasks
            } else {
                baseTasks.filter { task ->
                    task.title.contains(currentSearchQuery, ignoreCase = true) ||
                            task.description.contains(currentSearchQuery, ignoreCase = true)
                }
            }

            // Apply sorting by deadline
            val sortedTasks = when (currentSortOrder) {
                SortOrder.ASCENDING -> filteredTasks.sortedWith(compareBy(
                    { it.deadline.isEmpty() }, // put empty deadlines last
                    { it.deadline }
                ))
                SortOrder.DESCENDING -> filteredTasks.sortedWith(compareBy(
                    { it.deadline.isEmpty() },
                    { it.deadline }
                )).reversed()
                SortOrder.NONE -> filteredTasks
            }

            // Update RecyclerView
            adapter.updateList(sortedTasks)
        }
    }
}


// ----- Enum for sorting order -----
enum class SortOrder {
    ASCENDING,
    DESCENDING,
    NONE
}
