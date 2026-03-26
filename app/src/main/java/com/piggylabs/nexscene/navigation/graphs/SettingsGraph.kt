package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.Settings
import com.piggylabs.nexscene.ui.screens.settings.SettingsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.settingsScreenGraph(navController: NavHostController) {
    composable(
        route = Settings.route
    ) {
        SettingsScreen(navController)
    }
}