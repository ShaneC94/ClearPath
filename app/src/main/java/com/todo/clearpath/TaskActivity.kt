package com.todo.clearpath

import android.app.DatePickerDialog
import android.app.AlertDialog
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
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

class TaskActivity : AppCompatActivity() {
    private lateinit var service: TaskService
    private var taskId: Int? = null

    private lateinit var imageView: ImageView
    private var imageUri: String? = null

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

        imageView = findViewById(R.id.taskImageView)
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)
        val removeImageButton = findViewById<Button>(R.id.removeImageButton)
        val deleteTaskButton = findViewById<Button>(R.id.deleteTaskButton)

        galleryButton.text = getString(R.string.upload)

        service = TaskService(this)

        imageView.visibility = View.GONE

        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val scaledBitmap = scaleCenterCrop(it, 800, 800)
                val resolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "task_image_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ClearPath")
                }

                val imageUriObj = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUriObj?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                    imageUri = uri.toString()
                    imageView.setImageBitmap(scaledBitmap)
                    imageView.visibility = View.VISIBLE

                    imageView.setOnClickListener {
                        imageUri?.let { uriStr ->
                            this@TaskActivity.showImagePopup(uriStr)
                        }
                    }
                }
            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = contentResolver.openInputStream(it)
                    val fileName = "task_image_${System.currentTimeMillis()}.jpg"
                    val file = File(filesDir, fileName)
                    val outputStream = FileOutputStream(file)

                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    imageUri = file.absolutePath
                    imageView.setImageURI(Uri.fromFile(file))
                    imageView.visibility = View.VISIBLE

                    imageView.setOnClickListener {
                        imageUri?.let { uriStr ->
                            this@TaskActivity.showImagePopup(uriStr)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                takePicture.launch(null)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            }
        }

        galleryButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        removeImageButton.setOnClickListener {
            if (imageUri != null) {
                imageUri = null
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
                Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No image to remove", Toast.LENGTH_SHORT).show()
            }
        }

        taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }
        taskId?.let { id ->
            lifecycleScope.launch {
                val task = service.getTaskById(id)
                if (task != null) {
                    titleInput.setText(task.title)
                    deadlineInput.setText(task.deadline)
                    descriptionInput.setText(task.description)

                    when (task.colorResId) {
                        R.color.task_blue -> colorGroup.check(R.id.colorBlue)
                        R.color.task_yellow -> colorGroup.check(R.id.colorYellow)
                        R.color.task_pink -> colorGroup.check(R.id.colorPink)
                        R.color.task_orange -> colorGroup.check(R.id.colorOrange)
                        else -> colorGroup.clearCheck()
                    }

                    task.imageUri?.let {
                        val file = File(it)
                        if (file.exists()) {
                            imageView.setImageURI(Uri.fromFile(file))
                            imageView.visibility = View.VISIBLE
                            imageUri = file.absolutePath
                        } else if (it.startsWith("content://")) {
                            imageView.setImageURI(Uri.parse(it))
                            imageView.visibility = View.VISIBLE
                            imageUri = it
                        }
                    }

                    imageView.setOnClickListener {
                        imageUri?.let { uriStr ->
                            this@TaskActivity.showImagePopup(uriStr)
                        }
                    }

                    deleteTaskButton.visibility = Button.VISIBLE
                    saveButton.text = getString(R.string.update_task)
                } else {
                    Toast.makeText(this@TaskActivity, "Task not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

        backButton.setOnClickListener { finish() }

        deleteTaskButton.setOnClickListener {
            if (taskId != null) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            val task = service.getTaskById(taskId!!)
                            if (task != null) {
                                service.deleteTask(task)
                                Toast.makeText(this@TaskActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "No task to delete", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            if (title.isEmpty()) {
                titleInput.error = "A title is required"
                Toast.makeText(this, "Title is required!", Toast.LENGTH_SHORT).show()
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
                    service.addTask(
                        Task(
                            title = title,
                            deadline = deadline,
                            description = description,
                            colorResId = selectedColorId,
                            imageUri = imageUri
                        )
                    )
                    Toast.makeText(this@TaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    val existing = service.getTaskById(taskId!!)
                    if (existing != null) {
                        val updated = existing.copy(
                            title = title,
                            deadline = deadline,
                            description = description,
                            colorResId = selectedColorId,
                            imageUri = imageUri
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

    private fun scaleCenterCrop(source: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scale: Float
        val dx: Float
        val dy: Float

        val width = source.width.toFloat()
        val height = source.height.toFloat()

        if (width * newHeight > newWidth * height) {
            scale = newHeight / height
            dx = (newWidth - width * scale) * 0.5f
            dy = 0f
        } else {
            scale = newWidth / width
            dx = 0f
            dy = (newHeight - height * scale) * 0.5f
        }

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        val result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(source, matrix, null)
        return result
    }
}
