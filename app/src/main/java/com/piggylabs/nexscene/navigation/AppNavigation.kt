package com.piggylabs.nexscene.navigation

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.piggylabs.nexscene.navigation.graphs.homeScreenGraph
import com.piggylabs.nexscene.navigation.graphs.onBoardingScreenGraph
import com.piggylabs.nexscene.navigation.graphs.searchScreenGraph
import com.piggylabs.nexscene.navigation.graphs.settingsScreenGraph
import com.piggylabs.nexscene.navigation.graphs.titleDetailsScreenGraph
import com.piggylabs.nexscene.navigation.graphs.wishListScreenGraph

@ExperimentalMaterial3Api
@Composable
fun AppNavigation(context: Context){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = getStartDestination(context)){

        onBoardingScreenGraph(navController = navController)

        homeScreenGraph(navController = navController)
        titleDetailsScreenGraph(navController = navController)

        searchScreenGraph(navController = navController)

        wishListScreenGraph(navController = navController)

        settingsScreenGraph(navController = navController)

    }
}

fun getStartDestination(context: Context): String {
    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
    return if (isLoggedIn) Home.route else Home.route
}
