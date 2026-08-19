package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// THEME ENUM & PALETTE DEFINITIONS
// Specially curated high-craft Pink, Purple, Pastel & Aesthetic Choices
// =========================================================================

enum class ThemeStyle(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val isDark: Boolean = false
) {
    SAKURA_PINK(
        title = "Sakura Blossom & Strawberry Milk",
        subtitle = "Chic petal pink, strawberry milk & rose gold accents",
        emoji = "🌸"
    ),
    LAVENDER_VIOLET(
        title = "Royal Violet & Dreamy Lilac",
        subtitle = "Dreamy royal violet, wisteria lilac & periwinkle glow",
        emoji = "💜"
    ),
    ORCHID_BERRY(
        title = "Orchid Berry & Electric Plum",
        subtitle = "Luminous orchid magenta, ruby plum & electric purple flair",
        emoji = "🦄"
    ),
    CYBER_VELVET(
        title = "Cyber Velvet & Neon Noir",
        subtitle = "Obsidian night, glowing neon violet & hot pink contours",
        emoji = "🔮",
        isDark = true
    ),
    MATCHA_CREAM(
        title = "Matcha Latte & Vanilla Cream",
        subtitle = "Cozy pistachio sage, warm oat milk cream & forest green",
        emoji = "🍵"
    ),
    SUNSET_CORAL(
        title = "Sunset Sorbet & Peach Coral",
        subtitle = "Warm peach nectar, sweet coral blush & golden honey",
        emoji = "🍑"
    )
}

data class AppThemeColors(
    val primary: Color,
    val primaryVariant: Color,
    val primaryGradient: List<Color>,
    val secondary: Color,
    val secondaryVariant: Color,
    val accent: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundGradient: List<Color>,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val pillBg: Color,
    val pillText: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val isDark: Boolean,
    val badgeSuccessBg: Color,
    val badgeSuccessText: Color
)

// 1. Sakura Blossom & Strawberry Milk
val SakuraPinkColors = AppThemeColors(
    primary = Color(0xFFFF5B8B), // Radiant Sakura Rose
    primaryVariant = Color(0xFFFF8DAF),
    primaryGradient = listOf(Color(0xFFFF5B8B), Color(0xFFFF8DAF), Color(0xFFFFB3C6)),
    secondary = Color(0xFFB845C2), // Berry Purple Accent
    secondaryVariant = Color(0xFFD946EF),
    accent = Color(0xFFFFB3C6), // Baby Petal
    background = Color(0xFFFFF4F8), // Soft Milk Blush Canvas
    backgroundSecondary = Color(0xFFFFE8F1),
    backgroundGradient = listOf(Color(0xFFFFF8FB), Color(0xFFFFF0F5), Color(0xFFFFE8F1)),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFDFE),
    surfaceVariant = Color(0xFFFFE5EE),
    border = Color(0xFFFFD6E5),
    borderSubtle = Color(0xFFFFECF3),
    textPrimary = Color(0xFF2D121F), // Deep Rose Espresso
    textSecondary = Color(0xFF78475E),
    textTertiary = Color(0xFFAA7891),
    pillBg = Color(0xFFFFE8F1),
    pillText = Color(0xFFE11D48),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFFFD6E5),
    isDark = false,
    badgeSuccessBg = Color(0xFFD1FAE5),
    badgeSuccessText = Color(0xFF065F46)
)

// 2. Royal Violet & Dreamy Lilac
val LavenderVioletColors = AppThemeColors(
    primary = Color(0xFF8B5CF6), // Royal Violet
    primaryVariant = Color(0xFFA78BFA), // Luminous Lilac
    primaryGradient = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA), Color(0xFFC084FC)),
    secondary = Color(0xFFEC4899), // Neon Pink Accent
    secondaryVariant = Color(0xFFF472B6),
    accent = Color(0xFFC4B5FD), // Soft Lilac Mist
    background = Color(0xFFF7F4FE), // Gentle Violet Silk Canvas
    backgroundSecondary = Color(0xFFEDE8FC),
    backgroundGradient = listOf(Color(0xFFFAF7FF), Color(0xFFF1EBFF), Color(0xFFEDE5FF)),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFAF8FF),
    surfaceVariant = Color(0xFFECE5FB),
    border = Color(0xFFE2D9FC),
    borderSubtle = Color(0xFFEFEAFF),
    textPrimary = Color(0xFF1E1238), // Velvet Violet Charcoal
    textSecondary = Color(0xFF5B4A78),
    textTertiary = Color(0xFF8E7FA8),
    pillBg = Color(0xFFEDE5FF),
    pillText = Color(0xFF7C3AED),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2D9FC),
    isDark = false,
    badgeSuccessBg = Color(0xFFDCFCE7),
    badgeSuccessText = Color(0xFF15803D)
)

// 3. Orchid Berry & Electric Plum
val OrchidBerryColors = AppThemeColors(
    primary = Color(0xFFD946EF), // Luminous Orchid Magenta
    primaryVariant = Color(0xFFF43F5E), // Ruby Berry
    primaryGradient = listOf(Color(0xFFD946EF), Color(0xFFF43F5E), Color(0xFFFB7185)),
    secondary = Color(0xFF8B5CF6), // Royal Purple
    secondaryVariant = Color(0xFFA78BFA),
    accent = Color(0xFFF5D0FE),
    background = Color(0xFFFAF4FC), // Orchid Velvet Cream
    backgroundSecondary = Color(0xFFF5E8FA),
    backgroundGradient = listOf(Color(0xFFFCF7FD), Color(0xFFF7ECFA), Color(0xFFF2E0F7)),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFDF9FE),
    surfaceVariant = Color(0xFFF5E3FA),
    border = Color(0xFFF0D5F7),
    borderSubtle = Color(0xFFF8E8FC),
    textPrimary = Color(0xFF270930), // Deep Amethyst Velvet
    textSecondary = Color(0xFF6B337A),
    textTertiary = Color(0xFF9E68AC),
    pillBg = Color(0xFFF5E6FA),
    pillText = Color(0xFFC026D3),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFF0D5F7),
    isDark = false,
    badgeSuccessBg = Color(0xFFDCFCE7),
    badgeSuccessText = Color(0xFF166534)
)

// 4. Cyber Velvet & Neon Noir (Dark Aesthetic)
val CyberVelvetColors = AppThemeColors(
    primary = Color(0xFFC084FC), // Glowing Neon Violet
    primaryVariant = Color(0xFFF472B6), // Glowing Neon Magenta
    primaryGradient = listOf(Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFF38BDF8)),
    secondary = Color(0xFF38BDF8), // Electric Cyan
    secondaryVariant = Color(0xFF818CF8),
    accent = Color(0xFFF472B6),
    background = Color(0xFF100B1A), // Deep Cyber Obsidian
    backgroundSecondary = Color(0xFF1A122B),
    backgroundGradient = listOf(Color(0xFF140D22), Color(0xFF100B1A), Color(0xFF180F29)),
    surface = Color(0xFF1C1330), // Frosted Velvet Surface
    surfaceElevated = Color(0xFF251940),
    surfaceVariant = Color(0xFF2F2050),
    border = Color(0xFF452E75),
    borderSubtle = Color(0xFF322154),
    textPrimary = Color(0xFFFAF5FF), // Crisp Luminous White-Violet
    textSecondary = Color(0xFFC4B5FD),
    textTertiary = Color(0xFF8B79B0),
    pillBg = Color(0xFF2A1B4A),
    pillText = Color(0xFFE9D5FF),
    cardBackground = Color(0xFF1B122E),
    cardBorder = Color(0xFF422C70),
    isDark = true,
    badgeSuccessBg = Color(0xFF064E3B),
    badgeSuccessText = Color(0xFF6EE7B7)
)

// 5. Matcha Latte & Vanilla Cream
val MatchaCreamColors = AppThemeColors(
    primary = Color(0xFF2E8B57), // Forest Sage
    primaryVariant = Color(0xFF52B788), // Matcha Cream
    primaryGradient = listOf(Color(0xFF2E8B57), Color(0xFF52B788), Color(0xFF74C69D)),
    secondary = Color(0xFFD97706), // Warm Honey
    secondaryVariant = Color(0xFFF59E0B),
    accent = Color(0xFFD8F3DC),
    background = Color(0xFFF4F7F4), // Oat Matcha Canvas
    backgroundSecondary = Color(0xFFE8EFE8),
    backgroundGradient = listOf(Color(0xFFF9FBF9), Color(0xFFF3F7F3), Color(0xFFEAF1EA)),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFBFDFB),
    surfaceVariant = Color(0xFFE5EDE5),
    border = Color(0xFFD0DDD0),
    borderSubtle = Color(0xFFE7EFE7),
    textPrimary = Color(0xFF152A1C), // Deep Forest Noir
    textSecondary = Color(0xFF3D5A45),
    textTertiary = Color(0xFF6E8B76),
    pillBg = Color(0xFFE3EFE5),
    pillText = Color(0xFF1B5E20),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFD0DDD0),
    isDark = false,
    badgeSuccessBg = Color(0xFFD8F3DC),
    badgeSuccessText = Color(0xFF081C15)
)

// 6. Sunset Sorbet & Peach Coral
val SunsetCoralColors = AppThemeColors(
    primary = Color(0xFFFF6F59), // Coral Sunset Nectar
    primaryVariant = Color(0xFFFF9F1C), // Golden Honey Tangerine
    primaryGradient = listOf(Color(0xFFFF6F59), Color(0xFFFF9F1C), Color(0xFFFFBF69)),
    secondary = Color(0xFFE040FB), // Magenta Sunset Flare
    secondaryVariant = Color(0xFFF50057),
    accent = Color(0xFFFFE0D6),
    background = Color(0xFFFFF7F4), // Soft Peach Foam Canvas
    backgroundSecondary = Color(0xFFFFECE5),
    backgroundGradient = listOf(Color(0xFFFFFBF9), Color(0xFFFFF4EE), Color(0xFFFFEBE2)),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFDFD),
    surfaceVariant = Color(0xFFFFE4D9),
    border = Color(0xFFFFD5C4),
    borderSubtle = Color(0xFFFFEDE4),
    textPrimary = Color(0xFF3A1812), // Roasted Cocoa Espresso
    textSecondary = Color(0xFF7D463B),
    textTertiary = Color(0xFFA6766B),
    pillBg = Color(0xFFFFECE3),
    pillText = Color(0xFFD84315),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFFFD5C4),
    isDark = false,
    badgeSuccessBg = Color(0xFFE8F5E9),
    badgeSuccessText = Color(0xFF2E7D32)
)

fun getThemeColors(style: ThemeStyle): AppThemeColors {
    return when (style) {
        ThemeStyle.SAKURA_PINK -> SakuraPinkColors
        ThemeStyle.LAVENDER_VIOLET -> LavenderVioletColors
        ThemeStyle.ORCHID_BERRY -> OrchidBerryColors
        ThemeStyle.CYBER_VELVET -> CyberVelvetColors
        ThemeStyle.MATCHA_CREAM -> MatchaCreamColors
        ThemeStyle.SUNSET_CORAL -> SunsetCoralColors
    }
}

val LocalAppTheme = staticCompositionLocalOf { SakuraPinkColors }

object AppTheme {
    val colors: AppThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTheme.current
}

// Backward-compatible individual tokens
val PookiePinkBackground get() = SakuraPinkColors.background
val PookiePinkLight get() = SakuraPinkColors.pillBg
val PookiePinkMuted get() = SakuraPinkColors.border
val PookiePinkAccent get() = SakuraPinkColors.primaryVariant
val PookiePinkPrimary get() = SakuraPinkColors.primary
val PookiePinkDeep get() = Color(0xFFE04568)
val PookieLilac get() = LavenderVioletColors.accent
val PookieLilacDark get() = LavenderVioletColors.primary
val PookieMint get() = Color(0xFFD5F4E0)
val PookieMintDark get() = Color(0xFF52B788)
val PookieButter get() = Color(0xFFFFF2CE)
val PookieButterDark get() = Color(0xFFE0A928)
val PookieSky get() = Color(0xFFD8ECFF)
val PookieSkyDark get() = Color(0xFF4A90E2)
val PookieIvory get() = Color(0xFFFFFDFC)
val PookieTextPrimary get() = SakuraPinkColors.textPrimary
val PookieTextSecondary get() = SakuraPinkColors.textSecondary
val PookieTextTertiary get() = SakuraPinkColors.textTertiary
val PookieBorder get() = SakuraPinkColors.border

// Light & Dark scheme mappings
val LightPrimary = Color(0xFFFF5B8B)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFE8F1)
val LightOnPrimaryContainer = Color(0xFF5A0B2C)
val LightInversePrimary = Color(0xFFFFB3C6)

val LightSecondary = Color(0xFF8B5CF6)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFEDE8FC)
val LightOnSecondaryContainer = Color(0xFF2E1065)

val LightTertiary = Color(0xFF52B788)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFD5F4E0)
val LightOnTertiaryContainer = Color(0xFF0F4229)

val LightBackground = Color(0xFFFFF4F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceBright = Color(0xFFFFFFFF)
val LightSurfaceDim = Color(0xFFFFF0F5)
val LightSurfaceVariant = Color(0xFFFFE8F1)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFFF8FB)
val LightSurfaceContainer = Color(0xFFFFF0F5)
val LightSurfaceContainerHigh = Color(0xFFFFE8F1)
val LightSurfaceContainerHighest = Color(0xFFFFE0EC)

val LightBorder = Color(0xFFFFD6E5)
val LightBorderSubtle = Color(0xFFFFECF3)

val LightTextPrimary = Color(0xFF2D121F)
val LightTextSecondary = Color(0xFF78475E)
val LightTextTertiary = Color(0xFFAA7891)

val LightError = Color(0xFFE11D48)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFE4E8)
val LightOnErrorContainer = Color(0xFF881337)

// Dark Theme Mappings
val DarkBackground = Color(0xFF100B1A)
val DarkSurface = Color(0xFF1C1330)
val DarkSurfaceBright = Color(0xFF251940)
val DarkSurfaceDim = Color(0xFF0C0814)
val DarkSurfaceVariant = Color(0xFF2A1B48)
val DarkSurfaceContainerLowest = Color(0xFF09060E)
val DarkSurfaceContainerLow = Color(0xFF160E26)
val DarkSurfaceContainer = Color(0xFF1C1330)
val DarkSurfaceContainerHigh = Color(0xFF23183D)
val DarkSurfaceContainerHighest = Color(0xFF2C1E4C)

val DarkBorder = Color(0xFF452E75)
val DarkBorderSubtle = Color(0xFF322154)

val DarkTextPrimary = Color(0xFFFAF5FF)
val DarkTextSecondary = Color(0xFFC4B5FD)
val DarkTextTertiary = Color(0xFF8B79B0)

val DarkPrimary = Color(0xFFC084FC)
val DarkOnPrimary = Color(0xFF2E1065)
val DarkPrimaryContainer = Color(0xFF4C1D95)
val DarkOnPrimaryContainer = Color(0xFFF5F3FF)
val DarkInversePrimary = Color(0xFF8B5CF6)

val DarkSecondary = Color(0xFFF472B6)
val DarkOnSecondary = Color(0xFF500724)
val DarkSecondaryContainer = Color(0xFF831843)
val DarkOnSecondaryContainer = Color(0xFFFDF2F8)

val DarkTertiary = Color(0xFF80D4A8)
val DarkOnTertiary = Color(0xFF003820)
val DarkTertiaryContainer = Color(0xFF1D5A3A)
val DarkOnTertiaryContainer = Color(0xFFD5F4E0)

val DarkError = Color(0xFFFB7185)
val DarkOnError = Color(0xFF4C0519)
val DarkErrorContainer = Color(0xFF881337)
val DarkOnErrorContainer = Color(0xFFFFE4E8)

// Google Drive Colors
val DriveBlue = Color(0xFF4285F4)
val DriveGreen = Color(0xFF34A853)
val DriveYellow = Color(0xFFFBBC05)
val DriveRed = Color(0xFFEA4335)
