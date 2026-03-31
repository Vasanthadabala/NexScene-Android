package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.Notification
import com.piggylabs.nexscene.navigation.Profile
import com.piggylabs.nexscene.ui.screens.notification.NotificationScreen
import com.piggylabs.nexscene.ui.screens.profile.ProfileScreen
import com.piggylabs.nexscene.ui.screens.title_details.TitleDetailsScreen


@ExperimentalMaterial3Api
fun NavGraphBuilder.notificationScreenGraph(navController: NavHostController) {

    composable(
        route = Notification.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) {
        NotificationScreen(navController)
    }
}