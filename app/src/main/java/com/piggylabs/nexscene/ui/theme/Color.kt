package com.piggylabs.nexscene.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class AppColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
    val cards:Color
)

val LightAppColors = AppColors(
    primary = Color(0XFFFFB800),
    secondary = Color(0XFF927236),
    tertiary = Color(0XFF00D7FE),
    neutral = Color(0xFF000000),
    cards = Color.White.copy(alpha = 0.08f)
)

val DarkAppColors = AppColors(
    primary = Color(0XFFFFB800),
    secondary = Color(0XFF927236),
    tertiary = Color(0XFF00D7FE),
    neutral = Color(0xFF000000),
    cards = Color.White.copy(alpha = 0.08f)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
