package com.piggylabs.nexscene.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

data class AppColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
    val background: Color,
    val container: Color,
    val text: Color,
    val red: Color,
    val green: Color
)

val LightAppColors = AppColors(
    primary = Color(0XFF8A2BE2),
    secondary = Color(0XFFFFD700),
    tertiary = Color(0XFF935400),
    neutral = Color(0XFF0F0F0F),
    background = Color.White,
    container = Color.Gray.copy(alpha = 0.15f),
    text = Color.Black,
    red = Color.Red,
    green = Color(0xFF27C152)
)

val DarkAppColors = AppColors(
    primary = Color(0XFF8A2BE2),
    secondary = Color(0XFFFFD700),
    tertiary = Color(0XFF935400),
    neutral = Color(0XFF0F0F0F),
    background = Color.Black,
    container = Color.Gray.copy(alpha = 0.2f),
    text = Color.White,
    red = Color.Red,
    green = Color(0xFF27C152)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
