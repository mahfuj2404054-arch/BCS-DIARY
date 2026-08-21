package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TaskWithDetails
import com.example.ui.components.ConfettiCelebrationOverlay
import com.example.ui.components.CreateSubjectDialog
import com.example.ui.components.CreateTaskBottomSheet
import com.example.ui.components.CreateTopicDialog
import com.example.ui.components.LeaderboardDialog
import com.example.ui.components.NotificationPermissionPromptHandler
import com.example.ui.components.ThemeSelectionBottomSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ProgressDashboardScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TaskDetailScreen
import com.example.ui.screens.TopicsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudentTab
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TaskFilter
import com.example.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object StudentMain : Screen()
    data class TaskDetail(val taskId: String) : Screen()
    object ExamList : Screen()
    object ActiveExam : Screen()
    object ExamResult : Screen()
    object ExamReview : Screen()
    object ProgressAnalytics : Screen()
}

@Composable
fun StudyTaskApp(
    viewModel: StudyViewModel = viewModel(),
    examViewModel: com.example.ui.viewmodel.ExamViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val tasksWithDetails by viewModel.tasksWithDetails.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val subjectsWithStats by viewModel.subjectsWithStats.collectAsStateWithLifecycle()
    val topicsWithStats by viewModel.topicsWithStats.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allTopics by viewModel.allTopics.collectAsStateWithLifecycle()
    val allCompletionLogs by viewModel.allCompletionLogs.collectAsStateWithLifecycle()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isProfileLoading by viewModel.isProfileLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val celebrationState by viewModel.celebrationState.collectAsStateWithLifecycle()
    val isDarkThemeOverride by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val selectedThemeStyle by viewModel.selectedThemeStyle.collectAsStateWithLifecycle()
    val systemInDark = isSystemInDarkTheme()
    val effectiveDarkTheme = isDarkThemeOverride ?: systemInDark

    val pagerState = rememberPagerState(initialPage = activeTab.ordinal) { 4 }

    LaunchedEffect(currentUser?.id) {
        examViewModel.setCurrentUserId(currentUser?.id)
    }

    LaunchedEffect(activeTab) {
        if (pagerState.currentPage != activeTab.ordinal) {
            pagerState.scrollToPage(activeTab.ordinal)
        }
    }

    var currentScreen by remember { mutableStateOf<Screen>(if (currentUser != null) Screen.StudentMain else Screen.Login) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var showCreateSubjectDialog by remember { mutableStateOf(false) }
    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var createTopicInitialSubjectId by remember { mutableStateOf<String?>(null) }

    // Remembered callbacks to minimize recomposition overhead across screens
    val onSearchQueryChange = remember(viewModel) { { query: String -> viewModel.setSearchQuery(query) } }
    val onSubjectFilterChange = remember(viewModel) { { subjectId: String? -> viewModel.setSubjectFilter(subjectId) } }
    val onTaskFilterChange = remember(viewModel) { { filter: TaskFilter -> viewModel.setTaskFilter(filter) } }
    val onTaskClick = remember { { taskId: String -> currentScreen = Screen.TaskDetail(taskId) } }
    val onToggleComplete = remember(viewModel) { { taskWithDetails: TaskWithDetails -> viewModel.toggleTaskCompletion(taskWithDetails) } }
    val onSwitchAccount = remember(viewModel) { { viewModel.logout() } }
    val onToggleDarkTheme = remember(viewModel) { { viewModel.toggleDarkTheme() } }
    val onOpenThemePicker = remember { { showThemePicker = true } }
    val onOpenLeaderboard = remember { { showLeaderboardDialog = true } }
    val onSelectTopicFilter = remember(allTopics, viewModel) {
        { topicId: String ->
            val topic = allTopics.find { it.id == topicId }
            if (topic != null) {
                viewModel.setSearchQuery(topic.name)
                viewModel.setActiveTab(StudentTab.TASKS)
            }
        }
    }
    val onSelectSubject = remember(viewModel) {
        { subjectId: String ->
            viewModel.setSubjectFilter(subjectId)
            viewModel.setActiveTab(StudentTab.TASKS)
        }
    }
    val onSaveProfile = remember(viewModel) {
        { name: String, photoUri: String?, dateOfBirth: String?, bio: String?, schoolOrGrade: String?, avatarColorHex: String ->
            viewModel.updateUserProfile(name, photoUri, dateOfBirth, bio, schoolOrGrade, avatarColorHex)
        }
    }

    // Synchronize authentication state to route
    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user == null) {
            currentScreen = Screen.Login
        } else {
            if (currentScreen is Screen.Login) {
                currentScreen = Screen.StudentMain
            }
        }
    }

    // Post local notification alert for tasks due today on launch/task updates
    LaunchedEffect(currentUser, tasksWithDetails) {
        val user = currentUser
        if (user != null && tasksWithDetails.isNotEmpty()) {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val dueToday = tasksWithDetails.filter { it.task.dueDate == todayStr && !it.isCompletedByCurrentUser }
            if (dueToday.isNotEmpty()) {
                com.example.util.TaskNotificationHelper.sendDueTodayNotification(context, dueToday)
            }
        }
    }

    // Event listener for toast / snackbars
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.NavigateToTaskDetail -> {
                    currentScreen = Screen.TaskDetail(event.taskId)
                }
                is UiEvent.TaskCreated -> {
                    // Handled
                }
            }
        }
    }

    MyApplicationTheme(
        themeStyle = selectedThemeStyle,
        darkTheme = effectiveDarkTheme
    ) {
        val colors = AppTheme.colors

        // Request notification permission dialog on first launch after login
        NotificationPermissionPromptHandler(isUserLoggedIn = currentUser != null)

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                // Show Bottom Navbar only when on StudentMain screen
                if (currentScreen is Screen.StudentMain && currentUser != null) {
                    NavigationBar(
                        containerColor = colors.surfaceElevated,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        // Tab 1: Today's Tasks
                        NavigationBarItem(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                if (pagerState.currentPage != 0) {
                                    scope.launch { pagerState.scrollToPage(0) }
                                    viewModel.setActiveTab(StudentTab.TASKS)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (pagerState.currentPage == 0) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                                    contentDescription = "Tasks",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Tasks", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.pillBg
                            ),
                            modifier = Modifier.testTag("nav_tab_tasks")
                        )

                        // Tab 2: [Topics]
                        NavigationBarItem(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                if (pagerState.currentPage != 1) {
                                    scope.launch { pagerState.scrollToPage(1) }
                                    viewModel.setActiveTab(StudentTab.TOPICS)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (pagerState.currentPage == 1) Icons.Filled.Topic else Icons.Outlined.Topic,
                                    contentDescription = "Topics",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Topics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.pillBg
                            ),
                            modifier = Modifier.testTag("nav_tab_topics")
                        )

                        // Tab 3: [Subjects]
                        NavigationBarItem(
                            selected = pagerState.currentPage == 2,
                            onClick = {
                                if (pagerState.currentPage != 2) {
                                    scope.launch { pagerState.scrollToPage(2) }
                                    viewModel.setActiveTab(StudentTab.SUBJECTS)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (pagerState.currentPage == 2) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = "Subjects",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Subjects", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.pillBg
                            ),
                            modifier = Modifier.testTag("nav_tab_subjects")
                        )

                        // Tab 4: [Exams]
                        NavigationBarItem(
                            selected = pagerState.currentPage == 3,
                            onClick = {
                                if (pagerState.currentPage != 3) {
                                    scope.launch { pagerState.scrollToPage(3) }
                                    viewModel.setActiveTab(StudentTab.EXAMS)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (pagerState.currentPage == 3) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                    contentDescription = "Exams",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Exams", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.pillBg
                            ),
                            modifier = Modifier.testTag("nav_tab_exams")
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isProfileLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colors.background),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = colors.primary
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screenTransition"
                    ) { screen ->
                    when (screen) {
                        is Screen.Login -> {
                            AuthScreen(
                                onGoogleSignIn = {
                                    viewModel.signInWithGoogle(context)
                                },
                                isLoading = isAuthLoading,
                                errorMessage = authErrorMessage,
                                onClearError = {
                                    viewModel.clearAuthError()
                                }
                            )
                        }

                        is Screen.StudentMain -> {
                            val user = currentUser
                            if (user != null) {
                                HorizontalPager(
                                    state = pagerState,
                                    userScrollEnabled = false,
                                    beyondViewportPageCount = 3,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    when (page) {
                                        0 -> {
                                            StudentDashboardScreen(
                                                currentUser = user,
                                                tasks = filteredTasks,
                                                allTasks = tasksWithDetails,
                                                allSubjects = allSubjects,
                                                searchQuery = searchQuery,
                                                selectedSubjectFilter = selectedSubjectFilter,
                                                taskFilter = taskFilter,
                                                onSearchQueryChange = onSearchQueryChange,
                                                onSubjectFilterChange = onSubjectFilterChange,
                                                onTaskFilterChange = onTaskFilterChange,
                                                onTaskClick = onTaskClick,
                                                onToggleComplete = onToggleComplete,
                                                onSwitchAccount = onSwitchAccount,
                                                isDarkTheme = effectiveDarkTheme,
                                                onToggleDarkTheme = onToggleDarkTheme,
                                                onOpenThemePicker = onOpenThemePicker,
                                                onOpenLeaderboard = onOpenLeaderboard,
                                                onOpenProgress = { currentScreen = Screen.ProgressAnalytics },
                                                onOpenExams = { currentScreen = Screen.ExamList },
                                                onSaveProfile = onSaveProfile
                                            )
                                        }

                                        1 -> {
                                            TopicsScreen(
                                                topicsWithStats = topicsWithStats,
                                                onSelectTopicFilter = onSelectTopicFilter
                                            )
                                        }

                                        2 -> {
                                            SubjectsScreen(
                                                subjectsWithStats = subjectsWithStats,
                                                onSelectSubject = onSelectSubject
                                            )
                                        }

                                        3 -> {
                                            com.example.ui.screens.ExamListScreen(
                                                examViewModel = examViewModel,
                                                onNavigateBack = { },
                                                onStartExam = {
                                                    currentScreen = Screen.ActiveExam
                                                },
                                                isTab = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is Screen.TaskDetail -> {
                            val task = tasksWithDetails.find { it.task.id == screen.taskId }
                            TaskDetailScreen(
                                taskWithDetails = task,
                                onBack = {
                                    currentScreen = Screen.StudentMain
                                },
                                onToggleComplete = { note ->
                                    if (task != null) {
                                        viewModel.toggleTaskCompletion(task, note)
                                    }
                                }
                            )
                        }

                        is Screen.ProgressAnalytics -> {
                            val user = currentUser
                            if (user != null) {
                                ProgressDashboardScreen(
                                    currentUser = user,
                                    tasksWithDetails = tasksWithDetails,
                                    subjectsWithStats = subjectsWithStats,
                                    userCompletions = allCompletionLogs,
                                    isDarkTheme = effectiveDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme,
                                    onBack = { currentScreen = Screen.StudentMain }
                                )
                            }
                        }

                        is Screen.ExamList -> {
                            com.example.ui.screens.ExamListScreen(
                                examViewModel = examViewModel,
                                onNavigateBack = {
                                    currentScreen = Screen.StudentMain
                                },
                                onStartExam = {
                                    currentScreen = Screen.ActiveExam
                                }
                            )
                        }

                        is Screen.ActiveExam -> {
                            com.example.ui.screens.ActiveExamScreen(
                                examViewModel = examViewModel,
                                onExamFinished = {
                                    currentScreen = Screen.ExamResult
                                },
                                onExamQuit = {
                                    currentScreen = Screen.ExamList
                                }
                            )
                        }

                        is Screen.ExamResult -> {
                            com.example.ui.screens.ExamResultScreen(
                                examViewModel = examViewModel,
                                onFinish = {
                                    currentScreen = Screen.StudentMain
                                },
                                onReviewAnswers = {
                                    currentScreen = Screen.ExamReview
                                }
                            )
                        }
                        
                        is Screen.ExamReview -> {
                            com.example.ui.screens.ExamReviewScreen(
                                examViewModel = examViewModel,
                                onBackToResults = {
                                    currentScreen = Screen.ExamResult
                                }
                            )
                        }
                    }
                }
                } // End of if (isProfileLoading) else

                // Confetti & Streak Milestone Celebration Overlay
                ConfettiCelebrationOverlay(
                    celebrationData = celebrationState,
                    onDismiss = { viewModel.dismissCelebration() }
                )

                // Leaderboard Dialog
                if (showLeaderboardDialog) {
                    LeaderboardDialog(
                        entries = leaderboardEntries,
                        currentUser = currentUser,
                        onDismiss = { showLeaderboardDialog = false }
                    )
                }

                // Theme Selection Bottom Sheet
                if (showThemePicker) {
                    ThemeSelectionBottomSheet(
                        selectedTheme = selectedThemeStyle,
                        isDarkTheme = effectiveDarkTheme,
                        onSelectTheme = { newTheme ->
                            viewModel.setThemeStyle(newTheme)
                        },
                        onToggleDarkTheme = {
                            viewModel.toggleDarkTheme()
                        },
                        onDismiss = { showThemePicker = false }
                    )
                }

                // Create Task Bottom Sheet
                if (showCreateTaskSheet) {
                    CreateTaskBottomSheet(
                        subjects = allSubjects,
                        topics = allTopics,
                        onDismiss = { showCreateTaskSheet = false },
                        onOpenCreateSubject = {
                            showCreateTaskSheet = false
                            showCreateSubjectDialog = true
                        },
                        onCreateTask = { title, desc, subjectId, topicId, dueDate, dueTime, repeatSchedule, driveUrl, driveLabel, priority ->
                            viewModel.createTask(
                                title = title,
                                description = desc,
                                subjectId = subjectId,
                                topicId = topicId,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                repeatSchedule = repeatSchedule,
                                googleDriveUrl = driveUrl,
                                googleDriveLabel = driveLabel,
                                priority = priority
                            )
                        }
                    )
                }

                // Create Subject Dialog
                if (showCreateSubjectDialog) {
                    CreateSubjectDialog(
                        onDismiss = { showCreateSubjectDialog = false },
                        onCreateSubject = { name, code, colorHex, driveFolderUrl, description ->
                            viewModel.createSubject(
                                name = name,
                                code = code,
                                colorHex = colorHex,
                                driveFolderUrl = driveFolderUrl,
                                description = description
                            )
                        }
                    )
                }

                // Create Topic Dialog
                if (showCreateTopicDialog) {
                    CreateTopicDialog(
                        subjects = allSubjects,
                        initialSubjectId = createTopicInitialSubjectId,
                        onDismiss = { showCreateTopicDialog = false },
                        onCreateTopic = { subjectId, name, description, driveDocUrl ->
                            viewModel.createTopic(
                                subjectId = subjectId,
                                name = name,
                                description = description,
                                driveDocUrl = driveDocUrl
                            )
                        }
                    )
                }
            }
        }
    }
}
