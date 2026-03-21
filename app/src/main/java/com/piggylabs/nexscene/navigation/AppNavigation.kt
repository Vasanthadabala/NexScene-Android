package com.piggylabs.nexscene.navigation

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.piggylabs.nexscene.navigation.graphs.onBoardingScreenGraph

@ExperimentalMaterial3Api
@Composable
fun AppNavigation(context: Context){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = getStartDestination(context)){

        /* onBoarding */
        onBoardingScreenGraph(navController = navController)

    }
}

fun getStartDestination(context: Context): String {
    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
    return if (isLoggedIn) OnBoarding.route else OnBoarding.route
}