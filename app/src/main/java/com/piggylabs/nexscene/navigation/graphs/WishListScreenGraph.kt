package com.piggylabs.nexscene.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.nexscene.navigation.WishList
import com.piggylabs.nexscene.ui.screens.wishlist.WishListScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.wishListScreenGraph(navController: NavHostController) {
    composable(
        route = WishList.route
    ) {
        WishListScreen(navController)
    }
}