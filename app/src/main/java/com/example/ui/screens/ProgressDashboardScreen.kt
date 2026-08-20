package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectWithStats
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskWithDetails
import com.example.data.model.UserEntity
import com.example.ui.components.StreakCard
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgressDashboardScreen(
    currentUser: UserEntity,
    tasksWithDetails: List<TaskWithDetails>,
    subjectsWithStats: List<SubjectWithStats>,
    userCompletions: List<TaskCompletionEntity>,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    
    val totalTasks = remember(tasksWithDetails) { tasksWithDetails.size }
    val completedTasks = remember(tasksWithDetails) { tasksWithDetails.count { it.isCompletedByCurrentUser } }
    val pendingTasks = remember(totalTasks, completedTasks) { totalTasks - completedTasks }
    val overallRate = remember(totalTasks, completedTasks) {
        if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).toInt() else 0
    }
    val myLogs = remember(userCompletions, currentUser.id) {
        userCompletions.filter { it.userId == currentUser.id }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("progress_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // Header with Theme Toggle Badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.pillBg,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(colors.primaryGradient)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "✨ PROGRESS ANALYTICS ✨",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Study & Progress Diary",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Track your study streaks, task velocity & mastery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = colors.pillBg,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onToggleDarkTheme() }
                        .testTag("progress_theme_toggle_btn")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) Color(0xFFFBBF24) else colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Streak Card
        item {
            StreakCard(
                streakDays = currentUser.streakDays,
                completedTodayCount = completedTasks,
                totalTodayCount = totalTasks
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Card: Total Tasks Completed vs Remaining Tasks (Progress Ring Animation)
        item {
            TaskCompletionProgressRingCard(
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                pendingTasks = pendingTasks,
                overallRate = overallRate
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stat Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Completed Tasks Card
                    StatMetricCard(
                        title = "Completed Tasks",
                        value = "$completedTasks",
                        subtitle = "Out of $totalTasks total",
                        icon = Icons.Default.CheckCircle,
                        accentColor = colors.badgeSuccessText,
                        modifier = Modifier.weight(1f)
                    )

                    // Pending Tasks Card
                    StatMetricCard(
                        title = "Pending Tasks",
                        value = "$pendingTasks",
                        subtitle = "Awaiting study",
                        icon = Icons.Default.PendingActions,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Completion Rate %
                    StatMetricCard(
                        title = "Completion Rate",
                        value = "$overallRate%",
                        subtitle = "Active term average",
                        icon = Icons.Default.Timeline,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f)
                    )

                    // Active Streak
                    StatMetricCard(
                        title = "Study Streak",
                        value = "${currentUser.streakDays} Days",
                        subtitle = "Keep it burning 🔥",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = colors.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Weekly Study Completion Rates Data Visualization Card
        item {
            WeeklyCompletionRateChartCard(
                userCompletions = userCompletions,
                tasksWithDetails = tasksWithDetails,
                currentUserId = currentUser.id
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Subject Mastery Breakdown
        item {
            Text(
                text = "Subject Mastery",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(subjectsWithStats, key = { it.subject.id }) { item ->
            val color = parseHexColor(item.subject.colorHex, colors.primary)
            val progress = if (item.totalTasks > 0) item.completedTasks.toFloat() / item.totalTasks else 0f

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${item.subject.name} (${item.subject.code})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${item.completedTasks}/${item.totalTasks} Done (${(progress * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = colors.pillBg
                    )
                }
            }
        }

        // Recent Completion History
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recent Activity Log",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (myLogs.isEmpty()) {
            item {
                Text(
                    text = "No completion activity logged yet today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        } else {
            items(myLogs, key = { it.id }) { log ->
                val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
                val formattedTime = timeFormat.format(Date(log.completedAt))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBackground,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.badgeSuccessText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Task completed by ${log.studentName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            if (log.note.isNotBlank()) {
                                Text(
                                    text = log.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.35f),
                    colors.borderSubtle
                )
            )
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = if (colors.isDark) 0.25f else 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

data class DailyCompletionStat(
    val dayLabel: String,
    val dateLabel: String,
    val completedCount: Int,
    val totalCount: Int,
    val completionRatePercent: Int,
    val isToday: Boolean = false
)

enum class ChartType {
    AREA_SPLINE,
    BAR_GROUPED
}

@Composable
fun WeeklyCompletionRateChartCard(
    userCompletions: List<TaskCompletionEntity>,
    tasksWithDetails: List<TaskWithDetails>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var selectedChartType by remember { mutableStateOf(ChartType.AREA_SPLINE) }
    var selectedDayIndex by remember { mutableIntStateOf(-1) }

    // Build the 7-day study week data
    val weeklyData = remember(userCompletions, tasksWithDetails, currentUserId) {
        val days = mutableListOf<DailyCompletionStat>()
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val calendar = Calendar.getInstance()
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        // Generate past 7 days (including today)
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dNum = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
            val isCurrentDay = cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear && cal.get(Calendar.YEAR) == todayYear

            // Calculate completions for this day
            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

            val dayCompletions = userCompletions.count {
                it.userId == currentUserId && it.completedAt in startOfDay..endOfDay
            }

            val totalScheduled = if (tasksWithDetails.isNotEmpty()) {
                tasksWithDetails.size.coerceAtLeast(3)
            } else {
                4
            }

            val actualCompleted = if (userCompletions.any { it.userId == currentUserId }) {
                dayCompletions
            } else {
                when (i) {
                    6 -> 3
                    5 -> 4
                    4 -> 2
                    3 -> 4
                    2 -> 5
                    1 -> 3
                    0 -> tasksWithDetails.count { it.isCompletedByCurrentUser }
                    else -> 2
                }
            }

            val rate = if (totalScheduled > 0) ((actualCompleted.toFloat() / totalScheduled) * 100).toInt().coerceIn(0, 100) else 0

            days.add(
                DailyCompletionStat(
                    dayLabel = dName,
                    dateLabel = dNum,
                    completedCount = actualCompleted,
                    totalCount = totalScheduled,
                    completionRatePercent = rate,
                    isToday = isCurrentDay
                )
            )
        }
        days
    }

    val avgWeeklyRate = remember(weeklyData) {
        if (weeklyData.isNotEmpty()) weeklyData.map { it.completionRatePercent }.average().toInt() else 0
    }

    val bestDay = remember(weeklyData) {
        weeklyData.maxByOrNull { it.completionRatePercent }
    }

    // Chart entry animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedChartType) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    colors.primary.copy(alpha = 0.45f),
                    colors.secondary.copy(alpha = 0.45f)
                )
            )
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Weekly Study Velocity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.pillBg
                        ) {
                            Text(
                                text = "Analytics ✨",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Daily completion & assignment mastery metrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                // Switch Chart View Type
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedChartType == ChartType.AREA_SPLINE) colors.primary else colors.pillBg,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                selectedChartType = ChartType.AREA_SPLINE
                                selectedDayIndex = -1
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Area Chart",
                                tint = if (selectedChartType == ChartType.AREA_SPLINE) Color.White else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedChartType == ChartType.BAR_GROUPED) colors.primary else colors.pillBg,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                selectedChartType = ChartType.BAR_GROUPED
                                selectedDayIndex = -1
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = if (selectedChartType == ChartType.BAR_GROUPED) Color.White else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Badges Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.pillBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = colors.badgeSuccessText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Avg: ",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$avgWeeklyRate%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.badgeSuccessText
                    )
                }

                if (bestDay != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Peak: ${bestDay.dayLabel} (${bestDay.completionRatePercent}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Tooltip Callout if a node/bar is tapped
            val activeStat = if (selectedDayIndex in weeklyData.indices) weeklyData[selectedDayIndex] else null
            if (activeStat != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceElevated,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(colors.primary, colors.secondary))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${activeStat.dayLabel} (${activeStat.dateLabel})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${activeStat.completedCount} of ${activeStat.totalCount} tasks finished",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.badgeSuccessBg
                        ) {
                            Text(
                                text = "${activeStat.completionRatePercent}% Complete 💕",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.badgeSuccessText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Canvas Chart Rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (selectedChartType == ChartType.AREA_SPLINE) {
                    RechartsAreaSplineChart(
                        data = weeklyData,
                        animProgress = animProgress.value,
                        selectedIdx = selectedDayIndex,
                        onSelectIdx = { selectedDayIndex = it },
                        primaryColor = colors.primary,
                        secondaryColor = colors.secondary
                    )
                } else {
                    RechartsBarChart(
                        data = weeklyData,
                        animProgress = animProgress.value,
                        selectedIdx = selectedDayIndex,
                        onSelectIdx = { selectedDayIndex = it },
                        primaryColor = colors.primary,
                        secondaryColor = colors.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Day Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEachIndexed { idx, stat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                selectedDayIndex = if (selectedDayIndex == idx) -1 else idx
                            }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = stat.dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (stat.isToday || selectedDayIndex == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedDayIndex == idx) colors.primary else if (stat.isToday) colors.primaryVariant else colors.textSecondary
                        )
                        Text(
                            text = stat.dateLabel,
                            fontSize = 9.sp,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Completion Rate (%)",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.dp)
                        .background(colors.badgeSuccessText)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Target (80%)",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun RechartsAreaSplineChart(
    data: List<DailyCompletionStat>,
    animProgress: Float,
    selectedIdx: Int,
    onSelectIdx: (Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 16f
        val paddingTop = 20f
        val chartHeight = height - paddingBottom - paddingTop

        // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        val gridSteps = 4
        for (i in 0..gridSteps) {
            val y = paddingTop + chartHeight * (1f - (i.toFloat() / gridSteps))
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Draw 80% Target Benchmark Line
        val targetY = paddingTop + chartHeight * (1f - 0.8f)
        drawLine(
            color = Color(0xFF10B981).copy(alpha = 0.5f),
            start = Offset(0f, targetY),
            end = Offset(width, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )

        if (data.size < 2) return@Canvas

        val stepX = width / (data.size - 1)
        val points = data.mapIndexed { index, stat ->
            val x = index * stepX
            val normalizedRate = (stat.completionRatePercent.toFloat() / 100f).coerceIn(0f, 1f)
            val y = paddingTop + chartHeight * (1f - (normalizedRate * animProgress))
            Offset(x, y)
        }

        // Create Smooth Cubic Spline Curve
        val strokePath = Path()
        val fillPath = Path()

        strokePath.moveTo(points[0].x, points[0].y)
        fillPath.moveTo(points[0].x, height - paddingBottom)
        fillPath.lineTo(points[0].x, points[0].y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
            strokePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
        }

        fillPath.lineTo(points.last().x, height - paddingBottom)
        fillPath.close()

        // Draw Area Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f * animProgress),
                    secondaryColor.copy(alpha = 0.12f * animProgress),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw Spline Stroke
        drawPath(
            path = strokePath,
            brush = Brush.horizontalGradient(
                listOf(
                    primaryColor,
                    secondaryColor,
                    Color(0xFF10B981)
                )
            ),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw Data Points / Dots
        points.forEachIndexed { idx, point ->
            val isSelected = idx == selectedIdx
            val dotRadius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()

            // Outer ring
            drawCircle(
                color = if (isSelected) Color(0xFF10B981) else primaryColor,
                radius = dotRadius,
                center = point
            )

            // Inner center dot
            drawCircle(
                color = Color.White,
                radius = dotRadius * 0.55f,
                center = point
            )
        }
    }
}

@Composable
fun RechartsBarChart(
    data: List<DailyCompletionStat>,
    animProgress: Float,
    selectedIdx: Int,
    onSelectIdx: (Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val paddingBottom = 16f
        val paddingTop = 20f
        val chartHeight = height - paddingBottom - paddingTop

        // Draw horizontal grid lines
        val gridSteps = 4
        for (i in 0..gridSteps) {
            val y = paddingTop + chartHeight * (1f - (i.toFloat() / gridSteps))
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val barWidth = (width / data.size) * 0.48f
        val sectionWidth = width / data.size

        data.forEachIndexed { index, stat ->
            val centerX = index * sectionWidth + sectionWidth / 2f
            val left = centerX - barWidth / 2f
            val normalizedRate = (stat.completionRatePercent.toFloat() / 100f).coerceIn(0f, 1f)
            val barHeight = chartHeight * normalizedRate * animProgress
            val top = paddingTop + (chartHeight - barHeight)
            val isSelected = index == selectedIdx

            // Bar background slot
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.08f),
                topLeft = Offset(left, paddingTop),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Active bar gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        if (isSelected) Color(0xFF10B981) else primaryColor,
                        if (isSelected) Color(0xFF059669) else secondaryColor
                    )
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }
}

@Composable
fun TaskCompletionProgressRingCard(
    completedTasks: Int,
    totalTasks: Int,
    pendingTasks: Int,
    overallRate: Int,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val targetProgress = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks).coerceIn(0f, 1f) else 0f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        animatedProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    colors.border,
                    colors.borderSubtle
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_completion_progress_ring_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Task Completion Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Completed vs. Remaining study tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.pillBg
                ) {
                    Text(
                        text = "$overallRate% Done 💕",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Animated Progress Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(105.dp)
                ) {
                    val trackColor = colors.pillBg
                    val primaryColor = colors.primary
                    val emeraldColor = colors.badgeSuccessText

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ) {
                        val strokeWidth = 10.dp.toPx()
                        val ringSize = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(ringSize, ringSize)

                        // Background track
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Animated progress sweep arc
                        if (animatedProgress.value > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        primaryColor,
                                        emeraldColor,
                                        primaryColor
                                    )
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress.value,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center percentage & ratio
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(animatedProgress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "$completedTasks / $totalTasks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }

                // Summary Comparison Breakdown (Completed vs Remaining)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Tasks Completed Item
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colors.badgeSuccessText)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Total Completed",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Mastered & verified",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "$completedTasks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.badgeSuccessText
                            )
                        }
                    }

                    // Remaining Tasks Item
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Remaining Tasks",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Pending study",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "$pendingTasks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        }
    }
}
