package com.piggylabs.nexscene.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf

data class AppColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color
)

val LightAppColors = AppColors(
    primary = Color(0XFFFFB800),
    secondary = Color(0XFF927236),
    tertiary = Color(0XFF00D7FE),
    neutral = Color(0xFF3b3428)
)

val DarkAppColors = AppColors(
    primary = Color(0XFFFFB800),
    secondary = Color(0XFF927236),
    tertiary = Color(0XFF00D7FE),
    neutral = Color(0xFF211B11)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
