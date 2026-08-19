package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserEntity
import com.example.ui.theme.AppTheme

enum class LeaderboardTab {
    ALL_TIME,
    STREAK,
    TODAY
}

data class LeaderboardEntry(
    val user: UserEntity,
    val completedTasksCount: Int,
    val todayCompletedCount: Int,
    val streakDays: Int,
    val completionRate: Int,
    val rank: Int = 0
)

@Composable
fun LeaderboardDialog(
    entries: List<LeaderboardEntry>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var activeTab by remember { mutableStateOf(LeaderboardTab.ALL_TIME) }

    // Sort entries according to selected tab
    val sortedEntries = remember(entries, activeTab) {
        when (activeTab) {
            LeaderboardTab.ALL_TIME -> entries.sortedByDescending { it.completedTasksCount }
            LeaderboardTab.STREAK -> entries.sortedByDescending { it.streakDays }
            LeaderboardTab.TODAY -> entries.sortedByDescending { it.todayCompletedCount }
        }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.background,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(colors.primary, colors.secondary))
            ),
            shadowElevation = 16.dp,
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .testTag("leaderboard_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header with live pulsing status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Leaderboard",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Real-time sync active ✨",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("leaderboard_close_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.pillBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher (All-Time, Streak, Today)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        LeaderboardTab.ALL_TIME to "🏆 All-Time",
                        LeaderboardTab.STREAK to "🔥 Streaks",
                        LeaderboardTab.TODAY to "⚡ Today"
                    ).forEach { (tab, label) ->
                        val isSelected = activeTab == tab
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colors.primary else colors.surface,
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { activeTab = tab }
                                .testTag("leaderboard_tab_${tab.name}")
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

                Spacer(modifier = Modifier.height(16.dp))

                // Leaderboard List
                if (sortedEntries.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "No study records yet ✨",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sortedEntries, key = { it.user.id }) { entry ->
                            val isMe = currentUser?.id == entry.user.id
                            val rankColor = when (entry.rank) {
                                1 -> Color(0xFFF59E0B) // Gold
                                2 -> Color(0xFF94A3B8) // Silver
                                3 -> Color(0xFFD97706) // Bronze
                                else -> colors.primary
                            }

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) colors.pillBg else colors.cardBackground
                                ),
                                border = if (isMe) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(colors.primary, colors.secondary))
                                ) else CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (entry.rank <= 3) 3.dp else 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("leaderboard_item_${entry.user.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    // Rank Badge
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (entry.rank <= 3) rankColor.copy(alpha = 0.2f) else colors.surface
                                            )
                                            .border(
                                                width = if (entry.rank <= 3) 1.5.dp else 1.dp,
                                                color = rankColor,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Text(
                                            text = when (entry.rank) {
                                                1 -> "🥇"
                                                2 -> "🥈"
                                                3 -> "🥉"
                                                else -> "#${entry.rank}"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (entry.rank <= 3) 16.sp else 12.sp,
                                            color = if (entry.rank <= 3) Color.Unspecified else colors.textPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Student Avatar & Name
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.user.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (isMe) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = colors.primary
                                                ) {
                                                    Text(
                                                        text = "YOU",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = Color(0xFFF97316),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${entry.streakDays}d streak",
                                                fontSize = 11.5.sp,
                                                color = colors.textSecondary
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = "• ${entry.completedTasksCount} tasks done",
                                                fontSize = 11.5.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Score / Metric Chip
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = when (activeTab) {
                                                LeaderboardTab.ALL_TIME -> "${entry.completedTasksCount} pts"
                                                LeaderboardTab.STREAK -> "${entry.streakDays} days"
                                                LeaderboardTab.TODAY -> "${entry.todayCompletedCount} today"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.primary
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "${entry.completionRate}% rate",
                                            fontSize = 10.5.sp,
                                            color = colors.textTertiary
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
