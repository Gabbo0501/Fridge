package com.example.fridgeproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FridgeOrange,
    onPrimary = FridgeSurface,
    error = FridgeRed,
    onError = FridgeSurface,

    background = FridgeBackground,
    onBackground = FridgeTextPrimary,

    surface = FridgeSurface,
    onSurface = FridgeTextPrimary,

    surfaceVariant = FridgeSurfaceVariant,
    onSurfaceVariant = FridgeTextMuted,

    outline = FridgeOutline,

    tertiaryContainer = FridgeInfoContainer
)

private val LightColorScheme = lightColorScheme(
    primary = FridgeOrange,
    onPrimary = FridgeSurface,
    error = FridgeRed,
    onError = FridgeSurface,

    background = FridgeBackground,
    onBackground = FridgeTextPrimary,

    surface = FridgeSurface,
    onSurface = FridgeTextPrimary,

    surfaceVariant = FridgeSurfaceVariant,
    onSurfaceVariant = FridgeTextMuted,

    outline = FridgeOutline,

    tertiaryContainer = FridgeInfoContainer

)

@Composable
fun FridgeLab4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}