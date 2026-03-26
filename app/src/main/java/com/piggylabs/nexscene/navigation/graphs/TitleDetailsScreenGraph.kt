package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.ui.screens.title_details.TitleDetailsScreen
import com.piggylabs.nexscene.ui.screens.title_details.TitleItemDetails


@ExperimentalMaterial3Api
fun NavGraphBuilder.titleDetailsScreenGraph(navController: NavHostController) {

    composable(
        route = TitleDetails.routeWithArgs,
        arguments = listOf(
            navArgument(TitleDetails.titleArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.subtitleArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.ratingArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.overviewArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.posterUrlArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.mediaTypeArg) { type = NavType.StringType; defaultValue = "" },
            navArgument(TitleDetails.itemIdArg) { type = NavType.IntType; defaultValue = 0 }
        ),
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
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val details = TitleItemDetails(
            id = args?.getInt(TitleDetails.itemIdArg) ?: 0,
            title = args?.getString(TitleDetails.titleArg).orEmpty(),
            subtitle = args?.getString(TitleDetails.subtitleArg).orEmpty(),
            rating = args?.getString(TitleDetails.ratingArg).orEmpty(),
            overview = args?.getString(TitleDetails.overviewArg).orEmpty(),
            posterUrl = args?.getString(TitleDetails.posterUrlArg).orEmpty().ifBlank { null },
            mediaType = args?.getString(TitleDetails.mediaTypeArg).orEmpty()
        )
        TitleDetailsScreen(navController, details)
    }
}
