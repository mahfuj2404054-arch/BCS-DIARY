package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskWithDetails
import com.example.ui.theme.AppTheme
import com.example.util.TaskNotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DueTodayAlertCard(
    allTasks: List<TaskWithDetails>,
    onFilterToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Identify tasks due today
    val dueTodayTasks = remember(allTasks, todayDateStr) {
        allTasks.filter { it.task.dueDate == todayDateStr }
    }

    val pendingDueToday = remember(dueTodayTasks) {
        dueTodayTasks.filter { !it.isCompletedByCurrentUser }
    }

    val completedDueToday = remember(dueTodayTasks) {
        dueTodayTasks.filter { it.isCompletedByCurrentUser }
    }

    var isDismissed by remember { mutableStateOf(false) }

    // If dismissed or no tasks scheduled for today, don't show full banner unless there are tasks
    if (dueTodayTasks.isEmpty() || isDismissed) {
        return
    }

    val isAllCompleted = pendingDueToday.isEmpty() && completedDueToday.isNotEmpty()

    val cardBgGradient = if (isAllCompleted) {
        Brush.linearGradient(
            listOf(
                Color(0xFF10B981).copy(alpha = if (colors.isDark) 0.25f else 0.12f),
                Color(0xFF059669).copy(alpha = if (colors.isDark) 0.20f else 0.08f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.primary.copy(alpha = if (colors.isDark) 0.28f else 0.15f),
                Color(0xFFF59E0B).copy(alpha = if (colors.isDark) 0.22f else 0.12f)
            )
        )
    }

    val borderBrush = if (isAllCompleted) {
        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
    } else {
        Brush.linearGradient(listOf(colors.primary, Color(0xFFF59E0B)))
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = borderBrush),
        modifier = modifier
            .fillMaxWidth()
            .testTag("due_today_alert_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgGradient)
                .padding(18.dp)
        ) {
            Column {
                // Top Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isAllCompleted) Color(0xFF10B981) else colors.primary)
                        ) {
                            Icon(
                                imageVector = if (isAllCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = "Due Today",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAllCompleted) "Tasks Completed Today! 🎉" else "⏰ Tasks Due Today",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAllCompleted) {
                                    "Awesome! You completed all ${completedDueToday.size} tasks due today."
                                } else {
                                    "${pendingDueToday.size} pending • ${completedDueToday.size} done"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Task Items Preview List (Up to 3 items)
                if (pendingDueToday.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pendingDueToday.take(3).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface.copy(alpha = 0.7f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(colors.borderSubtle, colors.border)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (item.subject != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SubjectBadge(
                                            subjectName = item.subject.name,
                                            colorHex = item.subject.colorHex
                                        )
                                    }
                                }
                            }
                        }

                        if (pendingDueToday.size > 3) {
                            Text(
                                text = "+ ${pendingDueToday.size - 3} more task(s) scheduled for today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }

                // Action Row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Filter Today Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isAllCompleted) Color(0xFF10B981) else colors.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onFilterToday() }
                            .testTag("btn_alert_view_today_tasks")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "🎯 View Today's Tasks",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Dismiss Button
                    Text(
                        text = "Dismiss Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textTertiary,
                        modifier = Modifier
                            .clickable { isDismissed = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("btn_dismiss_today_alert")
                    )
                }
            }
        }
    }
}
