package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ExamEntity
import com.example.data.model.QuestionEntity
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.ExamViewModel
import com.example.ui.viewmodel.QuestionFeedback

import com.example.ui.components.ExamLeaderboardDialog

@Composable
fun ExamListScreen(
    examViewModel: ExamViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onStartExam: () -> Unit,
    isTab: Boolean = false
) {
    val pendingExams by examViewModel.pendingExams.collectAsStateWithLifecycle()
    val completedExams by examViewModel.completedExams.collectAsStateWithLifecycle()
    val isLoadingQuestions by examViewModel.isLoadingQuestions.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val displayExams = if (selectedTabIndex == 0) pendingExams else completedExams

    var showLeaderboardForExam by remember { mutableStateOf<ExamEntity?>(null) }
    
    if (showLeaderboardForExam != null) {
        val leaderboardEntries by examViewModel.examLeaderboard.collectAsStateWithLifecycle()
        ExamLeaderboardDialog(
            examTitle = showLeaderboardForExam!!.title,
            entries = leaderboardEntries,
            onDismiss = { showLeaderboardForExam = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 20.dp)
        ) {
            if (!isTab) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = "Exams",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { examViewModel.refreshExams() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh exams",
                    tint = colors.primary
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = colors.background,
            contentColor = colors.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Pending (${pendingExams.size})") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Completed (${completedExams.size})") }
            )
        }

        if (isLoadingQuestions) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.primary)
                    Text("Loading exam questions...", color = colors.textSecondary, fontSize = 13.sp)
                }
            }
        }

        if (displayExams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "No pending exams at the moment." else "No completed exams yet.",
                        color = colors.textSecondary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    if (selectedTabIndex == 0) {
                        Button(
                            onClick = { examViewModel.refreshExams() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Published Exams")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayExams) { exam ->
                    ExamCard(
                        exam = exam,
                        onClick = {
                            examViewModel.selectAndStartExam(exam) {
                                onStartExam()
                            }
                        },
                        onLeaderboardClick = {
                            examViewModel.loadLeaderboard(exam.id)
                            showLeaderboardForExam = exam
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCard(exam: ExamEntity, onClick: () -> Unit, onLeaderboardClick: () -> Unit = {}) {
    val colors = AppTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Live Exam",
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (exam.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exam.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onLeaderboardClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                ) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Leaderboard",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(
                        text = "Start Exam",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActiveExamScreen(
    examViewModel: ExamViewModel = viewModel(),
    onExamFinished: () -> Unit,
    onExamQuit: () -> Unit = {}
) {
    val questions by examViewModel.examQuestions.collectAsStateWithLifecycle()
    val currentIndex by examViewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val timeRemaining by examViewModel.timeRemaining.collectAsStateWithLifecycle()
    val feedback by examViewModel.feedback.collectAsStateWithLifecycle()
    val selectedOption by examViewModel.selectedOption.collectAsStateWithLifecycle()
    val isFinished by examViewModel.isExamFinished.collectAsStateWithLifecycle()

    val colors = AppTheme.colors
    var showQuitDialog by remember { mutableStateOf(false) }

    // Prevent accidental back navigation during exam
    BackHandler {
        showQuitDialog = true
    }

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onExamFinished()
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit Exam?", color = colors.textPrimary) },
            text = { Text("Are you sure you want to quit? Your progress will be lost and this will not be counted on the leaderboard.", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    examViewModel.quitExam()
                    onExamQuit()
                }) {
                    Text("Quit", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Cancel", color = colors.primary)
                }
            },
            containerColor = colors.surface
        )
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    val currentQuestion = questions.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Header: Progress & Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showQuitDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Quit", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Question ${currentIndex + 1} / ${questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                )
            }
            
            // Timer Badge
            val timerColor = if (timeRemaining <= 5) Color(0xFFEF4444) else colors.primary
            Row(
                modifier = Modifier
                    .background(timerColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time",
                    tint = timerColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${timeRemaining}s",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                )
            }
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1) / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colors.primary,
            trackColor = colors.surfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Question Area (Animated)
        AnimatedContent(
            targetState = currentQuestion,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400)) with slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(400)
                ) + fadeOut(tween(400))
            },
            label = "QuestionAnimation",
            modifier = Modifier.weight(1f)
        ) { question ->
            if (question != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            lineHeight = 36.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    val options = listOf(
                        "A" to question.optionA,
                        "B" to question.optionB,
                        "C" to question.optionC,
                        "D" to question.optionD
                    )
                    
                    val optionColors = listOf(
                        Color(0xFFE32929), // Kahoot Red
                        Color(0xFF1368CE), // Kahoot Blue
                        Color(0xFFD89E00), // Kahoot Yellow
                        Color(0xFF26890C)  // Kahoot Green
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        options.forEachIndexed { index, pair ->
                            val (key, text) = pair
                            val isSelected = selectedOption == key
                            val isCorrect = question.correctOption == key
                            
                            val cardColor = when {
                                feedback == QuestionFeedback.NONE -> optionColors[index]
                                feedback == QuestionFeedback.TIME_UP -> if (isCorrect) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f)
                                isSelected && feedback == QuestionFeedback.CORRECT -> Color(0xFF10B981)
                                isSelected && feedback == QuestionFeedback.WRONG -> Color(0xFFEF4444)
                                isCorrect -> Color(0xFF10B981)
                                else -> Color.Gray.copy(alpha = 0.5f)
                            }
                            
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clickable(enabled = feedback == QuestionFeedback.NONE) {
                                        examViewModel.submitAnswer(key)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = key,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    if (feedback != QuestionFeedback.NONE) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Correct",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Wrong",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamResultScreen(
    examViewModel: ExamViewModel = viewModel(),
    onFinish: () -> Unit
) {
    val score = examViewModel.examScore
    val correct = examViewModel.examCorrectCount
    val wrong = examViewModel.examWrongCount
    val skipped = examViewModel.examSkippedCount
    val totalTime = examViewModel.examTotalTime
    val questions = examViewModel.examQuestions.collectAsStateWithLifecycle().value
    val totalQuestions = questions.size
    val selectedExam = examViewModel.selectedExam.collectAsStateWithLifecycle().value
    
    val accuracy = if (totalQuestions > 0) (correct.toFloat() / totalQuestions * 100).toInt() else 0

    val colors = AppTheme.colors

    var showLeaderboard by remember { mutableStateOf(false) }

    if (showLeaderboard && selectedExam != null) {
        val leaderboardEntries by examViewModel.examLeaderboard.collectAsStateWithLifecycle()
        ExamLeaderboardDialog(
            examTitle = selectedExam.title,
            entries = leaderboardEntries,
            onDismiss = { showLeaderboard = false }
        )
    }

    BackHandler {
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Exam Complete!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(colors.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                )
                Text(
                    text = "Points",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.primary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Correct", "$correct", Color(0xFF10B981))
            StatItem("Wrong", "$wrong", Color(0xFFEF4444))
            StatItem("Skipped", "$skipped", Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Accuracy", "$accuracy%", colors.primary)
            StatItem("Time Taken", "${totalTime}s", colors.secondary)
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (selectedExam != null) {
                        examViewModel.loadLeaderboard(selectedExam.id)
                        showLeaderboard = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
            ) {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Leaderboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(
                    text = "Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textSecondary
            )
        )
    }
}
