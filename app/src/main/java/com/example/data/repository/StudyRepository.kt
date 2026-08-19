package com.example.data.repository

import android.util.Log
import com.example.data.db.StudyDao
import com.example.data.firebase.FirestoreSyncService
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectWithStats
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskWithDetails
import com.example.data.model.TopicEntity
import com.example.data.model.TopicWithStats
import com.example.data.model.UserEntity
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRepository(
    private val studyDao: StudyDao,
    private val firestoreSync: FirestoreSyncService? = null
) {
    private val tag = "StudyRepository"

    private var usersListener: ListenerRegistration? = null
    private var tasksListener: ListenerRegistration? = null
    private var subjectsListener: ListenerRegistration? = null
    private var topicsListener: ListenerRegistration? = null
    private var completionsListener: ListenerRegistration? = null

    val allUsers: Flow<List<UserEntity>> = studyDao.getAllUsers()
    val allStudents: Flow<List<UserEntity>> = studyDao.getStudents()
    val allSubjects: Flow<List<SubjectEntity>> = studyDao.getAllSubjects()
    val allTopics: Flow<List<TopicEntity>> = studyDao.getAllTopics()
    val allTasks: Flow<List<TaskEntity>> = studyDao.getAllTasks()
    val allCompletionLogs: Flow<List<TaskCompletionEntity>> = studyDao.getAllCompletionLogs()

    fun getUser(userId: String): Flow<UserEntity?> = studyDao.getUserById(userId)

    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>> =
        studyDao.getTopicsForSubject(subjectId)

    /**
     * Start real-time Firestore synchronization into Room database
     */
    fun startFirestoreSync(scope: CoroutineScope) {
        val fs = firestoreSync ?: return
        if (!fs.isAvailable()) return

        Log.d(tag, "Starting Firestore real-time listeners...")

        // Synchronize users
        usersListener?.remove()
        usersListener = fs.observeUsers { firestoreUsers ->
            scope.launch {
                studyDao.syncUsersTransaction(firestoreUsers)
            }
        }

        // Synchronize tasks
        tasksListener?.remove()
        tasksListener = fs.observeTasks { firestoreTasks ->
            scope.launch {
                studyDao.syncTasksTransaction(firestoreTasks)
            }
        }

        // Synchronize subjects
        subjectsListener?.remove()
        subjectsListener = fs.observeSubjects { firestoreSubjects ->
            scope.launch {
                studyDao.syncSubjectsTransaction(firestoreSubjects)
            }
        }

        // Synchronize topics
        topicsListener?.remove()
        topicsListener = fs.observeTopics { firestoreTopics ->
            scope.launch {
                studyDao.syncTopicsTransaction(firestoreTopics)
            }
        }

        // Synchronize completions
        completionsListener?.remove()
        completionsListener = fs.observeCompletions { firestoreCompletions ->
            scope.launch {
                studyDao.syncCompletionsTransaction(firestoreCompletions)
            }
        }
    }

    fun stopFirestoreSync() {
        usersListener?.remove()
        tasksListener?.remove()
        subjectsListener?.remove()
        topicsListener?.remove()
        completionsListener?.remove()
        usersListener = null
        tasksListener = null
        subjectsListener = null
        topicsListener = null
        completionsListener = null
    }

    fun getTaskWithDetails(taskId: String, currentUserId: String): Flow<TaskWithDetails?> {
        return combine(
            studyDao.getTaskById(taskId),
            studyDao.getAllSubjects(),
            studyDao.getAllTopics(),
            studyDao.getCompletionsForTask(taskId),
            studyDao.getStudents()
        ) { task, subjects, topics, completions, students ->
            if (task == null) return@combine null

            val subject = subjects.find { it.id == task.subjectId }
            val topic = topics.find { it.id == task.topicId }
            val isCompleted = completions.any { it.userId == currentUserId && it.isCompleted }

            TaskWithDetails(
                task = task,
                subject = subject,
                topic = topic,
                isCompletedByCurrentUser = isCompleted,
                completionCount = completions.count { it.isCompleted },
                totalStudentsCount = students.size
            )
        }.flowOn(Dispatchers.IO)
    }

    fun getTasksWithDetails(currentUserId: String): Flow<List<TaskWithDetails>> {
        return combine(
            studyDao.getAllTasks(),
            studyDao.getAllSubjects(),
            studyDao.getAllTopics(),
            studyDao.getAllCompletionLogs(),
            studyDao.getStudents()
        ) { tasks, subjects, topics, allCompletions, students ->
            val subjectsMap = subjects.associateBy { it.id }
            val topicsMap = topics.associateBy { it.id }
            val completionsByUser = allCompletions.filter { it.userId == currentUserId && it.isCompleted }
                .associateBy { it.taskId }
            val completionCounts = allCompletions.filter { it.isCompleted }
                .groupBy { it.taskId }

            tasks.map { task ->
                TaskWithDetails(
                    task = task,
                    subject = subjectsMap[task.subjectId],
                    topic = topicsMap[task.topicId],
                    isCompletedByCurrentUser = completionsByUser.containsKey(task.id),
                    completionCount = completionCounts[task.id]?.size ?: 0,
                    totalStudentsCount = students.size
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getSubjectsWithStats(currentUserId: String): Flow<List<SubjectWithStats>> {
        return combine(
            studyDao.getAllSubjects(),
            studyDao.getAllTopics(),
            studyDao.getAllTasks(),
            studyDao.getCompletionsForUser(currentUserId)
        ) { subjects, topics, tasks, userCompletions ->
            val completedTaskIds = userCompletions.asSequence().filter { it.isCompleted }.map { it.taskId }.toSet()
            val topicsBySubject = topics.groupBy { it.subjectId }
            val tasksBySubject = tasks.groupBy { it.subjectId }

            subjects.map { subject ->
                val subjectTopics = topicsBySubject[subject.id] ?: emptyList()
                val subjectTasks = tasksBySubject[subject.id] ?: emptyList()
                val completedCount = subjectTasks.count { completedTaskIds.contains(it.id) }

                SubjectWithStats(
                    subject = subject,
                    totalTopics = subjectTopics.size,
                    totalTasks = subjectTasks.size,
                    completedTasks = completedCount
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getTopicsWithStats(currentUserId: String): Flow<List<TopicWithStats>> {
        return combine(
            studyDao.getAllTopics(),
            studyDao.getAllSubjects(),
            studyDao.getAllTasks(),
            studyDao.getCompletionsForUser(currentUserId)
        ) { topics, subjects, tasks, userCompletions ->
            val completedTaskIds = userCompletions.asSequence().filter { it.isCompleted }.map { it.taskId }.toSet()
            val subjectsMap = subjects.associateBy { it.id }
            val tasksByTopic = tasks.groupBy { it.topicId }

            topics.map { topic ->
                val subject = subjectsMap[topic.subjectId]
                val topicTasks = tasksByTopic[topic.id] ?: emptyList()
                val completedCount = topicTasks.count { completedTaskIds.contains(it.id) }

                TopicWithStats(
                    topic = topic,
                    subjectName = subject?.name ?: "General",
                    subjectColorHex = subject?.colorHex ?: "#3B82F6",
                    totalTasks = topicTasks.size,
                    completedTasks = completedCount
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleTaskCompletion(
        taskId: String,
        user: UserEntity,
        isCompleted: Boolean,
        note: String = ""
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val completionId = "${user.id}_${taskId}"

        if (isCompleted) {
            val completion = TaskCompletionEntity(
                id = completionId,
                taskId = taskId,
                userId = user.id,
                studentName = user.name,
                completedAt = System.currentTimeMillis(),
                completionDate = todayStr,
                isCompleted = true,
                note = note
            )
            studyDao.insertCompletion(completion)
            firestoreSync?.saveCompletion(completion)

            // Update user streak if not completed yet today
            if (user.lastActiveDate != todayStr) {
                val newStreak = user.streakDays + 1
                val updatedUser = user.copy(
                    streakDays = newStreak,
                    lastActiveDate = todayStr
                )
                studyDao.updateUser(updatedUser)
                firestoreSync?.saveUser(updatedUser)
            }
        } else {
            studyDao.deleteCompletion(user.id, taskId)
            firestoreSync?.deleteCompletion(completionId)
        }
    }

    suspend fun resetUserProgress(user: UserEntity) {
        studyDao.deleteCompletionsForUser(user.id)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val resetUser = user.copy(
            streakDays = 1,
            lastActiveDate = todayStr
        )
        studyDao.updateUser(resetUser)
        firestoreSync?.saveUser(resetUser)
    }

    suspend fun insertTask(task: TaskEntity) {
        studyDao.insertTask(task)
        firestoreSync?.saveTask(task)
    }

    suspend fun deleteTask(taskId: String) {
        studyDao.deleteTask(taskId)
        firestoreSync?.deleteTask(taskId)
    }

    suspend fun insertSubject(subject: SubjectEntity) {
        studyDao.insertSubject(subject)
        firestoreSync?.saveSubject(subject)
    }

    suspend fun deleteSubject(subjectId: String) {
        studyDao.deleteSubject(subjectId)
        firestoreSync?.deleteSubject(subjectId)
    }

    suspend fun insertTopic(topic: TopicEntity) {
        studyDao.insertTopic(topic)
        firestoreSync?.saveTopic(topic)
    }

    suspend fun deleteTopic(topicId: String) {
        studyDao.deleteTopic(topicId)
        firestoreSync?.deleteTopic(topicId)
    }

    suspend fun insertUser(user: UserEntity) {
        studyDao.insertUser(user)
        firestoreSync?.saveUser(user)
    }

    suspend fun clearAllData() {
        studyDao.deleteAllTasks()
        studyDao.deleteAllSubjects()
        studyDao.deleteAllTopics()
        studyDao.deleteAllCompletions()
        firestoreSync?.clearAllFirestoreData()
    }

    suspend fun removeAllDemoData() {
        studyDao.deleteTask("task_bcs_01")
        studyDao.deleteTask("task_bcs_02")
        studyDao.deleteSubject("subj_bd_affairs")
        studyDao.deleteSubject("subj_english")
        studyDao.deleteSubject("subj_science")
        studyDao.deleteTopic("topic_bd_history")
        studyDao.deleteTopic("topic_eng_lit")
        studyDao.deleteAllCompletions()
    }

    suspend fun seedInitialBcsDataIfEmpty() {
        // Pure Firestore / live user database mode — no mock or demo data seeded
        removeAllDemoData()
    }
}
