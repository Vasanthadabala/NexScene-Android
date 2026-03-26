package com.piggylabs.nexscene.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.ui.theme.LocalAppColors

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(navController: NavHostController) {
    val palette = LocalAppColors.current
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.neutral)
                .padding(innerPadding)
        ) {
            SettingsScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SettingsScreenComponent(navController: NavHostController) {
}