package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.model.RepeatSchedule
import com.example.data.model.TaskPriority
import com.example.data.model.TaskWithDetails
import com.example.ui.theme.AppTheme
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Fast cache to eliminate repeated hex parsing
private val hexColorCache = ConcurrentHashMap<String, Color>()

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF8B5CF6)): Color {
    if (hex.isBlank()) return defaultColor
    return hexColorCache.computeIfAbsent(hex) { rawHex ->
        try {
            val cleanHex = rawHex.removePrefix("#")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color((0xFF000000 or colorInt).toInt())
            } else {
                Color(colorInt.toInt())
            }
        } catch (e: Exception) {
            defaultColor
        }
    }
}

fun openGoogleDriveUrl(context: Context, url: String) {
    if (url.isBlank()) {
        Toast.makeText(context, "No Google Drive link attached", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Opening link: $url", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SubjectBadge(
    subjectName: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val color = parseHexColor(colorHex, colors.primary)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = if (colors.isDark) 0.22f else 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.4f)))
        ),
        modifier = modifier
    ) {
        Text(
            text = "✨ $subjectName",
            color = if (colors.isDark) color.copy(alpha = 0.95f) else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TopicBadge(
    topicName: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.pillBg,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(colors.border, colors.borderSubtle))
        ),
        modifier = modifier
    ) {
        Text(
            text = "#$topicName",
            color = colors.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val (bg, text, label) = when (priority) {
        TaskPriority.HIGH -> Triple(
            colors.primary.copy(alpha = if (colors.isDark) 0.25f else 0.15f),
            colors.primary,
            "💖 High"
        )
        TaskPriority.MEDIUM -> Triple(
            colors.secondary.copy(alpha = if (colors.isDark) 0.25f else 0.15f),
            colors.secondary,
            "⭐ Med"
        )
        TaskPriority.LOW -> Triple(
            colors.badgeSuccessBg,
            colors.badgeSuccessText,
            "🌸 Low"
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
    }
}

@Composable
fun RepeatBadge(
    repeatSchedule: RepeatSchedule,
    modifier: Modifier = Modifier
) {
    if (repeatSchedule == RepeatSchedule.NONE) return
    val colors = AppTheme.colors

    val label = when (repeatSchedule) {
        RepeatSchedule.DAILY -> "Daily"
        RepeatSchedule.WEEKDAYS -> "Weekdays"
        RepeatSchedule.WEEKLY -> "Weekly"
        RepeatSchedule.NONE -> ""
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.pillBg)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = "Repeat",
            tint = colors.primary,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = colors.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GoogleDriveButton(
    driveUrl: String,
    label: String = "Study Material",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.pillBg,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.secondary.copy(alpha = 0.4f)))
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { openGoogleDriveUrl(context, driveUrl) }
            .testTag("google_drive_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Google Drive",
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label.ifBlank { "Drive Docs" },
                color = colors.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = colors.textSecondary,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    taskWithDetails: TaskWithDetails,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val isCompleted = taskWithDetails.isCompletedByCurrentUser
    val task = taskWithDetails.task
    val subject = taskWithDetails.subject
    val topic = taskWithDetails.topic

    val animatedCardBg by animateColorAsState(
        targetValue = if (isCompleted) {
            colors.cardBackground.copy(alpha = if (colors.isDark) 0.6f else 0.75f)
        } else {
            colors.cardBackground
        },
        label = "cardBg"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = animatedCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted || colors.isDark) 0.dp else 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (isCompleted) colors.border.copy(alpha = 0.4f) else colors.primary.copy(alpha = 0.45f),
                    if (isCompleted) colors.borderSubtle.copy(alpha = 0.3f) else colors.secondary.copy(alpha = 0.4f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onTaskClick() }
            .testTag("task_item_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badges row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (subject != null) {
                    SubjectBadge(subjectName = subject.name, colorHex = subject.colorHex)
                }
                if (topic != null) {
                    TopicBadge(topicName = topic.name)
                }
                PriorityBadge(priority = task.priority)
                RepeatBadge(repeatSchedule = task.repeatSchedule)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Bubbly Heart / Check Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isCompleted) colors.textTertiary else colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Satisfying Task Check Button with confetti particle explosion
                SatisfyingTaskCheckButton(
                    isCompleted = isCompleted,
                    onToggleComplete = onToggleComplete,
                    taskId = task.id
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Due Date/Time and Google Drive Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Due",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${task.dueDate} • ${task.dueTime}",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (task.googleDriveUrl.isNotBlank()) {
                    GoogleDriveButton(
                        driveUrl = task.googleDriveUrl,
                        label = "Study Docs"
                    )
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    streakDays: Int,
    completedTodayCount: Int,
    totalTodayCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val progress = if (totalTodayCount > 0) (completedTodayCount.toFloat() / totalTodayCount).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "streakProgress"
    )

    val flameScale = remember { Animatable(1f) }
    val flameRotation = remember { Animatable(0f) }
    val glowScale = remember { Animatable(1f) }
    val glowAlpha = remember { Animatable(0f) }
    var showCelebrationBadge by remember { mutableStateOf(false) }
    var lastCompletedCount by remember { mutableIntStateOf(completedTodayCount) }

    val infiniteTransition = rememberInfiniteTransition(label = "emberGlow")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    LaunchedEffect(completedTodayCount) {
        if (completedTodayCount > lastCompletedCount) {
            showCelebrationBadge = true
            launch {
                glowScale.snapTo(0.8f)
                glowAlpha.snapTo(0.8f)
                glowScale.animateTo(
                    targetValue = 2.2f,
                    animationSpec = tween(650, easing = FastOutSlowInEasing)
                )
                glowAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(650, easing = FastOutSlowInEasing)
                )
            }

            launch {
                flameRotation.animateTo(-15f, animationSpec = tween(90))
                flameRotation.animateTo(15f, animationSpec = tween(90))
                flameRotation.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }

            launch {
                flameScale.animateTo(1.45f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
                flameScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }

            delay(2800)
            showCelebrationBadge = false
        }
        lastCompletedCount = completedTodayCount
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(colors.primary.copy(alpha = 0.5f), colors.secondary.copy(alpha = 0.5f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_banner_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Flame container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (glowAlpha.value > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .scale(glowScale.value)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                colors.primary.copy(alpha = glowAlpha.value),
                                                colors.secondary.copy(alpha = glowAlpha.value * 0.5f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .scale(flameScale.value * ambientPulse)
                                .rotate(flameRotation.value)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(colors.pillBg, colors.surfaceElevated))
                                )
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(colors.primaryGradient),
                                    shape = CircleShape
                                )
                        ) {
                            Text(text = "🔥", fontSize = 22.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedContent(
                                targetState = streakDays,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                            slideOutVertically { height -> -height } + fadeOut()
                                        )
                                    } else {
                                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                            slideOutVertically { height -> height } + fadeOut()
                                        )
                                    }
                                },
                                label = "streakDaysCounter"
                            ) { days ->
                                Text(
                                    text = "$days Day Streak ✨",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "✨", fontSize = 14.sp)
                        }

                        AnimatedContent(
                            targetState = completedTodayCount,
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically { it / 2 }).togetherWith(
                                    fadeOut(tween(200)) + slideOutVertically { -it / 2 }
                                )
                            },
                            label = "taskCountTransition"
                        ) { count ->
                            Text(
                                text = if (count >= totalTodayCount && totalTodayCount > 0)
                                    "All tasks done! Superb momentum today! 💖"
                                else
                                    "$count of $totalTodayCount goals completed today 💕",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (count >= totalTodayCount && totalTodayCount > 0) colors.primary else colors.textSecondary
                            )
                        }
                    }
                }

                // Percentage Badge & Celebratory Pill
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = showCelebrationBadge,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.primary.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(colors.primaryGradient)
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+1 Streak! 💖",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.pillBg,
                        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spring animated pastel progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = colors.primary,
                trackColor = colors.pillBg
            )
        }
    }
}

private data class TaskConfettiParticle(
    val angle: Double,
    val maxDistance: Float,
    val color: Color,
    val sizePx: Float,
    val isCircle: Boolean
)

@Composable
fun TaskCompletionConfettiBurst(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    if (trigger <= 0) return

    val animProgress = remember { Animatable(0f) }
    val particles = remember(trigger) {
        val palette = listOf(
            Color(0xFF10B981), // Emerald Green
            Color(0xFFF59E0B), // Bright Amber/Gold
            Color(0xFF8B5CF6), // Royal Violet
            Color(0xFFEC4899), // Hot Pink
            Color(0xFF3B82F6), // Electric Blue
            Color(0xFF06B6D4)  // Cyan
        )
        List(16) { i ->
            val baseAngle = i * (360.0 / 16.0) + (Random.nextDouble() * 12.0 - 6.0)
            val rad = Math.toRadians(baseAngle)
            val dist = Random.nextFloat() * 48f + 28f // 28dp to 76dp burst distance
            val col = palette[i % palette.size]
            val sz = Random.nextFloat() * 7f + 4f // 4dp to 11dp particle size
            TaskConfettiParticle(rad, dist, col, sz, isCircle = i % 2 == 0)
        }
    }

    LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val p = animProgress.value
    if (p > 0f && p < 1f) {
        val alpha = (1f - p * p).coerceIn(0f, 1f)
        Canvas(modifier = modifier) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Expanding ring ripple
            val ringRadius = (size.width / 3.2f) + (p * 26.dp.toPx())
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = alpha * 0.5f),
                radius = ringRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = (3.5.dp.toPx() * (1f - p)).coerceAtLeast(0.5f)
                )
            )

            // Confetti burst particles
            particles.forEach { pt ->
                val currentDist = pt.maxDistance.dp.toPx() * p
                val px = center.x + (cos(pt.angle) * currentDist).toFloat()
                val py = center.y + (sin(pt.angle) * currentDist).toFloat()
                val pSize = pt.sizePx.dp.toPx() * (1f - p * 0.35f)

                if (pt.isCircle) {
                    drawCircle(
                        color = pt.color.copy(alpha = alpha),
                        radius = pSize / 2f,
                        center = Offset(px, py)
                    )
                } else {
                    drawRoundRect(
                        color = pt.color.copy(alpha = alpha),
                        topLeft = Offset(px - pSize / 2f, py - pSize / 2f),
                        size = Size(pSize * 1.3f, pSize * 0.8f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun SatisfyingTaskCheckButton(
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    taskId: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var burstCount by remember { mutableIntStateOf(0) }

    // Spring animations for check button scale & rotation
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.12f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btnScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isCompleted) 0f else -12f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnRotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(52.dp)
    ) {
        // Confetti burst particle overlay around the checkmark button
        TaskCompletionConfettiBurst(
            trigger = burstCount,
            modifier = Modifier.size(100.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .rotate(rotation)
                .clip(CircleShape)
                .background(
                    if (isCompleted) Brush.linearGradient(
                        listOf(Color(0xFF10B981), Color(0xFF059669))
                    )
                    else Brush.linearGradient(listOf(colors.pillBg, colors.surfaceElevated))
                )
                .border(
                    width = 2.dp,
                    brush = if (isCompleted) Brush.linearGradient(
                        listOf(Color(0xFF34D399), Color(0xFF10B981))
                    )
                    else Brush.linearGradient(listOf(colors.border, colors.borderSubtle)),
                    shape = CircleShape
                )
                .clickable {
                    if (!isCompleted) {
                        burstCount += 1
                    }
                    onToggleComplete()
                }
                .testTag("quick_complete_btn_$taskId")
        ) {
            AnimatedVisibility(
                visible = isCompleted,
                enter = scaleIn(
                    spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = !isCompleted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colors.border.copy(alpha = 0.5f))
                )
            }
        }
    }
}
