package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isPublished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val examId: String,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val timeLimitSeconds: Int = 30,
    val explanation: String = ""
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val examId: String,
    val score: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val totalTimeSeconds: Int,
    val completedAt: Long = System.currentTimeMillis()
)

data class ExamLeaderboardEntry(
    val userId: String,
    val userName: String,
    val avatarColorHex: String,
    val score: Int,
    val timeSeconds: Int,
    val completedAt: Long
)

data class ExamReviewItem(
    val question: QuestionEntity,
    val selectedOption: String?, // null means skipped
    val wasTimeUp: Boolean = false
)

