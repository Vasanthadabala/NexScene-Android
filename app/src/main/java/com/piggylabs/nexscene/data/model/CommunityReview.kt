package com.piggylabs.nexscene.data.model

data class CommunityReview(
    val userId: String = "",
    val userName: String = "Anonymous",
    val userPhotoUrl: String? = null,
    val rating: Int = 0,
    val comment: String = "",
    val updatedAtMillis: Long = 0L
)

data class CommunityRatingSummary(
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0
)
