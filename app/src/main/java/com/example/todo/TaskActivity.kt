package com.example.todo

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task_generator)

        val deadlineInput = findViewById< TextInputEditText>(R.id.editDeadlineDate)
        val deadlineLayout = findViewById<TextInputLayout>(R.id.deadlineLayout)
        val titleInput = findViewById<TextInputEditText>(R.id.editTitle)
        val descriptionInput = findViewById<TextInputEditText>(R.id.editDescription)
        val backButton = findViewById<Button>(R.id.backButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        //adds vertical scrollbars so user can scroll through longer
        //descriptions without issues
//        descriptionInput.isVerticalScrollBarEnabled = true
//        descriptionInput.setHorizontallyScrolling(false)
//        descriptionInput.maxLines = Integer.MAX_VALUE

        // Modal Date Picker
        deadlineLayout.setEndIconOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            //opens with today's date as default
            val datePicker = DatePickerDialog(this, {_, selYear, selMonth, selDay ->
                val selectedDate = "${selYear}/${selMonth + 1}/${selDay}"
                deadlineInput.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
        // The user does a normal click and is navigated back without saving
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
            val taskDeadline = if (deadlineInput.text.toString().trim().isEmpty()) {
                "No deadline"
            } else {
                deadlineInput.text.toString().trim()
            }
            val taskDescription = if (descriptionInput.text.toString().trim().isEmpty()) {
                "No description"
            } else {
                descriptionInput.text.toString().trim()
            }

            MainActivity.tasks.add(Task(taskTitle, taskDeadline, taskDescription))
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // If the user long clicks, it's ignored and nothing happens
        backButton.setOnLongClickListener { true }
        saveButton.setOnLongClickListener { true }
    }
}