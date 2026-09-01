package com.mymoss.learnlist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFFD95C4D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDED7),
    onPrimaryContainer = Color(0xFF3B0A05),
    secondary = Color(0xFF2E7D73),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8EFE9),
    onSecondaryContainer = Color(0xFF073A34),
    tertiary = Color(0xFFB57936),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE7C8),
    onTertiaryContainer = Color(0xFF2D1700),
    background = Color(0xFFF8F5EE),
    onBackground = Color(0xFF232522),
    surface = Color(0xFFFFFCF7),
    onSurface = Color(0xFF232522),
    surfaceVariant = Color(0xFFF0ECE3),
    onSurfaceVariant = Color(0xFF6E6A62),
    outline = Color(0xFFD5CEC2),
    outlineVariant = Color(0xFFE5DED3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF9B8D),
    onPrimary = Color(0xFF5B160E),
    primaryContainer = Color(0xFF7A2F25),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFF8CD2C5),
    onSecondary = Color(0xFF003832),
    secondaryContainer = Color(0xFF155047),
    onSecondaryContainer = Color(0xFFA6F0E3),
    tertiary = Color(0xFFE8B36D),
    onTertiary = Color(0xFF422800),
    tertiaryContainer = Color(0xFF5B410F),
    onTertiaryContainer = Color(0xFFFFDFA8),
    background = Color(0xFF181A18),
    onBackground = Color(0xFFE8E4DC),
    surface = Color(0xFF1D201D),
    onSurface = Color(0xFFE8E4DC),
    surfaceVariant = Color(0xFF302F2A),
    onSurfaceVariant = Color(0xFFC9C5BB),
    outline = Color(0xFF948F85),
    outlineVariant = Color(0xFF4A4842),
)

/** Warm paper, tomato and leaf-green form the visual language of the app. */
private val LearnListTypography = Typography().copy(
    displayLarge = Typography().displayLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    displayMedium = Typography().displayMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
)

private val LearnListShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun LearnListTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = LearnListTypography,
        shapes = LearnListShapes,
        content = content,
    )
}
