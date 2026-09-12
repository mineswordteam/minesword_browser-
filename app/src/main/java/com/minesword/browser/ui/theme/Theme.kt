package com.minesword.browser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyanPrimary = Color(0xFF00E5FF)
val CyanVariant = Color(0xFF00B0FF)
val DarkBackground = Color(0xFF121824)
val DarkSurface = Color(0xFF1E2638)
val LightBackground = Color(0xFFF5F7FA)
val LightSurface = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    secondary = CyanVariant,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00838F),
    secondary = Color(0xFF00ACC1),
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun MineswordBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
