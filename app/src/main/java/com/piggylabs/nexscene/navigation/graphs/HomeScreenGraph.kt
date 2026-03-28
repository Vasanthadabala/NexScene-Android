package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.Home
import com.piggylabs.nexscene.ui.screens.home.HomeScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.homeScreenGraph(navController: NavHostController) {
    composable(
        route = Home.route
    ) {
        HomeScreen(navController)
    }
}
