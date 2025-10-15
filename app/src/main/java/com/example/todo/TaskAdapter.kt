package com.example.todo

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(
    private var taskList: List<Task>,
    private val onTaskUpdated: suspend (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Tracks expanded titles/descriptions
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
        holder.deadline.text = holder.itemView.context.getString(
            R.string.deadline_task_card,
            task.deadline
        )
        holder.description.text = task.description
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isDone

        // Long click → edit task
        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TaskActivity::class.java)
            intent.putExtra("taskId", task.id)
            context.startActivity(intent)
            true
        }

        // Apply background color from task
        (holder.itemView as CardView).setCardBackgroundColor(
            holder.itemView.context.getColor(task.colorResId)
        )

        // Expand / collapse title
        if (expandedTitles.contains(position)) {
            holder.title.maxLines = Int.MAX_VALUE
            holder.title.ellipsize = null
        } else {
            holder.title.maxLines = 2
            holder.title.ellipsize = TextUtils.TruncateAt.END
        }

        // Expand / collapse description
        if (expandedDescriptions.contains(position)) {
            holder.description.maxLines = Int.MAX_VALUE
            holder.description.ellipsize = null
        } else {
            holder.description.maxLines = 2
            holder.description.ellipsize = TextUtils.TruncateAt.END
        }

        // Title toggle
        holder.title.setOnClickListener {
            if (expandedTitles.contains(position)) expandedTitles.remove(position)
            else expandedTitles.add(position)
            notifyItemChanged(position)
        }

        // Description toggle
        holder.description.setOnClickListener {
            if (expandedDescriptions.contains(position)) expandedDescriptions.remove(position)
            else expandedDescriptions.add(position)
            notifyItemChanged(position)
        }

        // Check / uncheck → update database
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val updated = task.copy(isDone = isChecked) // fix property name
            CoroutineScope(Dispatchers.IO).launch {
                onTaskUpdated(updated)
            }
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun updateList(newList: List<Task>) {
        taskList = newList
        expandedTitles.clear()
        expandedDescriptions.clear()
        notifyDataSetChanged()
    }

    // Helper for swipe-to-complete/delete
    fun getTaskAt(position: Int): Task = taskList[position]
}
