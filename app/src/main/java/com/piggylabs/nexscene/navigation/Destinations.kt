package com.piggylabs.nexscene.navigation

import android.net.Uri

interface Destinations{
    val route: String
}


object OnBoarding: Destinations{
    override val route = "OnBoarding"
}

object Home: Destinations{
    override val route = "Home"
}

object Explore: Destinations{
    override val route = "Explore"

    const val titleArg = "title"
    const val sourceArg = "source"
    const val mediaTypeArg = "mediaType"
    const val movieGenreIdArg = "movieGenreId"
    const val tvGenreIdArg = "tvGenreId"

    val routeWithArgs =
        "$route?$titleArg={$titleArg}&$sourceArg={$sourceArg}&$mediaTypeArg={$mediaTypeArg}&$movieGenreIdArg={$movieGenreIdArg}&$tvGenreIdArg={$tvGenreIdArg}"

    fun createRoute(
        title: String,
        source: String,
        mediaType: String,
        movieGenreId: Int = 0,
        tvGenreId: Int = movieGenreId
    ): String {
        return "$route?$titleArg=${Uri.encode(title)}" +
            "&$sourceArg=${Uri.encode(source)}" +
            "&$mediaTypeArg=${Uri.encode(mediaType)}" +
            "&$movieGenreIdArg=$movieGenreId" +
            "&$tvGenreIdArg=$tvGenreId"
    }
}

object Notification: Destinations{
    override val route = "Notification"
}

object TitleDetails: Destinations{
    override val route = "TitleDetails"

    const val titleArg = "title"
    const val subtitleArg = "subtitle"
    const val ratingArg = "rating"
    const val overviewArg = "overview"
    const val posterUrlArg = "posterUrl"
    const val mediaTypeArg = "mediaType"
    const val itemIdArg = "itemId"

    val routeWithArgs =
        "$route?$titleArg={$titleArg}&$subtitleArg={$subtitleArg}&$ratingArg={$ratingArg}&$overviewArg={$overviewArg}&$posterUrlArg={$posterUrlArg}&$mediaTypeArg={$mediaTypeArg}&$itemIdArg={$itemIdArg}"

    fun createRoute(
        title: String,
        subtitle: String,
        rating: String,
        overview: String,
        posterUrl: String?,
        mediaType: String,
        itemId: Int
    ): String {
        return "$route?$titleArg=${Uri.encode(title)}" +
            "&$subtitleArg=${Uri.encode(subtitle)}" +
            "&$ratingArg=${Uri.encode(rating)}" +
            "&$overviewArg=${Uri.encode(overview)}" +
            "&$posterUrlArg=${Uri.encode(posterUrl ?: "")}" +
            "&$mediaTypeArg=${Uri.encode(mediaType)}" +
            "&$itemIdArg=$itemId"
    }
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

object Profile: Destinations{
    override val route = "Profile"
}
