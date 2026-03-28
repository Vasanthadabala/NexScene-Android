package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.Search
import com.piggylabs.nexscene.ui.screens.search.SearchScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.searchScreenGraph(navController: NavHostController) {
    composable(
        route = Search.route
    ) {
        SearchScreen(navController)
    }
}