package com.mymoss.learnlist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A63),
    onPrimary = Color.White,
    secondary = Color(0xFF4F635F),
    tertiary = Color(0xFF4F5F7A),
    background = Color(0xFFF7FBF9),
    surface = Color(0xFFF7FBF9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64D8CB),
    secondary = Color(0xFFB3CCC7),
    tertiary = Color(0xFFB9C7E6),
)

@Composable
fun LearnListTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
