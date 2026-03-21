package com.piggylabs.nexscene.navigation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun TopBar(name:String,navController: NavHostController) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appColors().background
        ),
        title = {
            Text(
                text = name,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
                color = Color(0xFF27C152),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    tint = Color(0xFF27C152),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}