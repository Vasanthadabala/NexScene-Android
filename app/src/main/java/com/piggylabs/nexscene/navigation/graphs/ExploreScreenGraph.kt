package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.Explore
import com.piggylabs.nexscene.ui.screens.explore.ExploreScreen


@ExperimentalMaterial3Api
fun NavGraphBuilder.exploreScreenGraph(navController: NavHostController) {

    composable(
        route = Explore.routeWithArgs,
        arguments = listOf(
            navArgument(Explore.titleArg) { type = NavType.StringType; defaultValue = "Explore" },
            navArgument(Explore.sourceArg) { type = NavType.StringType; defaultValue = "popular" },
            navArgument(Explore.mediaTypeArg) { type = NavType.StringType; defaultValue = "mixed" },
            navArgument(Explore.movieGenreIdArg) { type = NavType.IntType; defaultValue = 0 },
            navArgument(Explore.tvGenreIdArg) { type = NavType.IntType; defaultValue = 0 }
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
    ) {
        val args = it.arguments
        ExploreScreen(
            navController = navController,
            title = args?.getString(Explore.titleArg).orEmpty(),
            source = args?.getString(Explore.sourceArg).orEmpty(),
            mediaType = args?.getString(Explore.mediaTypeArg).orEmpty(),
            movieGenreId = args?.getInt(Explore.movieGenreIdArg) ?: 0,
            tvGenreId = args?.getInt(Explore.tvGenreIdArg) ?: 0
        )
    }
}
