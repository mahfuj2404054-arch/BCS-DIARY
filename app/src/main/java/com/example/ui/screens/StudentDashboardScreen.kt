package com.example.ui.screens

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import com.example.ui.components.DueTodayAlertCard
import com.example.ui.components.EditProfileDialog
import com.example.util.TaskNotificationHelper
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    onOpenProgress: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onSaveProfile: (name: String, photoUri: String?, dateOfBirth: String?, bio: String?, schoolOrGrade: String?, avatarColorHex: String) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val loadedAvatarBitmap = remember(currentUser.photoUri) {
        currentUser.photoUri?.let { uriStr ->
            com.example.ui.components.loadBitmapFromPathOrUri(context, uriStr)
        }
    }

    val displayedTasks = remember(tasks, allTasks, selectedCalendarDate) {
        if (selectedCalendarDate != null) {
            allTasks.filter { it.task.dueDate == selectedCalendarDate }
        } else {
            tasks
        }
    }

    val completedCount = remember(allTasks) { allTasks.count { it.isCompletedByCurrentUser } }
    val totalCount = remember(allTasks) { allTasks.size }

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
                    // Profile Avatar & Info (Clickable to edit profile)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showEditProfileDialog = true }
                            .padding(4.dp)
                            .testTag("header_profile_clickable")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(currentUser.avatarColorHex))
                                    } catch (_: Exception) {
                                        colors.primary
                                    }
                                )
                                .testTag("profile_avatar")
                        ) {
                            if (loadedAvatarBitmap != null) {
                                Image(
                                    bitmap = loadedAvatarBitmap,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = currentUser.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "hey ${currentUser.name.lowercase()} ✨",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = currentUser.schoolOrGrade?.ifEmpty { null } ?: "$todayFormatted ✨",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                                imageVector = Icons.Default.AutoGraph,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Progress Analytics 📈",
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenProgress()
                                    },
                                    modifier = Modifier.testTag("menu_item_progress")
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Edit Profile 👤",
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showEditProfileDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_item_edit_profile")
                                )

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

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = { name, photoUri, dateOfBirth, bio, schoolOrGrade, avatarColorHex ->
                onSaveProfile(name, photoUri, dateOfBirth, bio, schoolOrGrade, avatarColorHex)
            }
        )
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
