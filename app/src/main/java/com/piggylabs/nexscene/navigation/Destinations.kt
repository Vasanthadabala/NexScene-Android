package com.piggylabs.nexscene.navigation

interface Destinations{
    val route: String
}


object OnBoarding: Destinations{
    override val route = "OnBoarding"
}

object Home: Destinations{
    override val route = "Home"
}

object Search: Destinations{
    override val route = "Search"
}

object WishList: Destinations{
    override val route = "WishList"
}

object Settings: Destinations{
    override val route = "Settings"
}