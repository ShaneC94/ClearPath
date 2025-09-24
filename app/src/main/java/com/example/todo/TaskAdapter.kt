package com.example.todo

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var taskList: List<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    //tracks which item is expanded - allows for both to expand individually
    //null if none are expanded
    private var expandedTitles = mutableSetOf<Int>()
    private var expandedDescriptions = mutableSetOf<Int>()


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.taskCheckBox)
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val deadline: TextView = itemView.findViewById(R.id.taskDeadline)
        val description: TextView = itemView.findViewById(R.id.taskDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.title.text = task.title
        holder.deadline.text = "Deadline: ${task.deadline}"
        holder.description.text = task.description
        holder.checkBox.isChecked = task.isDone

        //expand/collapse title
        if (expandedTitles.contains(position)) {
            holder.title.maxLines = Int.MAX_VALUE
            holder.title.ellipsize = null
        } else {
            holder.title.maxLines = 2
            holder.title.ellipsize = TextUtils.TruncateAt.END
        }

        //expand/collapse description
        if (expandedDescriptions.contains(position)) {
            holder.description.maxLines = Int.MAX_VALUE
            holder.description.ellipsize = null
        } else {
            holder.description.maxLines = 2
            holder.description.ellipsize = TextUtils.TruncateAt.END
        }

        //toggle title click listener
        holder.title.setOnClickListener {
            if (expandedTitles.contains(position)) {
                expandedTitles.remove(position)
            } else {
                expandedTitles.add(position)
            }
            notifyItemChanged(position)
        }

        //toggle description click listener
        holder.description.setOnClickListener {
            if (expandedDescriptions.contains(position)) {
                expandedDescriptions.remove(position)
            } else {
                expandedDescriptions.add(position)
            }
            notifyItemChanged(position)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
        }
    }
    //tells recyclerview how many tasks exist
    override fun getItemCount(): Int = taskList.size

    //refreshes adapter when the list changes
    fun updateList(newList: List<Task>) {
        taskList = newList
        expandedTitles.clear()
        expandedDescriptions.clear()
        notifyDataSetChanged()
    }


}