package com.piggylabs.nexscene.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.ui.theme.LocalAppColors

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreen(navController: NavHostController) {
    val palette = LocalAppColors.current
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.neutral)
                .padding(innerPadding)
        ) {
            OnBoardingScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreenComponent(navController: NavHostController) {
    val palette = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.neutral,
                        palette.primary.copy(alpha = 0.2f),
                        palette.neutral.copy(alpha = 0.98f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x4DA45BFF),
                            Color.Transparent
                        ),
                        center = Offset(360f, 1550f),
                        radius = 700f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x1AF9F9F9),
                            Color.Transparent,
                            Color(0x1AF9F9F9)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 2000f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CINEMA",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFD0B6FF), Color(0xFFA173F3))
                        )
                    ),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp
                )
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White.copy(alpha = 0.62f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlimited",
                    fontSize = 72.sp,
                    lineHeight = 74.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.93f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Cinematic",
                    style = TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFCFB2FF), Color(0xFFA66AF5))
                        )
                    ),
                    fontSize = 72.sp,
                    lineHeight = 74.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Journeys.",
                    style = TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFA66AF5), Color(0xFF7B2CE6))
                        )
                    ),
                    fontSize = 72.sp,
                    lineHeight = 74.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Track your favorites, discover new\nmasterpieces, and find where to\nstream them all in one place.",
                    color = Color.White.copy(alpha = 0.74f),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    lineHeight = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFBF95F4), Color(0xFF8E3EEA))
                            )
                        )
                        .clickable { },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.size(14.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color(0xFF30006A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(palette.neutral.copy(alpha = 0.72f))
                        .border(1.dp, palette.primary.copy(alpha = 0.2f), RoundedCornerShape(38.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = buildTermsText(),
                color = Color.White.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                lineHeight = 25.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

private fun buildTermsText(): AnnotatedString {
    return buildAnnotatedString {
        append("By continuing, you agree to our\n")
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        append("Terms of Service")
        pop()
        append(" & ")
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        append("Privacy Policy")
        pop()
    }
}
