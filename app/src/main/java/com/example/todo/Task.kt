package com.example.todo

data class Task (
    val title: String,
    val deadline: String,
    val description: String,
    var isDone: Boolean = false
)