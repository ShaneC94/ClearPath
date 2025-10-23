package com.todo.clearpath

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")

data class Task (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val deadline: String,
    val description: String,
    var isDone: Boolean = false,
    val colorResId: Int = R.color.meadow_beige, // default color
    val imageUri: String? = null
)