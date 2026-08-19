package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'STUDENT'")
    fun getStudents(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM users WHERE id NOT IN (:ids)")
    suspend fun deleteUsersNotIn(ids: Set<String>)

    @androidx.room.Transaction
    suspend fun syncUsersTransaction(users: List<UserEntity>) {
        if (users.isNotEmpty()) {
            val fetchedIds = users.map { it.id }.toSet()
            deleteUsersNotIn(fetchedIds)
            insertUsers(users)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)

    // --- Topics ---
    @Query("SELECT * FROM topics ORDER BY orderIndex ASC, name ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopic(id: String)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, dueTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdDirect(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopics()

    @Query("DELETE FROM task_completions")
    suspend fun deleteAllCompletions()

    // --- Task Completions ---
    @Query("SELECT * FROM task_completions WHERE userId = :userId")
    fun getCompletionsForUser(userId: String): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    fun getCompletionsForTask(taskId: String): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions ORDER BY completedAt DESC")
    fun getAllCompletionLogs(): Flow<List<TaskCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<TaskCompletionEntity>)

    @androidx.room.Transaction
    suspend fun syncTasksTransaction(tasks: List<TaskEntity>) {
        deleteAllTasks()
        if (tasks.isNotEmpty()) {
            insertTasks(tasks)
        }
    }

    @androidx.room.Transaction
    suspend fun syncSubjectsTransaction(subjects: List<SubjectEntity>) {
        if (subjects.isNotEmpty()) {
            deleteAllSubjects()
            insertSubjects(subjects)
        }
    }

    @androidx.room.Transaction
    suspend fun syncTopicsTransaction(topics: List<TopicEntity>) {
        if (topics.isNotEmpty()) {
            deleteAllTopics()
            insertTopics(topics)
        }
    }

    @androidx.room.Transaction
    suspend fun syncCompletionsTransaction(completions: List<TaskCompletionEntity>) {
        if (completions.isNotEmpty()) {
            deleteAllCompletions()
            insertCompletions(completions)
        }
    }

    @Query("DELETE FROM task_completions WHERE userId = :userId AND taskId = :taskId")
    suspend fun deleteCompletion(userId: String, taskId: String)

    @Query("DELETE FROM task_completions WHERE userId = :userId")
    suspend fun deleteCompletionsForUser(userId: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int
}
