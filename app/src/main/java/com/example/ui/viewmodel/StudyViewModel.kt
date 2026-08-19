package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.AuthResultState
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirestoreSyncService
import com.example.data.model.RepeatSchedule
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectWithStats
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskWithDetails
import com.example.data.model.TopicEntity
import com.example.data.model.TopicWithStats
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.StudyRepository
import com.example.ui.components.CelebrationData
import com.example.ui.theme.ThemeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class NavigateToTaskDetail(val taskId: String) : UiEvent()
    object TaskCreated : UiEvent()
}

enum class StudentTab {
    TASKS,
    TOPICS,
    SUBJECTS,
    PROGRESS
}

enum class TaskFilter {
    ALL,
    TODAY,
    PENDING,
    COMPLETED
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val authService = FirebaseAuthService(application)
    private val firestoreSync = FirestoreSyncService(application)
    private val db = AppDatabase.getDatabase(application)
    private val repository = StudyRepository(db.studyDao(), firestoreSync)
    private val prefs = application.getSharedPreferences("study_diary_prefs", android.content.Context.MODE_PRIVATE)

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    private val defaultStudentUser = UserEntity(
        id = "student_bcs_default",
        name = "BCS Student",
        email = "student@bcs.com",
        role = UserRole.STUDENT,
        avatarColorHex = "#8B5CF6",
        streakDays = 0,
        lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    private val _currentUser = MutableStateFlow<UserEntity?>(
        authService.currentFirebaseUser?.let { firebaseUser ->
            UserEntity(
                id = firebaseUser.uid,
                name = firebaseUser.displayName?.ifBlank { null }
                    ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: "Student",
                email = firebaseUser.email.orEmpty(),
                role = UserRole.STUDENT,
                avatarColorHex = "#8B5CF6",
                streakDays = 0,
                lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
        }
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // --- Authentication & User State ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<UserEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start real-time Firestore sync & seed initial BCS data if database is empty
        viewModelScope.launch {
            repository.startFirestoreSync(viewModelScope)
            repository.seedInitialBcsDataIfEmpty()

            val activeUser = _currentUser.value ?: defaultStudentUser
            repository.insertUser(activeUser)
        }

        // Restore saved student session if available in SharedPreferences
        viewModelScope.launch {
            val savedUserId = prefs.getString("logged_in_user_id", null)
            if (!savedUserId.isNullOrBlank()) {
                repository.allUsers.collect { users ->
                    val savedUser = users.find { it.id == savedUserId }
                    if (savedUser != null) {
                        _currentUser.value = savedUser
                    }
                }
            }
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun setCurrentUser(user: UserEntity) {
        _currentUser.value = user
        prefs.edit().putString("logged_in_user_id", user.id).apply()
    }

    fun logout() {
        authService.signOut()
        _currentUser.value = null
        prefs.edit().remove("logged_in_user_id").apply()
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowToast("Signed out successfully"))
        }
    }

    /**
     * Firebase Google Sign-In with Credential Manager
     */
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            when (val result = authService.signInWithGoogle(activityContext)) {
                is AuthResultState.Success -> {
                    val user = result.user
                    repository.insertUser(user)
                    _currentUser.value = user
                    prefs.edit().putString("logged_in_user_id", user.id).apply()
                    _isAuthLoading.value = false
                    val msg = if (result.isNewUser) {
                        "Welcome to BCS Diary, ${user.name}! ✨"
                    } else {
                        "Welcome back, ${user.name}! 💕"
                    }
                    _eventFlow.emit(UiEvent.ShowToast(msg))
                }
                is AuthResultState.Error -> {
                    _isAuthLoading.value = false
                    _authErrorMessage.value = result.message
                    _eventFlow.emit(UiEvent.ShowToast(result.message))
                }
                is AuthResultState.Cancelled -> {
                    _isAuthLoading.value = false
                }
            }
        }
    }

    /**
     * Quick Demo Sign-In for testing without Google Credentials
     */
    fun signInWithDemoStudent() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val demoUser = UserEntity(
                id = "demo_student_bcs",
                name = "Demo Student",
                email = "demo.student@bcsdiary.app",
                role = UserRole.STUDENT,
                avatarColorHex = "#3B82F6",
                streakDays = 5,
                lastActiveDate = today
            )
            repository.insertUser(demoUser)
            _currentUser.value = demoUser
            prefs.edit().putString("logged_in_user_id", demoUser.id).apply()
            _isAuthLoading.value = false
            _eventFlow.emit(UiEvent.ShowToast("Signed in as Demo Student! ✨"))
        }
    }

    /**
     * Firebase Sign Up with Email and Password
     */
    fun signUpWithEmail(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            when (val result = authService.signUpWithEmail(name, email, password)) {
                is AuthResultState.Success -> {
                    val user = result.user
                    repository.insertUser(user)
                    _currentUser.value = user
                    prefs.edit().putString("logged_in_user_id", user.id).apply()
                    _isAuthLoading.value = false
                    _eventFlow.emit(UiEvent.ShowToast("Account Created! Welcome, ${user.name} ✨"))
                }
                is AuthResultState.Error -> {
                    _isAuthLoading.value = false
                    // If Firebase network is unreachable, fallback gracefully to offline Room creation
                    if (!authService.isFirebaseAvailable() || result.message.contains("network", ignoreCase = true)) {
                        registerOrLogin(name, email)
                    } else {
                        _authErrorMessage.value = result.message
                        _eventFlow.emit(UiEvent.ShowToast(result.message))
                    }
                }
                is AuthResultState.Cancelled -> {
                    _isAuthLoading.value = false
                }
            }
        }
    }

    /**
     * Firebase Sign In with Email and Password
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            when (val result = authService.signInWithEmail(email, password)) {
                is AuthResultState.Success -> {
                    val user = result.user
                    repository.insertUser(user)
                    _currentUser.value = user
                    prefs.edit().putString("logged_in_user_id", user.id).apply()
                    _isAuthLoading.value = false
                    _eventFlow.emit(UiEvent.ShowToast("Welcome back, ${user.name}! 💕"))
                }
                is AuthResultState.Error -> {
                    _isAuthLoading.value = false
                    // If offline or Firebase is unreachable, fallback to matching existing Room user
                    if (!authService.isFirebaseAvailable() || result.message.contains("network", ignoreCase = true)) {
                        registerOrLogin("Student", email)
                    } else {
                        _authErrorMessage.value = result.message
                        _eventFlow.emit(UiEvent.ShowToast(result.message))
                    }
                }
                is AuthResultState.Cancelled -> {
                    _isAuthLoading.value = false
                }
            }
        }
    }

    fun registerOrLogin(name: String, email: String) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val cleanName = name.trim().ifBlank { "Student" }

            val existing = allUsers.value.find { it.email.lowercase() == cleanEmail && cleanEmail.isNotEmpty() }
                ?: if (cleanEmail.isEmpty() || cleanEmail == "student@bcs.com") defaultStudentUser else null

            if (existing != null) {
                _currentUser.value = existing
                prefs.edit().putString("logged_in_user_id", existing.id).apply()
                _eventFlow.emit(UiEvent.ShowToast("Welcome back, ${existing.name}! 💕"))
            } else {
                val userIdHash = cleanEmail.hashCode().let { if (it < 0) -it else it }
                val newUser = UserEntity(
                    id = "student_$userIdHash",
                    name = cleanName,
                    email = cleanEmail,
                    role = UserRole.STUDENT,
                    avatarColorHex = "#8B5CF6",
                    streakDays = 1,
                    lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                prefs.edit().putString("logged_in_user_id", newUser.id).apply()
                _eventFlow.emit(UiEvent.ShowToast("Student Account Created! Welcome, ${newUser.name} ✨"))
            }
        }
    }

    // --- Filter & Search State ---
    private val _selectedThemeStyle = MutableStateFlow(ThemeStyle.LAVENDER_VIOLET)
    val selectedThemeStyle: StateFlow<ThemeStyle> = _selectedThemeStyle.asStateFlow()

    fun setThemeStyle(style: ThemeStyle) {
        _selectedThemeStyle.value = style
    }

    private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null = follow system, true/false = manual override
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        val current = _isDarkTheme.value
        _isDarkTheme.value = if (current == null) {
            true // default manual toggle to dark mode
        } else {
            !current
        }
    }

    fun setDarkTheme(enabled: Boolean?) {
        _isDarkTheme.value = enabled
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedSubjectFilter = MutableStateFlow<String?>(null)
    val selectedSubjectFilter: StateFlow<String?> = _selectedSubjectFilter.asStateFlow()

    fun setSubjectFilter(subjectId: String?) {
        _selectedSubjectFilter.value = subjectId
    }

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    private val _activeTab = MutableStateFlow(StudentTab.TASKS)
    val activeTab: StateFlow<StudentTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: StudentTab) {
        _activeTab.value = tab
    }

    // --- Data Streams linked to Current User ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksWithDetails: StateFlow<List<TaskWithDetails>> = _currentUser
        .flatMapLatest { user ->
            val userId = user?.id.orEmpty()
            repository.getTasksWithDetails(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val subjectsWithStats: StateFlow<List<SubjectWithStats>> = _currentUser
        .flatMapLatest { user ->
            val userId = user?.id.orEmpty()
            repository.getSubjectsWithStats(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topicsWithStats: StateFlow<List<TopicWithStats>> = _currentUser
        .flatMapLatest { user ->
            val userId = user?.id.orEmpty()
            repository.getTopicsWithStats(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletionLogs: StateFlow<List<TaskCompletionEntity>> = repository.allCompletionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Tasks for Student View
    val filteredTasks: StateFlow<List<TaskWithDetails>> = combine(
        tasksWithDetails,
        _searchQuery,
        _selectedSubjectFilter,
        _taskFilter
    ) { tasks, rawQuery, subjectFilter, filter ->
        val query = rawQuery.trim()
        val hasQuery = query.isNotEmpty()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        tasks.filter { item ->
            // Search text match
            val matchesQuery = !hasQuery ||
                    item.task.title.contains(query, ignoreCase = true) ||
                    item.task.description.contains(query, ignoreCase = true) ||
                    (item.subject?.name?.contains(query, ignoreCase = true) == true) ||
                    (item.topic?.name?.contains(query, ignoreCase = true) == true)

            // Subject filter match
            val matchesSubject = subjectFilter == null || item.task.subjectId == subjectFilter

            // Status / date filter match
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> {
                    item.task.dueDate.isEmpty() ||
                            item.task.dueDate <= todayStr ||
                            !item.isCompletedByCurrentUser ||
                            item.task.repeatSchedule != RepeatSchedule.NONE
                }
                TaskFilter.PENDING -> !item.isCompletedByCurrentUser
                TaskFilter.COMPLETED -> item.isCompletedByCurrentUser
            }

            matchesQuery && matchesSubject && matchesFilter
        }
    }.flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Real-Time Leaderboard Flow ---
    val leaderboardEntries: StateFlow<List<com.example.ui.components.LeaderboardEntry>> = combine(
        allUsers,
        repository.allTasks,
        allCompletionLogs
    ) { users: List<UserEntity>, tasks: List<com.example.data.model.TaskEntity>, completionLogs: List<TaskCompletionEntity> ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val totalTasksCount = tasks.size.coerceAtLeast(1)

        val completionsByUser = completionLogs.filter { it.isCompleted }.groupBy { it.userId }
        val todayCompletionsByUser = completionLogs.filter { it.isCompleted && it.completionDate == todayStr }.groupBy { it.userId }

        val entries = users.map { user ->
            val userCompletions = completionsByUser[user.id] ?: emptyList()
            val userTodayCompletions = todayCompletionsByUser[user.id] ?: emptyList()
            val completedCount = userCompletions.size
            val rate = ((completedCount.toFloat() / totalTasksCount) * 100).toInt().coerceIn(0, 100)

            com.example.ui.components.LeaderboardEntry(
                user = user,
                completedTasksCount = completedCount,
                todayCompletedCount = userTodayCompletions.size,
                streakDays = user.streakDays,
                completionRate = rate,
                rank = 0
            )
        }

        entries.sortedWith(
            compareByDescending<com.example.ui.components.LeaderboardEntry> { it.completedTasksCount }
                .thenByDescending { it.streakDays }
                .thenBy { it.user.name }
        ).mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }.flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Celebration & Confetti State ---
    private val _celebrationState = MutableStateFlow<CelebrationData?>(null)
    val celebrationState: StateFlow<CelebrationData?> = _celebrationState.asStateFlow()

    fun dismissCelebration() {
        _celebrationState.value = null
    }

    fun triggerManualCelebration(title: String = "Streak On Fire! 🔥", subtitle: String = "Awesome work keeping your study streak alive!") {
        val user = _currentUser.value
        val allTasksList = tasksWithDetails.value
        val completedCount = allTasksList.count { it.isCompletedByCurrentUser }
        _celebrationState.value = CelebrationData(
            title = title,
            subtitle = subtitle,
            streakDays = (user?.streakDays ?: 1).coerceAtLeast(1),
            completedCount = completedCount,
            totalCount = allTasksList.size,
            isStreakBonus = true
        )
    }

    // --- Task Actions ---
    fun toggleTaskCompletion(task: TaskWithDetails, note: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newCompletedState = !task.isCompletedByCurrentUser
            repository.toggleTaskCompletion(
                taskId = task.task.id,
                user = user,
                isCompleted = newCompletedState,
                note = note
            )

            if (newCompletedState) {
                val currentTasks = tasksWithDetails.value
                val updatedCompletedCount = currentTasks.count { it.isCompletedByCurrentUser || it.task.id == task.task.id }
                val totalTasksCount = currentTasks.size
                val isMultiple = updatedCompletedCount >= 2
                val isAllDone = totalTasksCount > 0 && updatedCompletedCount >= totalTasksCount

                val celebration = when {
                    isAllDone -> CelebrationData(
                        title = "All Daily Tasks Crushed! 🏆",
                        subtitle = "Perfect completion! You've mastered all study goals for today.",
                        streakDays = (user.streakDays + 1).coerceAtLeast(1),
                        completedCount = updatedCompletedCount,
                        totalCount = totalTasksCount,
                        isStreakBonus = true
                    )
                    isMultiple -> CelebrationData(
                        title = "Streak Multiplier! 🔥",
                        subtitle = "You completed multiple study tasks! Momentum is at an all-time high.",
                        streakDays = (user.streakDays + 1).coerceAtLeast(1),
                        completedCount = updatedCompletedCount,
                        totalCount = totalTasksCount,
                        isStreakBonus = true
                    )
                    else -> CelebrationData(
                        title = "Task Completed! 🎉",
                        subtitle = "Great start! Complete more tasks to supercharge your streak.",
                        streakDays = (user.streakDays + 1).coerceAtLeast(1),
                        completedCount = updatedCompletedCount,
                        totalCount = totalTasksCount,
                        isStreakBonus = false
                    )
                }

                _celebrationState.value = celebration
                _eventFlow.emit(UiEvent.ShowToast("Task Completed! 🔥 Streak updated"))
            } else {
                _eventFlow.emit(UiEvent.ShowToast("Task marked incomplete"))
            }
        }
    }

    fun resetProgress() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.resetUserProgress(user)
            _currentUser.value = user.copy(streakDays = 1)
            _eventFlow.emit(UiEvent.ShowToast("Study progress & streak reset to 0! 🔄"))
        }
    }

    // --- Admin Operations ---
    fun createTask(
        title: String,
        description: String,
        subjectId: String,
        topicId: String,
        dueDate: String,
        dueTime: String,
        repeatSchedule: RepeatSchedule,
        googleDriveUrl: String,
        googleDriveLabel: String,
        priority: TaskPriority
    ) {
        viewModelScope.launch {
            if (title.isBlank()) {
                _eventFlow.emit(UiEvent.ShowToast("Please enter a task title"))
                return@launch
            }
            if (subjectId.isBlank()) {
                _eventFlow.emit(UiEvent.ShowToast("Please select a subject"))
                return@launch
            }

            val newTask = TaskEntity(
                id = "task_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                description = description.trim(),
                subjectId = subjectId,
                topicId = topicId.ifBlank { "" },
                dueDate = dueDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
                dueTime = dueTime.ifBlank { "23:59" },
                repeatSchedule = repeatSchedule,
                googleDriveUrl = googleDriveUrl.trim(),
                googleDriveLabel = googleDriveLabel.ifBlank { "Study Material (Google Drive)" },
                priority = priority,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(newTask)
            _eventFlow.emit(UiEvent.ShowToast("New Task Created! Material Link Attached 📚"))
            _eventFlow.emit(UiEvent.TaskCreated)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _eventFlow.emit(UiEvent.ShowToast("Task deleted"))
        }
    }

    fun createSubject(name: String, code: String, colorHex: String, driveFolderUrl: String, description: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _eventFlow.emit(UiEvent.ShowToast("Please provide subject name"))
                return@launch
            }
            val newSubj = SubjectEntity(
                id = "subj_${UUID.randomUUID().toString().take(6)}",
                name = name.trim(),
                code = code.trim().ifBlank { "GEN101" },
                colorHex = colorHex.ifBlank { "#3B82F6" },
                iconName = "menu_book",
                driveFolderUrl = driveFolderUrl.trim(),
                description = description.trim()
            )
            repository.insertSubject(newSubj)
            _eventFlow.emit(UiEvent.ShowToast("Subject added: $name"))
        }
    }

    fun createTopic(subjectId: String, name: String, description: String, driveDocUrl: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _eventFlow.emit(UiEvent.ShowToast("Please provide topic name"))
                return@launch
            }
            val newTopic = TopicEntity(
                id = "topic_${UUID.randomUUID().toString().take(6)}",
                subjectId = subjectId,
                name = name.trim(),
                description = description.trim(),
                driveDocUrl = driveDocUrl.trim(),
                orderIndex = (allTopics.value.filter { it.subjectId == subjectId }.size + 1)
            )
            repository.insertTopic(newTopic)
            _eventFlow.emit(UiEvent.ShowToast("Topic added: $name"))
        }
    }

    fun clearAllDemoData() {
        viewModelScope.launch {
            repository.clearAllData()
            _eventFlow.emit(UiEvent.ShowToast("All tasks and subjects cleared! 🗑️"))
        }
    }
}
