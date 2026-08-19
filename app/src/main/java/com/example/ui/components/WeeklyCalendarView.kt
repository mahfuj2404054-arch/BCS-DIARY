package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskWithDetails
import com.example.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayInfo(
    val dateString: String,
    val dayOfWeekShort: String,
    val dayOfMonth: Int,
    val monthNameShort: String,
    val isToday: Boolean,
    val isPast: Boolean
)

data class CalendarDay(
    val dateString: String,
    val dayOfWeekShort: String,
    val dayOfMonth: Int,
    val monthNameShort: String,
    val isToday: Boolean,
    val isPast: Boolean,
    val tasksDue: List<TaskWithDetails>
)

@Composable
fun WeeklyCalendarView(
    tasks: List<TaskWithDetails>,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var weekOffset by remember { mutableStateOf(0) }

    val baseCal = remember(weekOffset) {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
    }

    val daysTemplate = remember(baseCal) {
        val todayCalendar = Calendar.getInstance()
        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCalendar.time)

        val list = ArrayList<Pair<String, CalendarDayInfo>>(7)
        val dayCal = baseCal.clone() as Calendar
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val monthShortFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (i in 0..6) {
            val dateStr = dayFormat.format(dayCal.time)
            val dayOfWeek = dayOfWeekFormat.format(dayCal.time)
            val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH)
            val monthName = monthShortFormat.format(dayCal.time)
            val isToday = (dateStr == todayDateStr)
            val isPast = dayCal.time.before(todayCalendar.time) && !isToday

            list.add(
                dateStr to CalendarDayInfo(
                    dateString = dateStr,
                    dayOfWeekShort = dayOfWeek,
                    dayOfMonth = dayOfMonth,
                    monthNameShort = monthName,
                    isToday = isToday,
                    isPast = isPast
                )
            )
            dayCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val tasksByDate = remember(tasks) {
        tasks.groupBy { it.task.dueDate }
    }

    val weekDays = remember(daysTemplate, tasksByDate) {
        daysTemplate.map { (dateStr, info) ->
            CalendarDay(
                dateString = info.dateString,
                dayOfWeekShort = info.dayOfWeekShort,
                dayOfMonth = info.dayOfMonth,
                monthNameShort = info.monthNameShort,
                isToday = info.isToday,
                isPast = info.isPast,
                tasksDue = tasksByDate[dateStr] ?: emptyList()
            )
        }
    }

    val monthYearLabel = remember(baseCal) {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        format.format(baseCal.time)
    }

    val selectedDayObj = remember(weekDays, selectedDate) {
        weekDays.find { it.dateString == selectedDate }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(colors.primary.copy(alpha = 0.45f), colors.secondary.copy(alpha = 0.45f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_calendar_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Month & Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🗓️ $monthYearLabel",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    if (weekOffset != 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.pillBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { weekOffset = 0 }
                        ) {
                            Text(
                                text = "Today ✨",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Prev / Next Week navigation buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { weekOffset -= 1 },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_prev_week_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Week",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { weekOffset += 1 },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_next_week_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Week",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7-Day Pill Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDays.forEach { day ->
                    CalendarDayPill(
                        day = day,
                        isSelected = (day.dateString == selectedDate),
                        onClick = {
                            if (selectedDate == day.dateString) {
                                onDateSelected(null)
                            } else {
                                onDateSelected(day.dateString)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selected Day Context Banner
            AnimatedVisibility(
                visible = selectedDayObj != null,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                if (selectedDayObj != null) {
                    val count = selectedDayObj.tasksDue.size
                    val done = selectedDayObj.tasksDue.count { it.isCompletedByCurrentUser }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.pillBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedDayObj.dayOfWeekShort} ${selectedDayObj.dayOfMonth}: " +
                                            if (count == 0) "No tasks scheduled ✨"
                                            else "$count task${if (count > 1) "s" else ""} ($done done 💕)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.surface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDateSelected(null) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = "View All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = colors.primary,
                                        modifier = Modifier.size(12.dp)
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

@Composable
fun CalendarDayPill(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val taskCount = day.tasksDue.size
    val allCompleted = taskCount > 0 && day.tasksDue.all { it.isCompletedByCurrentUser }

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.primary
            day.isToday -> colors.pillBg
            else -> Color.Transparent
        },
        label = "pill_bg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            day.isToday -> colors.primary
            else -> colors.textPrimary
        },
        label = "pill_text"
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White.copy(alpha = 0.85f)
            day.isToday -> colors.primary
            else -> colors.textSecondary
        },
        label = "pill_label"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp)
            .testTag("calendar_day_${day.dateString}")
    ) {
        Text(
            text = day.dayOfWeekShort.take(3),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${day.dayOfMonth}",
            fontSize = 14.sp,
            fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Indicator Dot
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(6.dp)
        ) {
            when {
                taskCount == 0 -> {}
                allCompleted -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else colors.secondary)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else colors.primary)
                    )
                }
            }
        }
    }
}
