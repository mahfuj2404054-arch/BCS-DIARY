package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import com.example.ui.components.DueTodayAlertCard
import com.example.util.TaskNotificationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskWithDetails
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.StreakCard
import com.example.ui.components.TaskCard
import com.example.ui.components.WeeklyCalendarView
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.TaskFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    currentUser: UserEntity,
    tasks: List<TaskWithDetails>,
    allTasks: List<TaskWithDetails> = tasks,
    allSubjects: List<SubjectEntity>,
    searchQuery: String,
    selectedSubjectFilter: String?,
    taskFilter: TaskFilter,
    onSearchQueryChange: (String) -> Unit,
    onSubjectFilterChange: (String?) -> Unit,
    onTaskFilterChange: (TaskFilter) -> Unit,
    onTaskClick: (String) -> Unit,
    onToggleComplete: (TaskWithDetails) -> Unit,
    onSwitchAccount: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onOpenThemePicker: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val displayedTasks = remember(tasks, allTasks, selectedCalendarDate) {
        if (selectedCalendarDate != null) {
            allTasks.filter { it.task.dueDate == selectedCalendarDate }
        } else {
            tasks
        }
    }

    val completedCount = allTasks.count { it.isCompletedByCurrentUser }
    val totalCount = allTasks.size

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .testTag("student_dashboard_screen"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // --- 1. Top Bar with Profile Header & Theme Switcher ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar & Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors.primaryGradient))
                                .testTag("profile_avatar")
                        ) {
                            Text(
                                text = currentUser.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "hey ${currentUser.name.lowercase()} ✨",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$todayFormatted ✨",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Action buttons & Three-Dot Dropdown Menu
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Leaderboard Quick Trophy
                        IconButton(
                            onClick = onOpenLeaderboard,
                            modifier = Modifier.testTag("leaderboard_quick_btn")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.pillBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Leaderboard",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Three-Dot Menu
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier.testTag("three_dot_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = colors.textPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier
                                    .background(colors.cardBackground)
                                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                    .testTag("three_dot_dropdown_menu")
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Real-Time Leaderboard 🏆",
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenLeaderboard()
                                    },
                                    modifier = Modifier.testTag("menu_item_leaderboard")
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = "Theme Palette 🎨", color = colors.textPrimary)
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenThemePicker()
                                    },
                                    modifier = Modifier.testTag("menu_item_theme_palette")
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = null,
                                                tint = if (isDarkTheme) Color(0xFFFBBF24) else colors.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (isDarkTheme) "Light Mode ☀️" else "Dark Mode 🌙",
                                                color = colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onToggleDarkTheme()
                                    },
                                    modifier = Modifier.testTag("menu_item_toggle_dark")
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Send Due Today Alert 🔔",
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                        val dueToday = allTasks.filter { it.task.dueDate == todayStr && !it.isCompletedByCurrentUser }
                                        if (dueToday.isNotEmpty()) {
                                            TaskNotificationHelper.sendDueTodayNotification(context, dueToday)
                                            Toast.makeText(context, "🔔 Local notification sent for ${dueToday.size} due task(s)!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "✨ No pending tasks due today!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_item_send_alert")
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Sign Out 🚪",
                                                color = Color(0xFFEF4444),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onSwitchAccount()
                                    },
                                    modifier = Modifier.testTag("menu_item_sign_out")
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 1.5. Due Today In-App Visual Alert Banner ---
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                DueTodayAlertCard(
                    allTasks = allTasks,
                    onFilterToday = {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        selectedCalendarDate = todayStr
                        onTaskFilterChange(TaskFilter.TODAY)
                    }
                )
            }
        }

        // --- 2. Streak & Daily Progress Banner ---
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                StreakCard(
                    streakDays = currentUser.streakDays,
                    completedTodayCount = completedCount,
                    totalTodayCount = totalCount
                )
            }
        }

        // --- 3. Weekly Study Calendar View ---
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                WeeklyCalendarView(
                    tasks = allTasks,
                    selectedDate = selectedCalendarDate,
                    onDateSelected = { newDate ->
                        selectedCalendarDate = newDate
                    }
                )
            }
        }

        // --- 4. Search & Subject Filter Section ---
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("✨ Search study tasks, subjects...", fontSize = 13.sp, color = colors.textTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Horizontal Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectFilter == null,
                            onClick = { onSubjectFilterChange(null) },
                            label = { Text("✨ All Subjects", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedSubjectFilter == null,
                                borderColor = colors.border,
                                selectedBorderColor = colors.primary
                            ),
                            modifier = Modifier.testTag("filter_all_subjects")
                        )
                    }
                    items(allSubjects, key = { it.id }) { subject ->
                        val isSelected = selectedSubjectFilter == subject.id
                        val subjectColor = parseHexColor(subject.colorHex, colors.primary)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSubjectFilterChange(if (isSelected) null else subject.id)
                            },
                            label = { Text("✨ ${subject.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subjectColor,
                                selectedLabelColor = Color.White,
                                containerColor = colors.surface,
                                labelColor = colors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = colors.border,
                                selectedBorderColor = subjectColor
                            ),
                            modifier = Modifier.testTag("filter_subject_${subject.id}")
                        )
                    }
                }
            }
        }

        // --- 5. Tasks Section Header & Filter Tabs ---
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (selectedCalendarDate != null) "Tasks for Selected Day" else "Today's Study Plan ✨",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        if (selectedCalendarDate != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.pillBg,
                                modifier = Modifier.clickable { selectedCalendarDate = null }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$selectedCalendarDate ✕",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                        )
                    ) {
                        Text(
                            text = "${displayedTasks.size} tasks",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (selectedCalendarDate == null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Task Filter Tabs (Today, Pending, Completed, All)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            TaskFilter.TODAY to "✨ Today",
                            TaskFilter.PENDING to "⏳ Pending",
                            TaskFilter.COMPLETED to "💖 Done",
                            TaskFilter.ALL to "🎀 All"
                        ).forEach { (filter, label) ->
                            val isSelected = taskFilter == filter
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) colors.primary else colors.surface,
                                border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onTaskFilterChange(filter) }
                                    .testTag("tab_filter_${filter.name}")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else colors.textSecondary,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Tasks List Items or Empty State ---
        if (displayedTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(colors.pillBg)
                        ) {
                            Text(text = "💖", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedCalendarDate != null) "No Tasks on $selectedCalendarDate" else "All Clear! ✨",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedCalendarDate != null) "You have no study tasks scheduled for this day." else "No pending study goals right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(displayedTasks, key = { it.task.id }) { taskWithDetails ->
                TaskCardItem(
                    taskWithDetails = taskWithDetails,
                    onTaskClick = onTaskClick,
                    onToggleComplete = onToggleComplete
                )
            }
        }
    }
}
}

@Composable
private fun TaskCardItem(
    taskWithDetails: TaskWithDetails,
    onTaskClick: (String) -> Unit,
    onToggleComplete: (TaskWithDetails) -> Unit
) {
    val onClick = remember(taskWithDetails.task.id, onTaskClick) { { onTaskClick(taskWithDetails.task.id) } }
    val onToggle = remember(taskWithDetails, onToggleComplete) { { onToggleComplete(taskWithDetails) } }

    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        TaskCard(
            taskWithDetails = taskWithDetails,
            onTaskClick = onClick,
            onToggleComplete = onToggle
        )
    }
}
