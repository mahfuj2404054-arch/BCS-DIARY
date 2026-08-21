package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.FirestoreSyncService
import com.example.data.model.ExamAttemptEntity
import com.example.data.model.ExamEntity
import com.example.data.model.QuestionEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.data.model.ExamLeaderboardEntry
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth

enum class QuestionFeedback {
    NONE, CORRECT, WRONG, TIME_UP
}

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = StudyRepository(db.studyDao(), FirestoreSyncService(application))
    private val prefs = application.getSharedPreferences("study_diary_prefs", android.content.Context.MODE_PRIVATE)
    
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    val publishedExams: StateFlow<List<ExamEntity>> = repository.allPublishedExams
        .combine(repository.allExams) { published, all ->
            if (published.isNotEmpty()) published else all
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUserId = MutableStateFlow(
        prefs.getString("logged_in_user_id", null).orEmpty()
    )
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setCurrentUserId(userId: String?) {
        _currentUserId.value = userId.orEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userExamAttempts: StateFlow<List<ExamAttemptEntity>> =
        _currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repository.getExamAttemptsForUser(userId)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val pendingExams: StateFlow<List<ExamEntity>> = publishedExams.combine(userExamAttempts) { exams, attempts ->
        val completedExamIds = attempts.map { it.examId }.toSet()
        exams.filter { it.id !in completedExamIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedExams: StateFlow<List<ExamEntity>> = publishedExams.combine(userExamAttempts) { exams, attempts ->
        val completedExamIds = attempts.map { it.examId }.toSet()
        exams.filter { it.id in completedExamIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedExam = MutableStateFlow<ExamEntity?>(null)
    val selectedExam: StateFlow<ExamEntity?> = _selectedExam.asStateFlow()

    private val _examQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val examQuestions: StateFlow<List<QuestionEntity>> = _examQuestions.asStateFlow()

    private val _isLoadingQuestions = MutableStateFlow(false)
    val isLoadingQuestions: StateFlow<Boolean> = _isLoadingQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _feedback = MutableStateFlow(QuestionFeedback.NONE)
    val feedback: StateFlow<QuestionFeedback> = _feedback.asStateFlow()

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption.asStateFlow()

    private val _isExamFinished = MutableStateFlow(false)
    val isExamFinished: StateFlow<Boolean> = _isExamFinished.asStateFlow()

    private val _examReviewItems = MutableStateFlow<List<com.example.data.model.ExamReviewItem>>(emptyList())
    val examReviewItems: StateFlow<List<com.example.data.model.ExamReviewItem>> = _examReviewItems.asStateFlow()

    private var score = 0
    private var correctCount = 0
    private var wrongCount = 0
    private var skippedCount = 0
    private var totalTimeTaken = 0

    private var timerJob: Job? = null

    val examScore get() = score
    val examCorrectCount get() = correctCount
    val examWrongCount get() = wrongCount
    val examSkippedCount get() = skippedCount
    val examTotalTime get() = totalTimeTaken

    private val _endedEarly = MutableStateFlow(false)
    val endedEarly: StateFlow<Boolean> = _endedEarly.asStateFlow()

    private var attemptSaved = false

    private val _leaderboardExamId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val examLeaderboard: StateFlow<List<ExamLeaderboardEntry>> = _leaderboardExamId
        .flatMapLatest { examId ->
            if (examId == null) flowOf(emptyList())
            else {
                combine(
                    repository.getExamLeaderboard(examId),
                    repository.allUsers
                ) { attempts, users ->
                    // Group by user, keep best score
                    val bestAttempts = attempts.groupBy { it.userId }
                        .mapNotNull { (_, userAttempts) ->
                            userAttempts.minByOrNull { it.completedAt }
                        }

                    val userMap = users.associateBy { it.id }
                    bestAttempts.map { attempt ->
                        val user = userMap[attempt.userId]
                        ExamLeaderboardEntry(
                            userId = attempt.userId,
                            userName = user?.name ?: "Unknown Student",
                            avatarColorHex = user?.avatarColorHex ?: "#9E9E9E",
                            score = attempt.score,
                            timeSeconds = attempt.totalTimeSeconds,
                            completedAt = attempt.completedAt
                        )
                    }.sortedWith(compareByDescending<ExamLeaderboardEntry> { it.score }.thenBy { it.timeSeconds })
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLeaderboard(examId: String) {
        _leaderboardExamId.value = examId
    }

    init {
        // Start Firestore synchronization immediately for exams, questions, and attempts
        viewModelScope.launch {
            try {
                repository.startFirestoreSync(viewModelScope)
            } catch (e: Exception) {
                // Ignore sync errors on launch
            }
        }
        
        // Restart sync automatically when user signs in (auth state changes to non-null)
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) {
                viewModelScope.launch {
                    try {
                        repository.startFirestoreSync(viewModelScope)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            } else {
                repository.stopFirestoreSync()
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
        repository.stopFirestoreSync()
    }

    fun refreshExams() {
        viewModelScope.launch {
            try {
                repository.startFirestoreSync(viewModelScope)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun getQuestionCount(examId: String) = repository.getQuestionCountForExam(examId)

    fun selectAndStartExam(exam: ExamEntity, onReady: () -> Unit = {}) {
        _selectedExam.value = exam
        _isLoadingQuestions.value = true
        viewModelScope.launch {
            try {
                val questions = repository.getQuestionsForExamDirect(exam.id)
                if (questions.isNotEmpty()) {
                    _examQuestions.value = questions.shuffled()
                    startExam()
                    onReady()
                } else {
                    // Even if no questions yet, create fallback or start
                    _examQuestions.value = emptyList()
                    onReady()
                }
            } finally {
                _isLoadingQuestions.value = false
            }
        }
    }

    fun selectExam(exam: ExamEntity) {
        _selectedExam.value = exam
        viewModelScope.launch {
            _examQuestions.value = repository.getQuestionsForExamDirect(exam.id).shuffled()
        }
    }

    fun startExam() {
        attemptSaved = false
        _endedEarly.value = false
        score = 0
        correctCount = 0
        wrongCount = 0
        skippedCount = 0
        totalTimeTaken = 0
        _currentQuestionIndex.value = 0
        _isExamFinished.value = false
        _examReviewItems.value = emptyList()
        startQuestion()
    }

    private fun startQuestion() {
        _feedback.value = QuestionFeedback.NONE
        _selectedOption.value = null
        val question = _examQuestions.value.getOrNull(_currentQuestionIndex.value)
        if (question != null) {
            _timeRemaining.value = question.timeLimitSeconds
            startTimer()
        } else {
            finishExam()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
                totalTimeTaken += 1
            }
            if (_timeRemaining.value == 0 && _feedback.value == QuestionFeedback.NONE) {
                handleTimeUp()
            }
        }
    }

    private fun handleTimeUp() {
        _feedback.value = QuestionFeedback.TIME_UP
        skippedCount++
        
        val question = _examQuestions.value.getOrNull(_currentQuestionIndex.value)
        if (question != null) {
            _examReviewItems.value += com.example.data.model.ExamReviewItem(
                question = question,
                selectedOption = null,
                wasTimeUp = true
            )
        }
        
        timerJob?.cancel()
        viewModelScope.launch {
            delay(2000) // Show feedback for 2 seconds
            nextQuestion()
        }
    }

    fun submitAnswer(option: String) {
        if (_feedback.value != QuestionFeedback.NONE) return // Already answered

        timerJob?.cancel()
        _selectedOption.value = option
        val question = _examQuestions.value[_currentQuestionIndex.value]
        
        if (option == question.correctOption) {
            _feedback.value = QuestionFeedback.CORRECT
            correctCount++
            score += 10 // e.g., 10 points per correct answer
        } else {
            _feedback.value = QuestionFeedback.WRONG
            wrongCount++
        }

        _examReviewItems.value += com.example.data.model.ExamReviewItem(
            question = question,
            selectedOption = option,
            wasTimeUp = false
        )

        viewModelScope.launch {
            delay(2000) // Show feedback for 2 seconds
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        if (_currentQuestionIndex.value < _examQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
            startQuestion()
        } else {
            finishExam()
        }
    }

    fun quitExam() {
        finalizeExam(endedEarly = true)
    }

    private fun finishExam() {
        finalizeExam(endedEarly = false)
    }

    private fun finalizeExam(endedEarly: Boolean) {
        if (attemptSaved) return
        attemptSaved = true

        timerJob?.cancel()
        _endedEarly.value = endedEarly

        val unansweredQuestions =
            (_examQuestions.value.size - correctCount - wrongCount - skippedCount)
                .coerceAtLeast(0)

        skippedCount += unansweredQuestions
        _isExamFinished.value = true

        val userId = prefs.getString("logged_in_user_id", null)
        val examId = _selectedExam.value?.id

        if (userId != null && examId != null) {
            viewModelScope.launch {
                repository.insertExamAttempt(
                    ExamAttemptEntity(
                        userId = userId,
                        examId = examId,
                        score = score,
                        correctCount = correctCount,
                        wrongCount = wrongCount,
                        skippedCount = skippedCount,
                        totalTimeSeconds = totalTimeTaken,
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}