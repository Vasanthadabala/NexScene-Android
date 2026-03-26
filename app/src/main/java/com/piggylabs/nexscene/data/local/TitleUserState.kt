package com.piggylabs.nexscene.data.local

data class TitleUserState(
    val itemId: Int,
    val mediaType: String,
    val title: String = "",
    val posterUrl: String? = null,
    val userRating: Int = 0,
    val inWatchlist: Boolean = false,
    val watched: Boolean = false
)
