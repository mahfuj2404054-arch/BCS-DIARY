package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CelebrationData(
    val title: String = "Streak On Fire! 🔥",
    val subtitle: String = "Great job! Keep up the daily study habit.",
    val streakDays: Int = 1,
    val completedCount: Int = 1,
    val totalCount: Int = 1,
    val isStreakBonus: Boolean = false
)

private data class ConfettiParticle(
    val startX: Float,        // 0.0 to 1.0 (relative canvas width)
    val startY: Float,        // 0.0 to 1.0
    val velocityX: Float,     // Horizontal velocity
    val velocityY: Float,     // Initial upward/burst velocity
    val size: Float,          // Size in px
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean,
    val isRibbon: Boolean
)

@Composable
fun ConfettiCelebrationOverlay(
    celebrationData: CelebrationData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = celebrationData != null

    // Auto dismiss after 2.8 seconds
    LaunchedEffect(celebrationData) {
        if (celebrationData != null) {
            delay(2800)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(250)),
        modifier = modifier.fillMaxSize()
    ) {
        if (celebrationData != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
                    .testTag("celebration_overlay")
            ) {
                // Confetti Falling Particle Canvas
                ConfettiCanvas(
                    particleCount = if (celebrationData.isStreakBonus) 70 else 45
                )

                // Celebratory Center Popup Card
                CelebrationCard(
                    data = celebrationData,
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
private fun CelebrationCard(
    data: CelebrationData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFF59E0B),
                    Color(0xFF10B981),
                    Color(0xFF6366F1)
                )
            )
        ),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(0.92f)
            .testTag("celebration_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Top Row: Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close celebration",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Pulsing Fire & Milestone Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFEF3C7),
                                Color(0xFFFDE68A),
                                Color(0xFFF59E0B).copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = if (data.isStreakBonus) Icons.Default.LocalFireDepartment else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (data.isStreakBonus) Color(0xFFEA580C) else Color(0xFF059669),
                    modifier = Modifier
                        .size(36.dp)
                        .scale(flameScale)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Celebration Title
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Description
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Streak & Completion Pill Highlights
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Days Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.5f), Color(0xFFF59E0B).copy(alpha = 0.5f)))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${data.streakDays} Day Streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Progress Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(Color(0xFF10B981).copy(alpha = 0.5f), Color(0xFF10B981).copy(alpha = 0.5f)))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${data.completedCount} Done Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button: Keep Studying
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClose() }
                    .testTag("celebration_continue_btn")
            ) {
                Text(
                    text = "Keep Going 🚀",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

/**
 * Animated Canvas that shoots and floats confetti particles down the screen.
 */
@Composable
private fun ConfettiCanvas(
    particleCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    val palette = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Solar Amber
        Color(0xFF6366F1), // Indigo Violet
        Color(0xFFF43F5E), // Coral Rose
        Color(0xFF0284C7), // Sky Blue
        Color(0xFFFBBF24), // Golden Yellow
        Color(0xFFEC4899)  // Pink
    )

    val particles = remember {
        val rand = Random(System.currentTimeMillis())
        List(particleCount) {
            ConfettiParticle(
                startX = rand.nextFloat(),
                startY = rand.nextFloat() * 0.25f, // start around top quarter
                velocityX = (rand.nextFloat() - 0.5f) * 450f,
                velocityY = rand.nextFloat() * 700f + 300f,
                size = rand.nextFloat() * 12f + 8f,
                color = palette[rand.nextInt(palette.size)],
                rotationSpeed = (rand.nextFloat() - 0.5f) * 720f,
                isCircle = rand.nextBoolean(),
                isRibbon = rand.nextFloat() > 0.6f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        particles.forEach { p ->
            val curX = (p.startX * w) + (p.velocityX * t) + (sin(t * 10f + p.startX * 20f) * 25f)
            val curY = (p.startY * h) + (p.velocityY * t) + (0.5f * 980f * t * t)
            val rotation = p.rotationSpeed * t

            if (curY <= h + 50f) {
                rotate(degrees = rotation, pivot = Offset(curX, curY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2f,
                            center = Offset(curX, curY)
                        )
                    } else if (p.isRibbon) {
                        drawRoundRect(
                            color = p.color,
                            topLeft = Offset(curX - p.size / 4f, curY - p.size),
                            size = Size(p.size / 2.5f, p.size * 1.6f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    } else {
                        drawRoundRect(
                            color = p.color,
                            topLeft = Offset(curX - p.size / 2f, curY - p.size / 2f),
                            size = Size(p.size, p.size),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }
        }
    }
}
