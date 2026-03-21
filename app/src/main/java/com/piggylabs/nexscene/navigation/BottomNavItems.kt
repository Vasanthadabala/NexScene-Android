package com.piggylabs.nexscene.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route:String
)

val bottomBarItems = listOf(
    BottomNavigationItem(
        title = "Home",
        icon = Icons.Rounded.Home,
        route = Home.route
    ),
    BottomNavigationItem(
        title = "Search",
        icon = Icons.Rounded.Search,
        route = Search.route
    ),
    BottomNavigationItem(
        title = "WishList",
        icon = Icons.Rounded.Bookmark,
        route = WishList.route
    ),
    BottomNavigationItem(
        title = "Settings",
        icon = Icons.Rounded.Settings,
        route = Settings.route
    ),
)