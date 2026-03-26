package com.piggylabs.nexscene.ui.screens.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
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
import androidx.compose.ui.res.painterResource
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
import com.piggylabs.nexscene.R
import com.piggylabs.nexscene.navigation.Home
import com.piggylabs.nexscene.ui.theme.LocalAppColors
import com.piggylabs.nexscene.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreen(navController: NavHostController) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            OnBoardingScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreenComponent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NexScene",
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
                imageVector = Icons.Filled.LocalMovies,
                contentDescription = "Menu",
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
                fontSize = 48.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Cinematic",
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFCFB2FF), Color(0xFFA66AF5))
                    )
                ),
                fontSize = 60.sp,
                lineHeight = 36.sp,
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
                fontSize = 60.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Track your favorites, discover new\nmasterpieces, and find where to\nstream them all in one place.",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(52.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFBF95F4), Color(0xFF8E3EEA))
                        )
                    )
                    .clickable { navController.navigate(Home.route) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = ""
                    )
                }
                Spacer(modifier = Modifier.size(14.dp))
                Text(
                    text = "Continue with Google",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
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
