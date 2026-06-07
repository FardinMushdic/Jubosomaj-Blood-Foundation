package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JbfRed,
    secondary = JbfDarkGray,
    tertiary = JbfLightGray,
    background = JbfDeepBlack,
    surface = JbfCharcoal,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = JbfRed,
    secondary = JbfCharcoal,
    tertiary = JbfLightGray,
    background = JbfBackground,
    surface = JbfCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = JbfCharcoal,
    onSurface = JbfCharcoal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom color palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
