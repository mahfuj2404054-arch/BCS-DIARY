package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.ExamViewModel
import com.example.data.model.ExamReviewItem

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamReviewScreen(
    examViewModel: ExamViewModel = viewModel(),
    onBackToResults: () -> Unit
) {
    val reviewItems by examViewModel.examReviewItems.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    
    var currentIndex by remember { mutableIntStateOf(0) }
    
    val correct = examViewModel.examCorrectCount
    val wrong = examViewModel.examWrongCount
    val skipped = examViewModel.examSkippedCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Review", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackToResults) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Overview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Correct", value = "$correct", color = Color(0xFF10B981))
                StatItem(label = "Wrong", value = "$wrong", color = Color(0xFFEF4444))
                StatItem(label = "Skipped", value = "$skipped", color = Color.Gray)
            }
            
            Divider(color = colors.border, thickness = 1.dp)

            if (reviewItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                        ReviewCard(index = currentIndex + 1, item = reviewItems[currentIndex])
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Previous")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = { if (currentIndex < reviewItems.size - 1) currentIndex++ },
                            enabled = currentIndex < reviewItems.size - 1,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(index: Int, item: ExamReviewItem) {
    val colors = AppTheme.colors
    val question = item.question
    
    val isCorrect = item.selectedOption == question.correctOption
    val isSkipped = item.selectedOption == null
    
    val statusColor = when {
        isSkipped -> Color.Gray
        isCorrect -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }
    
    val statusText = when {
        isSkipped -> "Skipped"
        isCorrect -> "Correct"
        else -> "Wrong"
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $index",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = statusText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Options
            val options = listOf("A" to question.optionA, "B" to question.optionB, "C" to question.optionC, "D" to question.optionD)
            options.forEach { (key, text) ->
                val isCorrectAnswer = key == question.correctOption
                val isSelectedAnswer = key == item.selectedOption
                
                val bgColor = when {
                    isCorrectAnswer -> Color(0xFF10B981).copy(alpha = 0.15f)
                    isSelectedAnswer && !isCorrectAnswer -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    else -> colors.background
                }
                
                val borderColor = when {
                    isCorrectAnswer -> Color(0xFF10B981)
                    isSelectedAnswer && !isCorrectAnswer -> Color(0xFFEF4444)
                    else -> colors.border
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$key.",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }
            }
            
            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Explanation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
