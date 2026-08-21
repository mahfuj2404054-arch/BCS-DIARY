    val cardBorderBrush = remember(isCompleted, colors.border, colors.borderSubtle, colors.primary, colors.secondary) {
        Brush.linearGradient(
            listOf(
                if (isCompleted) colors.border.copy(alpha = 0.4f) else colors.primary.copy(alpha = 0.45f),
                if (isCompleted) colors.borderSubtle.copy(alpha = 0.3f) else colors.secondary.copy(alpha = 0.4f)
            )
        )
    }
