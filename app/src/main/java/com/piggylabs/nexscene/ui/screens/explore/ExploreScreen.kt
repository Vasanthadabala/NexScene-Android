package com.piggylabs.nexscene.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(name = "back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            ExploreScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ExploreScreenComponent(navController: NavHostController) {
}