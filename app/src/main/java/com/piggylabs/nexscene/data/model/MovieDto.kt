package com.piggylabs.nexscene.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<MovieDto>
)

@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double
)

@Serializable
data class TvResponse(
    val page: Int,
    val results: List<TvDto>
)

@Serializable
data class TvDto(
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double
)

@Serializable
data class VideoResponse(
    val results: List<VideoDto>
)

@Serializable
data class VideoDto(
    val key: String,
    val type: String,
    val site: String
)

@Serializable
data class CreditsResponse(
    val cast: List<CastDto>
)

@Serializable
data class CastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    val profile_path: String? = null
)

data class CastPerson(
    val id: Int,
    val name: String,
    val role: String,
    val profileUrl: String?
)

data class TitleCardDto(
    val id: Int,
    val title: String,
    val subtitle: String,
    val rating: String,
    val posterUrl: String?,
    val overview: String,
    val mediaType: String
)
