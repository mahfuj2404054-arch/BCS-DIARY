package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ExamAttemptEntity
import com.example.data.model.ExamEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.RepeatSchedule
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TopicEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class FirestoreSyncService(private val context: Context) {

    private val tag = "FirestoreSyncService"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                FirebaseApp.initializeApp(context)
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firestore: ${e.message}")
            null
        }
    }

    fun isAvailable(): Boolean = firestore != null

    // ==========================================
    // USERS
    // ==========================================

    suspend fun saveUser(user: UserEntity) {
        val db = firestore ?: return
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "role" to user.role.name,
                "avatarColorHex" to user.avatarColorHex,
                "photoUri" to user.photoUri,
                "dateOfBirth" to user.dateOfBirth,
                "bio" to user.bio,
                "schoolOrGrade" to user.schoolOrGrade,
                "streakDays" to user.streakDays,
                "lastActiveDate" to user.lastActiveDate,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(user.id).set(userMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "User saved to Firestore: ${user.name} (${user.id})")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save user to Firestore: ${e.message}")
        }
    }

    suspend fun getUser(userId: String): UserEntity? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection("users").document(userId).get().awaitTask()
            if (doc.exists()) {
                UserEntity(
                    id = doc.getString("id") ?: userId,
                    name = doc.getString("name") ?: "Student",
                    email = doc.getString("email") ?: "",
                    role = try {
                        UserRole.valueOf(doc.getString("role") ?: "STUDENT")
                    } catch (_: Exception) {
                        UserRole.STUDENT
                    },
                    avatarColorHex = doc.getString("avatarColorHex") ?: "#8B5CF6",
                    photoUri = doc.getString("photoUri"),
                    dateOfBirth = doc.getString("dateOfBirth"),
                    bio = doc.getString("bio"),
                    schoolOrGrade = doc.getString("schoolOrGrade"),
                    streakDays = doc.getLong("streakDays")?.toInt() ?: 1,
                    lastActiveDate = doc.getString("lastActiveDate") ?: ""
                )
            } else null
        } catch (e: Exception) {
            Log.e(tag, "Failed to get user from Firestore: ${e.message}", e)
            null
        }
    }

    suspend fun deleteUser(userId: String) {
        val db = firestore ?: return
        try {
            db.collection("users").document(userId).delete().awaitTask()
            Log.d(tag, "User deleted from Firestore: $userId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete user from Firestore: ${e.message}", e)
        }
    }

    fun observeCurrentUser(userId: String, onUserChanged: (UserEntity) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("users").document(userId).addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e(tag, "Listen failed for current user: ${error.message}")
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    try {
                        val user = UserEntity(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "Student",
                            email = doc.getString("email") ?: "",
                            role = try {
                                UserRole.valueOf(doc.getString("role") ?: "STUDENT")
                            } catch (_: Exception) {
                                UserRole.STUDENT
                            },
                            avatarColorHex = doc.getString("avatarColorHex") ?: "#8B5CF6",
                            photoUri = doc.getString("photoUri"),
                            dateOfBirth = doc.getString("dateOfBirth"),
                            bio = doc.getString("bio"),
                            schoolOrGrade = doc.getString("schoolOrGrade"),
                            streakDays = doc.getLong("streakDays")?.toInt() ?: 1,
                            lastActiveDate = doc.getString("lastActiveDate") ?: ""
                        )
                        onUserChanged(user)
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing current user doc: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to observe current user: ${e.message}")
            null
        }
    }

    fun observeUsers(onUsersChanged: (List<UserEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("users").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore users permission restricted")
                    } else {
                        Log.e(tag, "Listen failed for users: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        try {
                            UserEntity(
                                id = doc.getString("id") ?: doc.id,
                                name = doc.getString("name") ?: "Student",
                                email = doc.getString("email") ?: "",
                                role = try {
                                    UserRole.valueOf(doc.getString("role") ?: "STUDENT")
                                } catch (_: Exception) {
                                    UserRole.STUDENT
                                },
                                avatarColorHex = doc.getString("avatarColorHex") ?: "#8B5CF6",
                                photoUri = doc.getString("photoUri"),
                                dateOfBirth = doc.getString("dateOfBirth"),
                                bio = doc.getString("bio"),
                                schoolOrGrade = doc.getString("schoolOrGrade"),
                                streakDays = doc.getLong("streakDays")?.toInt() ?: 1,
                                lastActiveDate = doc.getString("lastActiveDate") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing user doc: ${e.message}")
                            null
                        }
                    }
                    onUsersChanged(users)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach user listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // TASKS
    // ==========================================

    suspend fun saveTask(task: TaskEntity) {
        val db = firestore ?: return
        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "description" to task.description,
                "subjectId" to task.subjectId,
                "topicId" to task.topicId,
                "dueDate" to task.dueDate,
                "dueTime" to task.dueTime,
                "repeatSchedule" to task.repeatSchedule.name,
                "googleDriveUrl" to task.googleDriveUrl,
                "googleDriveLabel" to task.googleDriveLabel,
                "priority" to task.priority.name,
                "createdAt" to task.createdAt
            )
            db.collection("tasks").document(task.id).set(taskMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Task saved to Firestore: ${task.title}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save task to Firestore: ${e.message}")
        }
    }

    suspend fun deleteTask(taskId: String) {
        val db = firestore ?: return
        try {
            db.collection("tasks").document(taskId).delete().awaitTask()
            Log.d(tag, "Task deleted from Firestore: $taskId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete task from Firestore: ${e.message}", e)
        }
    }

    fun observeTasks(onTasksChanged: (List<TaskEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("tasks").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore tasks permission restricted (using local Room database)")
                    } else {
                        Log.e(tag, "Listen failed for tasks: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { doc ->
                        try {
                            val rawTitle = doc.getString("title")
                                ?: doc.getString("name")
                                ?: doc.getString("taskTitle")
                                ?: doc.getString("task")
                                ?: doc.getString("heading")
                                ?: ""

                            val rawDesc = doc.getString("description")
                                ?: doc.getString("details")
                                ?: doc.getString("desc")
                                ?: doc.getString("content")
                                ?: doc.getString("body")
                                ?: ""

                            val rawSubjectId = doc.getString("subjectId")
                                ?: doc.getString("subject_id")
                                ?: doc.getString("subject")
                                ?: doc.getString("subjectName")
                                ?: ""

                            val rawTopicId = doc.getString("topicId")
                                ?: doc.getString("topic_id")
                                ?: doc.getString("topic")
                                ?: doc.getString("topicName")
                                ?: ""

                            val rawDueDate = doc.getString("dueDate")
                                ?: doc.getString("due_date")
                                ?: doc.getString("date")
                                ?: doc.getString("deadline")
                                ?: ""

                            val rawDueTime = doc.getString("dueTime")
                                ?: doc.getString("due_time")
                                ?: doc.getString("time")
                                ?: "23:59"

                            val rawDriveUrl = doc.getString("googleDriveUrl")
                                ?: doc.getString("driveUrl")
                                ?: doc.getString("drive_url")
                                ?: doc.getString("link")
                                ?: doc.getString("url")
                                ?: doc.getString("pdfUrl")
                                ?: ""

                            val rawDriveLabel = doc.getString("googleDriveLabel")
                                ?: doc.getString("driveLabel")
                                ?: doc.getString("drive_label")
                                ?: doc.getString("label")
                                ?: "Study Material (Google Drive)"

                            val rawPriority = doc.getString("priority") ?: doc.getString("taskPriority") ?: "MEDIUM"
                            val rawRepeat = doc.getString("repeatSchedule") ?: doc.getString("repeat") ?: "NONE"

                            TaskEntity(
                                id = doc.getString("id") ?: doc.id,
                                title = rawTitle,
                                description = rawDesc,
                                subjectId = rawSubjectId,
                                topicId = rawTopicId,
                                dueDate = rawDueDate,
                                dueTime = rawDueTime,
                                repeatSchedule = try {
                                    RepeatSchedule.valueOf(rawRepeat.uppercase())
                                } catch (_: Exception) {
                                    RepeatSchedule.NONE
                                },
                                googleDriveUrl = rawDriveUrl,
                                googleDriveLabel = rawDriveLabel,
                                priority = try {
                                    TaskPriority.valueOf(rawPriority.uppercase())
                                } catch (_: Exception) {
                                    TaskPriority.MEDIUM
                                },
                                createdAt = doc.getLong("createdAt") ?: doc.getLong("created_at") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing task doc: ${e.message}")
                            null
                        }
                    }
                    onTasksChanged(tasks)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach task listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // SUBJECTS
    // ==========================================

    suspend fun saveSubject(subject: SubjectEntity) {
        val db = firestore ?: return
        try {
            val subjectMap = hashMapOf(
                "id" to subject.id,
                "name" to subject.name,
                "code" to subject.code,
                "colorHex" to subject.colorHex,
                "iconName" to subject.iconName,
                "driveFolderUrl" to subject.driveFolderUrl,
                "description" to subject.description
            )
            db.collection("subjects").document(subject.id).set(subjectMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Subject saved to Firestore: ${subject.name}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save subject to Firestore: ${e.message}")
        }
    }

    suspend fun deleteSubject(subjectId: String) {
        val db = firestore ?: return
        try {
            db.collection("subjects").document(subjectId).delete().awaitTask()
            Log.d(tag, "Subject deleted from Firestore: $subjectId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete subject from Firestore: ${e.message}", e)
        }
    }

    fun observeSubjects(onSubjectsChanged: (List<SubjectEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("subjects").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore subjects permission restricted (using local Room database)")
                    } else {
                        Log.e(tag, "Listen failed for subjects: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val subjects = snapshot.documents.mapNotNull { doc ->
                        try {
                            SubjectEntity(
                                id = doc.getString("id") ?: doc.id,
                                name = doc.getString("name") ?: "",
                                code = doc.getString("code") ?: "",
                                colorHex = doc.getString("colorHex") ?: "#3B82F6",
                                iconName = doc.getString("iconName") ?: "menu_book",
                                driveFolderUrl = doc.getString("driveFolderUrl") ?: "",
                                description = doc.getString("description") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing subject doc: ${e.message}")
                            null
                        }
                    }
                    onSubjectsChanged(subjects)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach subject listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // TOPICS
    // ==========================================

    suspend fun saveTopic(topic: TopicEntity) {
        val db = firestore ?: return
        try {
            val topicMap = hashMapOf(
                "id" to topic.id,
                "subjectId" to topic.subjectId,
                "name" to topic.name,
                "description" to topic.description,
                "driveDocUrl" to topic.driveDocUrl,
                "orderIndex" to topic.orderIndex
            )
            db.collection("topics").document(topic.id).set(topicMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Topic saved to Firestore: ${topic.name}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save topic to Firestore: ${e.message}")
        }
    }

    suspend fun deleteTopic(topicId: String) {
        val db = firestore ?: return
        try {
            db.collection("topics").document(topicId).delete().awaitTask()
            Log.d(tag, "Topic deleted from Firestore: $topicId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete topic from Firestore: ${e.message}", e)
        }
    }

    fun observeTopics(onTopicsChanged: (List<TopicEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("topics").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore topics permission restricted (using local Room database)")
                    } else {
                        Log.e(tag, "Listen failed for topics: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val topics = snapshot.documents.mapNotNull { doc ->
                        try {
                            TopicEntity(
                                id = doc.getString("id") ?: doc.id,
                                subjectId = doc.getString("subjectId") ?: "",
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                driveDocUrl = doc.getString("driveDocUrl") ?: "",
                                orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing topic doc: ${e.message}")
                            null
                        }
                    }
                    onTopicsChanged(topics)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach topic listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // TASK COMPLETIONS
    // ==========================================

    suspend fun saveCompletion(completion: TaskCompletionEntity) {
        val db = firestore ?: return
        try {
            val compMap = hashMapOf(
                "id" to completion.id,
                "taskId" to completion.taskId,
                "userId" to completion.userId,
                "studentName" to completion.studentName,
                "completedAt" to completion.completedAt,
                "completionDate" to completion.completionDate,
                "isCompleted" to completion.isCompleted,
                "note" to completion.note
            )
            db.collection("task_completions").document(completion.id).set(compMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Task completion saved to Firestore: ${completion.id}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save completion to Firestore: ${e.message}")
        }
    }

    suspend fun deleteCompletion(completionId: String) {
        val db = firestore ?: return
        try {
            db.collection("task_completions").document(completionId).delete().awaitTask()
            Log.d(tag, "Task completion deleted from Firestore: $completionId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete completion from Firestore: ${e.message}", e)
        }
    }

    fun observeCompletions(onCompletionsChanged: (List<TaskCompletionEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("task_completions").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore completions permission restricted (using local Room database)")
                    } else {
                        Log.e(tag, "Listen failed for completions: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val completions = snapshot.documents.mapNotNull { doc ->
                        try {
                            TaskCompletionEntity(
                                id = doc.getString("id") ?: doc.id,
                                taskId = doc.getString("taskId") ?: "",
                                userId = doc.getString("userId") ?: "",
                                studentName = doc.getString("studentName") ?: "Student",
                                completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis(),
                                completionDate = doc.getString("completionDate") ?: "",
                                isCompleted = doc.getBoolean("isCompleted") ?: true,
                                note = doc.getString("note") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing completion doc: ${e.message}")
                            null
                        }
                    }
                    onCompletionsChanged(completions)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach completion listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // EXAMS
    // ==========================================

    suspend fun saveExam(exam: ExamEntity) {
        val db = firestore ?: return
        try {
            val examMap = hashMapOf(
                "id" to exam.id,
                "title" to exam.title,
                "description" to exam.description,
                "isPublished" to exam.isPublished,
                "createdAt" to exam.createdAt
            )
            db.collection("exams").document(exam.id).set(examMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Exam saved to Firestore: ${exam.title}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save exam to Firestore: ${e.message}")
        }
    }

    suspend fun deleteExam(examId: String) {
        val db = firestore ?: return
        try {
            db.collection("exams").document(examId).delete().awaitTask()
            Log.d(tag, "Exam deleted from Firestore: $examId")
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete exam from Firestore: ${e.message}", e)
        }
    }

    fun observeExams(onExamsChanged: (List<ExamEntity>, List<QuestionEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("exams").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore exams permission restricted (using local Room database)")
                    } else {
                        Log.e(tag, "Listen failed for exams: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val exams = mutableListOf<ExamEntity>()
                    val embeddedQuestions = mutableListOf<QuestionEntity>()

                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title")
                                ?: doc.getString("name")
                                ?: doc.getString("examTitle")
                                ?: doc.getString("exam_title")
                                ?: doc.getString("heading")
                                ?: "Exam"
                            val description = doc.getString("description")
                                ?: doc.getString("details")
                                ?: doc.getString("desc")
                                ?: doc.getString("subtitle")
                                ?: ""

                            val isPublished = when {
                                doc.contains("isPublished") -> doc.getBoolean("isPublished") ?: (doc.getString("isPublished")?.toBooleanStrictOrNull() ?: true)
                                doc.contains("published") -> doc.getBoolean("published") ?: (doc.getString("published")?.toBooleanStrictOrNull() ?: true)
                                doc.contains("is_published") -> doc.getBoolean("is_published") ?: (doc.getString("is_published")?.toBooleanStrictOrNull() ?: true)
                                doc.contains("status") -> doc.getString("status")?.equals("published", ignoreCase = true) ?: true
                                doc.contains("active") -> doc.getBoolean("active") ?: true
                                doc.contains("isActive") -> doc.getBoolean("isActive") ?: true
                                else -> true
                            }

                            val createdAt = doc.getLong("createdAt")
                                ?: doc.getLong("created_at")
                                ?: doc.getLong("timestamp")
                                ?: System.currentTimeMillis()

                            val exam = ExamEntity(
                                id = id,
                                title = title,
                                description = description,
                                isPublished = isPublished,
                                createdAt = createdAt
                            )
                            exams.add(exam)

                            // Check if questions are embedded as a list in the exam doc
                            val rawQuestions = doc.get("questions") as? List<*> ?: doc.get("questionList") as? List<*>
                            if (rawQuestions != null) {
                                for (item in rawQuestions) {
                                    if (item is Map<*, *>) {
                                        val q = parseQuestionMap(item, id)
                                        if (q != null) embeddedQuestions.add(q)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing exam doc ${doc.id}: ${e.message}")
                        }
                    }
                    Log.d(tag, "Firestore parsed ${exams.size} exams and ${embeddedQuestions.size} embedded questions")
                    onExamsChanged(exams, embeddedQuestions)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach exams listener: ${e.message}")
            null
        }
    }

    // ==========================================
    // QUESTIONS
    // ==========================================

    private fun parseQuestionMap(map: Map<*, *>, fallbackExamId: String = ""): QuestionEntity? {
        try {
            val qId = (map["id"] ?: map["_id"] ?: java.util.UUID.randomUUID().toString()).toString()
            val examId = (map["examId"] ?: map["exam_id"] ?: map["testId"] ?: fallbackExamId).toString()
            val text = (map["text"] ?: map["question"] ?: map["questionText"] ?: map["title"] ?: map["prompt"] ?: "").toString()
            if (text.isBlank()) return null

            var optA = ""
            var optB = ""
            var optC = ""
            var optD = ""

            val optionsList = map["options"] as? List<*>
            if (optionsList != null && optionsList.isNotEmpty()) {
                optA = optionsList.getOrNull(0)?.toString() ?: ""
                optB = optionsList.getOrNull(1)?.toString() ?: ""
                optC = optionsList.getOrNull(2)?.toString() ?: ""
                optD = optionsList.getOrNull(3)?.toString() ?: ""
            } else {
                optA = (map["optionA"] ?: map["option_a"] ?: map["optA"] ?: map["a"] ?: map["option1"] ?: "").toString()
                optB = (map["optionB"] ?: map["option_b"] ?: map["optB"] ?: map["b"] ?: map["option2"] ?: "").toString()
                optC = (map["optionC"] ?: map["option_c"] ?: map["optC"] ?: map["c"] ?: map["option3"] ?: "").toString()
                optD = (map["optionD"] ?: map["option_d"] ?: map["optD"] ?: map["d"] ?: map["option4"] ?: "").toString()
            }

            val rawCorrect = (map["correctOption"]
                ?: map["correct_option"]
                ?: map["answer"]
                ?: map["correctAnswer"]
                ?: map["correct"]
                ?: map["correctAnswerIndex"]
                ?: "A").toString().trim()

            val correctOption = when {
                rawCorrect.equals("0", ignoreCase = true) || rawCorrect.equals("A", ignoreCase = true) -> "A"
                rawCorrect.equals("1", ignoreCase = true) || rawCorrect.equals("B", ignoreCase = true) -> "B"
                rawCorrect.equals("2", ignoreCase = true) || rawCorrect.equals("C", ignoreCase = true) -> "C"
                rawCorrect.equals("3", ignoreCase = true) || rawCorrect.equals("D", ignoreCase = true) -> "D"
                optA.isNotBlank() && rawCorrect.equals(optA, ignoreCase = true) -> "A"
                optB.isNotBlank() && rawCorrect.equals(optB, ignoreCase = true) -> "B"
                optC.isNotBlank() && rawCorrect.equals(optC, ignoreCase = true) -> "C"
                optD.isNotBlank() && rawCorrect.equals(optD, ignoreCase = true) -> "D"
                rawCorrect.uppercase().startsWith("A") -> "A"
                rawCorrect.uppercase().startsWith("B") -> "B"
                rawCorrect.uppercase().startsWith("C") -> "C"
                rawCorrect.uppercase().startsWith("D") -> "D"
                else -> "A"
            }

            val timeLimit = (map["timeLimitSeconds"] ?: map["timeLimit"] ?: map["timer"] ?: map["duration"] ?: 30).toString().toIntOrNull() ?: 30

            val explanation = (
                map["explanation"]
                    ?: map["answerExplanation"]
                    ?: map["solution"]
                    ?: ""
            ).toString()

            return QuestionEntity(
                id = qId,
                examId = examId,
                text = text,
                optionA = optA,
                optionB = optB,
                optionC = optC,
                optionD = optD,
                correctOption = correctOption,
                timeLimitSeconds = timeLimit,
                explanation = explanation
            )
        } catch (e: Exception) {
            Log.e(tag, "Error parsing question map: ${e.message}")
            return null
        }
    }

    suspend fun saveQuestions(questions: List<QuestionEntity>) {
        val db = firestore ?: return
        try {
            for (q in questions) {
                val qMap = hashMapOf(
                    "id" to q.id,
                    "examId" to q.examId,
                    "text" to q.text,
                    "optionA" to q.optionA,
                    "optionB" to q.optionB,
                    "optionC" to q.optionC,
                    "optionD" to q.optionD,
                    "correctOption" to q.correctOption,
                    "timeLimitSeconds" to q.timeLimitSeconds
                )
                db.collection("questions").document(q.id).set(qMap, SetOptions.merge()).awaitTask()
            }
            Log.d(tag, "Saved ${questions.size} questions to Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save questions to Firestore: ${e.message}")
        }
    }

    fun observeQuestions(onQuestionsChanged: (List<QuestionEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("questions").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore questions permission restricted")
                    } else {
                        Log.e(tag, "Listen failed for questions: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val questions = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            parseQuestionMap(data, "")
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing question doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    Log.d(tag, "Firestore parsed ${questions.size} top-level questions")
                    onQuestionsChanged(questions)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach questions listener: ${e.message}")
            null
        }
    }

    suspend fun fetchQuestionsForExam(examId: String): List<QuestionEntity> {
        val db = firestore ?: return emptyList()
        val results = mutableListOf<QuestionEntity>()

        // 1. Check top-level collection "questions" with examId
        try {
            val qSnap = db.collection("questions").whereEqualTo("examId", examId).get().awaitTask()
            for (doc in qSnap.documents) {
                val data = doc.data ?: continue
                val q = parseQuestionMap(data, examId)
                if (q != null) results.add(q)
            }
        } catch (e: Exception) {
            Log.d(tag, "Query questions by examId: ${e.message}")
        }

        // 2. Check top-level collection "questions" with exam_id
        if (results.isEmpty()) {
            try {
                val qSnap = db.collection("questions").whereEqualTo("exam_id", examId).get().awaitTask()
                for (doc in qSnap.documents) {
                    val data = doc.data ?: continue
                    val q = parseQuestionMap(data, examId)
                    if (q != null) results.add(q)
                }
            } catch (e: Exception) {
                Log.d(tag, "Query questions by exam_id: ${e.message}")
            }
        }

        // 3. Check subcollection exams/{examId}/questions
        try {
            val subSnap = db.collection("exams").document(examId).collection("questions").get().awaitTask()
            for (doc in subSnap.documents) {
                val data = doc.data ?: continue
                val q = parseQuestionMap(data, examId)
                if (q != null) results.add(q)
            }
        } catch (e: Exception) {
            Log.d(tag, "Query subcollection exams/$examId/questions: ${e.message}")
        }

        // 4. Check exam document for embedded questions array
        if (results.isEmpty()) {
            try {
                val examDoc = db.collection("exams").document(examId).get().awaitTask()
                if (examDoc.exists()) {
                    val rawQuestions = examDoc.get("questions") as? List<*> ?: examDoc.get("questionList") as? List<*>
                    if (rawQuestions != null) {
                        for (item in rawQuestions) {
                            if (item is Map<*, *>) {
                                val q = parseQuestionMap(item, examId)
                                if (q != null) results.add(q)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(tag, "Query examDoc for embedded questions: ${e.message}")
            }
        }

        return results.distinctBy { it.id }
    }

    // ==========================================
    // EXAM ATTEMPTS
    // ==========================================

    suspend fun saveExamAttempt(attempt: ExamAttemptEntity, studentName: String) {
        val db = firestore ?: return
        try {
            val attemptMap = hashMapOf(
                "id" to attempt.id,
                "userId" to attempt.userId,
                "studentName" to studentName,
                "examId" to attempt.examId,
                "score" to attempt.score,
                "correctCount" to attempt.correctCount,
                "wrongCount" to attempt.wrongCount,
                "skippedCount" to attempt.skippedCount,
                "totalTimeSeconds" to attempt.totalTimeSeconds,
                "completedAt" to attempt.completedAt
            )
            db.collection("exam_attempts").document(attempt.id).set(attemptMap, SetOptions.merge()).awaitTask()
            Log.d(tag, "Exam attempt saved to Firestore: ${attempt.id}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to save exam attempt to Firestore: ${e.message}")
        }
    }

    fun observeExamAttempts(onAttemptsChanged: (List<ExamAttemptEntity>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("exam_attempts").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(tag, "Firestore exam_attempts permission restricted")
                    } else {
                        Log.e(tag, "Listen failed for exam_attempts: ${error.message}")
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val attempts = snapshot.documents.mapNotNull { doc ->
                        try {
                            ExamAttemptEntity(
                                id = doc.getString("id") ?: doc.id,
                                userId = doc.getString("userId") ?: doc.getString("user_id") ?: "",
                                examId = doc.getString("examId") ?: doc.getString("exam_id") ?: "",
                                score = doc.getLong("score")?.toInt() ?: 0,
                                correctCount = doc.getLong("correctCount")?.toInt() ?: doc.getLong("correct_count")?.toInt() ?: 0,
                                wrongCount = doc.getLong("wrongCount")?.toInt() ?: doc.getLong("wrong_count")?.toInt() ?: 0,
                                skippedCount = doc.getLong("skippedCount")?.toInt() ?: doc.getLong("skipped_count")?.toInt() ?: 0,
                                totalTimeSeconds = doc.getLong("totalTimeSeconds")?.toInt() ?: doc.getLong("total_time_seconds")?.toInt() ?: 0,
                                completedAt = doc.getLong("completedAt") ?: doc.getLong("completed_at") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "Error parsing exam attempt doc: ${e.message}")
                            null
                        }
                    }
                    onAttemptsChanged(attempts)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to attach exam attempts listener: ${e.message}")
            null
        }
    }

    suspend fun clearAllFirestoreData() {
        val db = firestore ?: return
        try {
            val tasks = db.collection("tasks").get().awaitTask()
            for (doc in tasks.documents) {
                db.collection("tasks").document(doc.id).delete().awaitTask()
            }
            val subjects = db.collection("subjects").get().awaitTask()
            for (doc in subjects.documents) {
                db.collection("subjects").document(doc.id).delete().awaitTask()
            }
            val topics = db.collection("topics").get().awaitTask()
            for (doc in topics.documents) {
                db.collection("topics").document(doc.id).delete().awaitTask()
            }
            val completions = db.collection("task_completions").get().awaitTask()
            for (doc in completions.documents) {
                db.collection("task_completions").document(doc.id).delete().awaitTask()
            }
            val exams = db.collection("exams").get().awaitTask()
            for (doc in exams.documents) {
                db.collection("exams").document(doc.id).delete().awaitTask()
            }
            val questions = db.collection("questions").get().awaitTask()
            for (doc in questions.documents) {
                db.collection("questions").document(doc.id).delete().awaitTask()
            }
            Log.d(tag, "Cleared all data from Firestore")
        } catch (e: Exception) {
            Log.w(tag, "Firestore clear restricted or skipped: ${e.message}")
        }
    }
}
