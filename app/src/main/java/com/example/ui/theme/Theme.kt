package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangeFlame,
    onPrimary = Color.White,
    secondary = OrangeFlameBright,
    tertiary = GoldPoints,
    background = DarkBackground,
    surface = SurfaceGraphite,
    onBackground = TextLight,
    onSurface = TextLight,
    outline = GrayBorder
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeFlameDark,
    onPrimary = Color.White,
    secondary = OrangeFlame,
    tertiary = GoldPoints,
    background = LightBackground,
    surface = LightSurface,
    onBackground = TextDarkText,
    onSurface = TextDarkText,
    outline = Color(0xFFE0E0E0)
)

@Composable
fun LuncBurnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
