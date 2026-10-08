package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color(0xFF031A24),
    primaryContainer = Color(0xFF073642),
    onPrimaryContainer = Color(0xFF80DEEA),
    secondary = CyanBright,
    onSecondary = Color(0xFF00222B),
    secondaryContainer = Color(0xFF133644),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = Color(0xFF38BDF8),
    background = StealthBg,
    onBackground = TextPrimary,
    surface = StealthSurface,
    onSurface = TextPrimary,
    surfaceVariant = StealthSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StealthCardBorder,
    outlineVariant = Color(0xFF24334D),
    error = StatusRed,
    onError = Color.White
)

@Composable
fun NinjaTechnoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
