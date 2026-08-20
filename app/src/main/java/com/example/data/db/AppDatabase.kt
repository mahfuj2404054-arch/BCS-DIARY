package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ExamEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.ExamAttemptEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class,
        ExamEntity::class,
        QuestionEntity::class,
        ExamAttemptEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_task_production_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
