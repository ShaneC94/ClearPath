package com.example.todo

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class TaskActivity : AppCompatActivity() {
    private lateinit var service: TaskService
    private var taskId: Int? = null // null means new task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        val deadlineInput = findViewById<TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)

        // ----- Initialize TaskService (Controller) -----
        service = TaskService(this)

        // ----- Check if editing an existing task -----
        taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
        taskId?.let { id ->
            lifecycleScope.launch {
                val task = service.getTaskById(id)
                if (task != null) {
                    titleInput.setText(task.title)
                    deadlineInput.setText(task.deadline)
                    descriptionInput.setText(task.description)

                    // Restore selected color
                    when (task.colorResId) {
                        R.color.task_blue -> colorGroup.check(R.id.colorBlue)
                        R.color.task_yellow -> colorGroup.check(R.id.colorYellow)
                        R.color.task_pink -> colorGroup.check(R.id.colorPink)
                        R.color.task_orange -> colorGroup.check(R.id.colorOrange)
                        else -> colorGroup.clearCheck()
                    }

                    saveButton.text = getString(R.string.update_task)
                } else {
                    Toast.makeText(this@TaskActivity, "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ----- Live color change -----
        colorGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedColorId = when (checkedId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            val color = getColor(selectedColorId)
            titleLayout.boxBackgroundColor = color
            deadlineLayout.boxBackgroundColor = color
            descriptionLayout.boxBackgroundColor = color
        }

        // ----- Date picker -----
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    deadlineInput.setText("$year/${month + 1}/$day")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // ----- Back button -----
        backButton.setOnClickListener { finish() }

        // ----- Save task -----
        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isEmpty()) {
                titleInput.error = "A title is required"
                return@setOnClickListener
            }

            val deadline = deadlineInput.text.toString().ifEmpty { "No deadline" }
            val description = descriptionInput.text.toString().ifEmpty { "No description" }

            val selectedColorId = when (colorGroup.checkedRadioButtonId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            lifecycleScope.launch {
                if (taskId == null) {
                    // Create new task
                    service.addTask(Task(title = title, deadline = deadline, description = description, colorResId = selectedColorId))
                    Toast.makeText(this@TaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    // Update existing task
                    val existing = service.getTaskById(taskId!!)
                    if (existing != null) {
                        val updated = existing.copy(
                            title = title,
                            deadline = deadline,
                            description = description,
                            colorResId = selectedColorId
                        )
                        service.updateTask(updated)
                        Toast.makeText(this@TaskActivity, "Task updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }

            backButton.setOnLongClickListener { true }
            saveButton.setOnLongClickListener { true }
        }
    }
}
