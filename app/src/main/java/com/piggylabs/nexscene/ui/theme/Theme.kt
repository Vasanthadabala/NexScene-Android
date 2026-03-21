package com.piggylabs.nexscene.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    background = LightAppColors.background,
    surface = LightAppColors.background,
    onBackground = LightAppColors.text,
    onSurface = LightAppColors.text,
    primary = LightAppColors.green,
    error = LightAppColors.red
)

private val DarkColorScheme = darkColorScheme(
    background = DarkAppColors.background,
    surface = DarkAppColors.background,
    onBackground = DarkAppColors.text,
    onSurface = DarkAppColors.text,
    primary = DarkAppColors.green,
    error = DarkAppColors.red
)

@Composable
fun NexSceneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun appColors() = LocalAppColors.current