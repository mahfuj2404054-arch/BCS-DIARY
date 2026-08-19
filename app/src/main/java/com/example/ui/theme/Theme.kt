package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

fun createAppColorScheme(themeColors: AppThemeColors): ColorScheme {
    return if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primary,
            onPrimary = Color(0xFF1E0A3C),
            primaryContainer = themeColors.surfaceVariant,
            onPrimaryContainer = themeColors.textPrimary,
            inversePrimary = themeColors.primaryVariant,
            secondary = themeColors.secondary,
            onSecondary = Color(0xFF380020),
            secondaryContainer = themeColors.pillBg,
            onSecondaryContainer = themeColors.textPrimary,
            tertiary = themeColors.accent,
            onTertiary = Color(0xFF1B0B2E),
            tertiaryContainer = themeColors.surfaceElevated,
            onTertiaryContainer = themeColors.textPrimary,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceBright = themeColors.surfaceElevated,
            surfaceDim = themeColors.background,
            surfaceVariant = themeColors.surfaceVariant,
            onSurfaceVariant = themeColors.textSecondary,
            surfaceContainerLowest = themeColors.background,
            surfaceContainerLow = themeColors.backgroundSecondary,
            surfaceContainer = themeColors.surface,
            surfaceContainerHigh = themeColors.surfaceElevated,
            surfaceContainerHighest = themeColors.surfaceVariant,
            surfaceTint = themeColors.primary,
            inverseSurface = Color(0xFFFAF5FF),
            inverseOnSurface = Color(0xFF1A102A),
            outline = themeColors.border,
            outlineVariant = themeColors.borderSubtle,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer
        )
    } else {
        lightColorScheme(
            primary = themeColors.primary,
            onPrimary = Color.White,
            primaryContainer = themeColors.pillBg,
            onPrimaryContainer = themeColors.textPrimary,
            inversePrimary = themeColors.primaryVariant,
            secondary = themeColors.secondary,
            onSecondary = Color.White,
            secondaryContainer = themeColors.pillBg,
            onSecondaryContainer = themeColors.textPrimary,
            tertiary = themeColors.primaryVariant,
            onTertiary = Color.White,
            tertiaryContainer = themeColors.pillBg,
            onTertiaryContainer = themeColors.textPrimary,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceBright = themeColors.surfaceElevated,
            surfaceDim = themeColors.backgroundSecondary,
            surfaceVariant = themeColors.surfaceVariant,
            onSurfaceVariant = themeColors.textSecondary,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = themeColors.surfaceElevated,
            surfaceContainer = themeColors.surface,
            surfaceContainerHigh = themeColors.surfaceVariant,
            surfaceContainerHighest = themeColors.pillBg,
            surfaceTint = themeColors.primary,
            inverseSurface = Color(0xFF1E1238),
            inverseOnSurface = Color(0xFFFAF5FF),
            outline = themeColors.border,
            outlineVariant = themeColors.borderSubtle,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer
        )
    }
}

@Composable
fun MyApplicationTheme(
    themeStyle: ThemeStyle = ThemeStyle.LAVENDER_VIOLET,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // If user explicitly selects Cyber Velvet, it is dark by design.
    // If user is in another theme and triggers dark mode, we adapt.
    val effectiveColors = if (darkTheme && themeStyle != ThemeStyle.CYBER_VELVET) {
        CyberVelvetColors
    } else {
        getThemeColors(themeStyle)
    }

    val colorScheme = createAppColorScheme(effectiveColors)

    CompositionLocalProvider(LocalAppTheme provides effectiveColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
