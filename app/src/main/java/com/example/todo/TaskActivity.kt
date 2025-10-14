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
    private lateinit var dao: TaskDao
    private var taskId: Int? = null //null means new task, not editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        val deadlineInput = findViewById< TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val titleLayout = findViewById<TextInputLayout>(R.id.titleLayout)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val colorGroup = findViewById<RadioGroup>(R.id.colorPickerGroup)


        //init db
        val db = TaskDatabase.getDatabase(this)
        dao = db.taskDao()

        taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
        taskId?.let { id ->
            lifecycleScope.launch {
                val task = dao.getTaskById(id)
                if (task != null) {
                    titleInput.setText(task.title)
                    deadlineInput.setText(task.deadline)
                    descriptionInput.setText(task.description)

                    // restore previously selected color
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


        // Live color change on the input boxes
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


        // Modal Date Picker
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            //opens with today's date as default
            val datePicker = DatePickerDialog(this, { _, selYear, selMonth, selDay ->
                val selectedDate = "${selYear}/${selMonth + 1}/${selDay}"
                deadlineInput.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
        // The user does a normal click and is navigated back without saving
        backButton.setOnClickListener {
            finish()
        }
        // The user does a normal click and is navigated back with saving
        //and task generation
        saveButton.setOnClickListener {
            val taskTitle = titleInput.text.toString().trim()
            if (taskTitle.isEmpty()) {
                titleInput.error = "A title is required"
                return@setOnClickListener
            }

            val taskDeadline = deadlineInput.text.toString().trim().ifEmpty {
                "No deadline"
            }

            val taskDescription = descriptionInput.text.toString().trim().ifEmpty {
                "No description"
            }

            val selectedColorId = when (colorGroup.checkedRadioButtonId) {
                R.id.colorBlue -> R.color.task_blue
                R.id.colorYellow -> R.color.task_yellow
                R.id.colorPink -> R.color.task_pink
                R.id.colorOrange -> R.color.task_orange
                else -> R.color.meadow_beige
            }

            lifecycleScope.launch {
                if (taskId == null) {
                    //create a new task
                    dao.insertTask(
                        Task(
                            title = taskTitle,
                            deadline = taskDeadline,
                            description = taskDescription,
                            colorResId = selectedColorId

                        )
                    )
                    Toast.makeText(
                        this@TaskActivity,
                        "Task saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    taskId?.let { id ->
                        val existing = dao.getTaskById(id)
                        if (existing != null) {
                            val updated = existing.copy(
                                title = taskTitle,
                                deadline = taskDeadline,
                                description = taskDescription,
                                colorResId = selectedColorId
                            )
                            dao.updateTask(updated)
                            Toast.makeText(
                                this@TaskActivity,
                                "Task saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                finish()
            }
            // If the user long clicks, it's ignored and nothing happens
            backButton.setOnLongClickListener { true }
            saveButton.setOnLongClickListener { true }
        }
    }
}