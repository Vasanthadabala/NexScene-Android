package com.piggylabs.nexscene.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background = DarkAppColors.neutral, //Main app background
    surface = DarkAppColors.neutral, // components placed on background
    onBackground = Color.White, //Content color on background
    onSurface = Color.White, // Content color on surface
    primary = DarkAppColors.primary, // Main brand color
    secondary = DarkAppColors.secondary, // Supporting color
    tertiary = DarkAppColors.tertiary, // Extra accent color
    error = DarkAppColors.tertiary //Error state color


)

private val DarkColorScheme = darkColorScheme(
    background = DarkAppColors.neutral, //Main app background
    surface = DarkAppColors.neutral, // components placed on background
    onBackground = Color.White, //Content color on background
    onSurface = Color.White, // Content color on surface
    primary = DarkAppColors.primary, // Main brand color
    secondary = DarkAppColors.secondary, // Supporting color
    tertiary = DarkAppColors.tertiary, // Extra accent color
    error = DarkAppColors.tertiary //Error state color
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
