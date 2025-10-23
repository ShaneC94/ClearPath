# ClearPath

**ClearPath** is a lightweight Android task management application developed in Kotlin using the Room persistence library and the Model-View-Controller (MVC) architectural pattern.  
It provides a simple, responsive interface for managing daily tasks efficiently with built-in color categorization, sorting, and persistent local storage.

---

## Overview

ClearPath allows users to:
- Add, edit, delete, and mark tasks as completed  
- Assign color labels for quick organization  
- Search and sort tasks by color or deadline
- Upload or capture images for tasks
- View attached images with a fullscreen popup viewer
- View completed tasks in a dedicated section  
- Remove tasks easily using swipe-to-delete  
- Benefit from smooth performance through Kotlin coroutines for asynchronous database operations  

---

## Architecture

The application follows the **MVC architecture** to ensure a clean separation of concerns.

| Component | Description |
|------------|--------------|
| **Model** | Represents the data layer, including the `Task` entity and Room database classes. |
| **View** | Includes the XML layout files and RecyclerView used to display tasks. |
| **Controller** | Handles user interaction and communicates between the View and Model layers through the Activity classes. |

---

## Technologies Used

- **Language:** Kotlin  
- **Architecture:** Model-View-Controller (MVC)  
- **Database:** Room (SQLite abstraction)  
- **Concurrency:** Kotlin Coroutines  
- **UI Components:** RecyclerView, CardView, ConstraintLayout, ScrollView
- **UX Enhancements:** Fullscreen image viwer, swipe gestures, confirmation dialogs  
- **Design Principles:** Material Design Guidelines  

---

## Features

**CRUD Operations**
- Create: Add new tasks with titles, descriptions, deadlines, and colors.
- Read: View all active and completed tasks with smooth scrolling and filtering.
- Update: Edit existing tasks and automatically update changes in the database.
- Delete: Swipe left to delete a task, with Undo support for accidental removals.

**Image Integration**
- Capture new images directly with the camera or upload from the gallery.
- Images are stored locally and linked to their corresponding task records.
- Preview images in a compact thumbnail and tap to expand in fullscreen view.
- Remove or replace images easily when editing tasks.

**Filtering and Sorting**
- Search by keyword across task titles and descriptions.
- Filter by task color or clear filters to view all tasks.
- Sort by deadline in ascending or descending order.


---

## File Structure

```
com.todo.clearpath
│
├── CompletedTasksActivity.kt    # Displays completed tasks
├── ImagePopup.kt                # Handles fullscreen image viewer dialog
├── MainActivity.kt              # Displays and manages active tasks
├── SwipeCallbackHelper.kt       # Handles swipe gestures and undo actions
├── Task.kt                      # Task entity definition (Room @Entity)
├── TaskActivity.kt              # Add/Edit task screen with image support
├── TaskAdapter.kt               # RecyclerView adapter for displaying tasks
├── TaskDao.kt                   # Data Access Object for CRUD operations
├── TaskDatabase.kt              # Singleton Room database instance
└── TaskService.kt               # Service layer for task operations
```

---

## Database Design

### Task Entity
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val deadline: String,
    val isDone: Boolean = false,
    val colorResId: Int,
    val imageUri: String? = null
)
```

### Data Access Object
```kotlin
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getActiveTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
```

### Database Builder
```kotlin
@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/ShaneC94/ClearPath.git
   ```

2. **Open in Android Studio**
   - Select *File → Open* and choose the project directory.

3. **Build and run**
   - Use *Run → Run 'app'* to deploy on an emulator or physical Android device.

4. **Usage**
   - Use the plus button to add new tasks.
   - Search or filter tasks by color or deadline using the menu options.
   - Swipe tasks to delete them.
   - Mark tasks as completed to move them to the completed tasks screen.

---

## Future Enhancements

- In-place task editing and updates  
- Deadline notifications and reminders  
- Calendar view and integration  
- Cloud synchronization for multi-device use  
- Light and dark theme support  
- Text recognition for scanning handwritten to-do lists (ML Kit integration)  
- And more to come!

---

## Author

**Shane Currie**  
Software Engineering Student, Ontario Tech University  
Website: [smcurrie.com](https://smcurrie.com)  
LinkedIn: [Shane Currie](https://www.linkedin.com/in/shane-currie-24bb09293/)  
GitHub: [ShaneC94](https://github.com/ShaneC94)
