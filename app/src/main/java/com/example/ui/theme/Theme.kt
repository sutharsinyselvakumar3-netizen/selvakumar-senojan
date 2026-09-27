package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AiCompanionTheme(
    isAutoMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val activeAccent = if (isAutoMode) AutoNavy else ManualGreen
    val activeAccentContainer = if (isAutoMode) AutoNavyLight else ManualGreenLight

    val colorScheme = lightColorScheme(
        primary = activeAccent,
        onPrimary = Color.White,
        primaryContainer = activeAccentContainer,
        onPrimaryContainer = activeAccent,
        secondary = activeAccent,
        onSecondary = Color.White,
        tertiary = StatusWarning,
        background = RoyalWhite,
        onBackground = TextPrimary,
        surface = SurfaceWhite,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceCard,
        onSurfaceVariant = TextSecondary,
        outline = SurfaceCardBorder,
        error = StatusCritical,
        onError = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
