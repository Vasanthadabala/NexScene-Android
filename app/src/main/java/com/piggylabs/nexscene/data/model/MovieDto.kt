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

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class SpokenLanguageDto(
    val english_name: String? = null,
    val iso_639_1: String? = null,
    val name: String? = null
)

@Serializable
data class ProductionCountryDto(
    val iso_3166_1: String? = null,
    val name: String? = null
)

@Serializable
data class MovieDetailsDto(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    val tagline: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double = 0.0,
    val release_date: String? = null,
    val runtime: Int? = null,
    val status: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val spoken_languages: List<SpokenLanguageDto> = emptyList(),
    val production_countries: List<ProductionCountryDto> = emptyList(),
    val original_language: String? = null
)

@Serializable
data class TvDetailsDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    val tagline: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double = 0.0,
    val first_air_date: String? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val status: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val spoken_languages: List<SpokenLanguageDto> = emptyList(),
    val production_countries: List<ProductionCountryDto> = emptyList(),
    val original_language: String? = null
)

data class TitleDetailsDto(
    val id: Int,
    val title: String,
    val overview: String,
    val tagline: String,
    val posterUrl: String?,
    val rating: String,
    val releaseDate: String,
    val status: String,
    val genres: List<String>,
    val language: String,
    val countries: List<String>,
    val runtimeLabel: String
)

@Serializable
data class ProviderItemDto(
    val logo_path: String? = null,
    val provider_id: Int = 0,
    val provider_name: String = "",
    val display_priority: Int = 0
)

@Serializable
data class CountryProvidersDto(
    val link: String = "",
    val flatrate: List<ProviderItemDto> = emptyList(),
    val buy: List<ProviderItemDto> = emptyList(),
    val rent: List<ProviderItemDto> = emptyList()
)

@Serializable
data class WatchProvidersResponse(
    val id: Int = 0,
    val results: Map<String, CountryProvidersDto> = emptyMap()
)

data class ProviderInfoDto(
    val id: Int,
    val name: String,
    val logoUrl: String?
)

data class TitleWatchProvidersDto(
    val countryCode: String,
    val countryLink: String,
    val watch: List<ProviderInfoDto>,
    val buy: List<ProviderInfoDto>,
    val rent: List<ProviderInfoDto>
)
