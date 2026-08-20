package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    STUDENT
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["role"]), Index(value = ["email"])]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarColorHex: String = "#3B82F6",
    val photoUri: String? = null,
    val dateOfBirth: String? = null,
    val bio: String? = null,
    val schoolOrGrade: String? = null,
    val streakDays: Int = 0,
    val lastActiveDate: String = ""
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val colorHex: String,
    val iconName: String,
    val driveFolderUrl: String,
    val description: String = ""
)

@Entity(
    tableName = "topics",
    indices = [Index(value = ["subjectId"]), Index(value = ["orderIndex"])]
)
data class TopicEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val name: String,
    val description: String,
    val driveDocUrl: String,
    val orderIndex: Int = 0
)

enum class RepeatSchedule {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKLY
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["topicId"]),
        Index(value = ["dueDate"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subjectId: String,
    val topicId: String,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String, // e.g. "17:00"
    val repeatSchedule: RepeatSchedule = RepeatSchedule.NONE,
    val googleDriveUrl: String = "",
    val googleDriveLabel: String = "Lecture & Materials",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "task_completions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["taskId"]),
        Index(value = ["completionDate"])
    ]
)
data class TaskCompletionEntity(
    @PrimaryKey val id: String, // composite or UUID: "${userId}_${taskId}"
    val taskId: String,
    val userId: String,
    val studentName: String,
    val completedAt: Long,
    val completionDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = true,
    val note: String = ""
)

// Combined UI data representations
data class TaskWithDetails(
    val task: TaskEntity,
    val subject: SubjectEntity?,
    val topic: TopicEntity?,
    val isCompletedByCurrentUser: Boolean,
    val completionCount: Int = 0,
    val totalStudentsCount: Int = 0
)

data class TopicWithStats(
    val topic: TopicEntity,
    val subjectName: String,
    val subjectColorHex: String,
    val totalTasks: Int,
    val completedTasks: Int
)

data class SubjectWithStats(
    val subject: SubjectEntity,
    val totalTopics: Int,
    val totalTasks: Int,
    val completedTasks: Int
)
