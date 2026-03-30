package com.piggylabs.nexscene.data.api

import android.util.Log
import com.piggylabs.nexscene.BuildConfig
import com.piggylabs.nexscene.data.model.CastDto
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.CreditsResponse
import com.piggylabs.nexscene.data.model.MovieDto
import com.piggylabs.nexscene.data.model.MovieDetailsDto
import com.piggylabs.nexscene.data.model.MovieResponse
import com.piggylabs.nexscene.data.model.ProviderInfoDto
import com.piggylabs.nexscene.data.model.TitleDetailsDto
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.model.TitleWatchProvidersDto
import com.piggylabs.nexscene.data.model.TvDto
import com.piggylabs.nexscene.data.model.TvDetailsDto
import com.piggylabs.nexscene.data.model.TvResponse
import com.piggylabs.nexscene.data.model.VideoResponse
import com.piggylabs.nexscene.data.model.WatchProvidersResponse
import com.piggylabs.nexscene.data.remote.KtorClientProvider
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

sealed class MovieApiResponse {
    data class Success(val data: List<MovieDto>) : MovieApiResponse()
    data class Error(val message: String) : MovieApiResponse()
}

sealed class TrailerApiResponse {
    data class Success(
        val videoId: String,
        val youtubeUrl: String
    ) : TrailerApiResponse()
    data class Error(val message: String) : TrailerApiResponse()
}

sealed class TvApiResponse {
    data class Success(val data: List<TvDto>) : TvApiResponse()
    data class Error(val message: String) : TvApiResponse()
}

sealed class CastApiResponse {
    data class Success(val data: List<CastPerson>) : CastApiResponse()
    data class Error(val message: String) : CastApiResponse()
}

sealed class SimilarApiResponse {
    data class Success(val data: List<TitleCardDto>) : SimilarApiResponse()
    data class Error(val message: String) : SimilarApiResponse()
}

sealed class TitleDetailsApiResponse {
    data class Success(val data: TitleDetailsDto) : TitleDetailsApiResponse()
    data class Error(val message: String) : TitleDetailsApiResponse()
}

sealed class ProvidersApiResponse {
    data class Success(val data: TitleWatchProvidersDto) : ProvidersApiResponse()
    data class Error(val message: String) : ProvidersApiResponse()
}

object MovieApi {
    private val client = KtorClientProvider.client
    private val json = KtorClientProvider.json

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/original"
    private const val LOGO_BASE_URL = "https://image.tmdb.org/t/p/w500"

    fun posterUrl(path: String?): String? {
        val safePath = path?.trim().orEmpty()
        Log.d("TMDB_IMAGE", "Original path: $safePath")

        if (safePath.isBlank()) return null
        if (safePath.startsWith("http://") || safePath.startsWith("https://")) return safePath

        val normalizedPath = if (safePath.startsWith("/")) safePath else "/$safePath"
        val url = "$POSTER_BASE_URL$normalizedPath"
        Log.d("TMDB_IMAGE", "Final poster URL: $url")
        return url
    }

    suspend fun getPopularMovies(): MovieApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return MovieApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}movie/popular") {
                parameter("api_key", apiKey)
            }

            val body = response.bodyAsText()
            Log.d("APi", body)
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<MovieResponse>(body)
                MovieApiResponse.Success(parsed.results)
            } else {
                MovieApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            MovieApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun searchMovies(query: String): MovieApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return MovieApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}search/movie") {
                parameter("api_key", apiKey)
                parameter("query", query)
            }

            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<MovieResponse>(body)
                MovieApiResponse.Success(parsed.results)
            } else {
                MovieApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            MovieApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun searchTvShows(query: String): TvApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TvApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}search/tv") {
                parameter("api_key", apiKey)
                parameter("query", query)
            }

            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<TvResponse>(body)
                TvApiResponse.Success(parsed.results)
            } else {
                TvApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TvApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getPopularTvShows(): TvApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TvApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}tv/popular") {
                parameter("api_key", apiKey)
            }

            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<TvResponse>(body)
                TvApiResponse.Success(parsed.results)
            } else {
                TvApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TvApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getTopRatedMovies(): MovieApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return MovieApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}movie/top_rated") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<MovieResponse>(body)
                MovieApiResponse.Success(parsed.results)
            } else {
                MovieApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            MovieApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getTopRatedTvShows(): TvApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TvApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}tv/top_rated") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<TvResponse>(body)
                TvApiResponse.Success(parsed.results)
            } else {
                TvApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TvApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun discoverMoviesByGenre(genreId: Int): MovieApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return MovieApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}discover/movie") {
                parameter("api_key", apiKey)
                parameter("with_genres", genreId)
                parameter("sort_by", "popularity.desc")
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<MovieResponse>(body)
                MovieApiResponse.Success(parsed.results)
            } else {
                MovieApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            MovieApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun discoverTvByGenre(genreId: Int): TvApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TvApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}discover/tv") {
                parameter("api_key", apiKey)
                parameter("with_genres", genreId)
                parameter("sort_by", "popularity.desc")
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<TvResponse>(body)
                TvApiResponse.Success(parsed.results)
            } else {
                TvApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TvApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getTrailer(itemId: Int, mediaType: String): TrailerApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TrailerApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }
        val safeMediaType = if (mediaType.equals("tv", ignoreCase = true)) "tv" else "movie"

        return try {
            val response = client.get("${BASE_URL}$safeMediaType/$itemId/videos") {
                parameter("api_key", apiKey)
            }

            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<VideoResponse>(body)
                val trailer = parsed.results.firstOrNull {
                    it.type == "Trailer" && it.site == "YouTube"
                }

                if (trailer != null) {
                    TrailerApiResponse.Success(
                        videoId = trailer.key,
                        youtubeUrl = "https://www.youtube.com/watch?v=${trailer.key}"
                    )
                } else {
                    TrailerApiResponse.Error("No trailer found")
                }
            } else {
                TrailerApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TrailerApiResponse.Error(e.message ?: "Error")
        }
    }

    suspend fun getMovieTrailer(movieId: Int): TrailerApiResponse =
        getTrailer(itemId = movieId, mediaType = "movie")

    suspend fun getLeadingCast(itemId: Int, mediaType: String): CastApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) return CastApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        val safeMediaType = if (mediaType.equals("tv", ignoreCase = true)) "tv" else "movie"

        return try {
            val response = client.get("${BASE_URL}$safeMediaType/$itemId/credits") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<CreditsResponse>(body)
                val people = parsed.cast.take(10).map { cast ->
                    CastPerson(
                        id = cast.id,
                        name = cast.name,
                        role = cast.character.orEmpty().ifBlank { "Cast" },
                        profileUrl = posterUrl(cast.profile_path)
                    )
                }
                CastApiResponse.Success(people)
            } else {
                CastApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            CastApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getSimilarTitles(itemId: Int, mediaType: String): SimilarApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) return SimilarApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        val safeMediaType = if (mediaType.equals("tv", ignoreCase = true)) "tv" else "movie"

        return try {
            val response = client.get("${BASE_URL}$safeMediaType/$itemId/similar") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val cards = if (safeMediaType == "movie") {
                    val parsed = json.decodeFromString<MovieResponse>(body)
                    parsed.results.take(12).map { movie ->
                        TitleCardDto(
                            id = movie.id,
                            title = movie.title,
                            subtitle = "Movie",
                            rating = String.format("%.1f", movie.vote_average),
                            posterUrl = posterUrl(movie.poster_path),
                            overview = movie.overview,
                            mediaType = "movie"
                        )
                    }
                } else {
                    val parsed = json.decodeFromString<TvResponse>(body)
                    parsed.results.take(12).map { tv ->
                        TitleCardDto(
                            id = tv.id,
                            title = tv.name,
                            subtitle = "TV Show",
                            rating = String.format("%.1f", tv.vote_average),
                            posterUrl = posterUrl(tv.poster_path),
                            overview = tv.overview,
                            mediaType = "tv"
                        )
                    }
                }
                SimilarApiResponse.Success(cards)
            } else {
                SimilarApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            SimilarApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getTitleDetails(itemId: Int, mediaType: String): TitleDetailsApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) return TitleDetailsApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        val safeMediaType = if (mediaType.equals("tv", ignoreCase = true)) "tv" else "movie"

        return try {
            val response = client.get("${BASE_URL}$safeMediaType/$itemId") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val data = if (safeMediaType == "movie") {
                    val details = json.decodeFromString<MovieDetailsDto>(body)
                    TitleDetailsDto(
                        id = details.id,
                        title = details.title,
                        overview = details.overview,
                        tagline = details.tagline.orEmpty(),
                        posterUrl = posterUrl(details.poster_path),
                        rating = String.format("%.1f", details.vote_average),
                        releaseDate = details.release_date.orEmpty(),
                        status = details.status.orEmpty(),
                        genres = details.genres.map { it.name },
                        language = details.spoken_languages.firstOrNull()?.english_name
                            ?: details.original_language.orEmpty(),
                        countries = details.production_countries.mapNotNull { it.name },
                        runtimeLabel = details.runtime?.let { "$it min" }.orEmpty()
                    )
                } else {
                    val details = json.decodeFromString<TvDetailsDto>(body)
                    val seasons = details.number_of_seasons ?: 0
                    val episodes = details.number_of_episodes ?: 0
                    val runtimeLabel = when {
                        seasons > 0 && episodes > 0 -> "$seasons seasons • $episodes episodes"
                        seasons > 0 -> "$seasons seasons"
                        episodes > 0 -> "$episodes episodes"
                        else -> ""
                    }
                    TitleDetailsDto(
                        id = details.id,
                        title = details.name,
                        overview = details.overview,
                        tagline = details.tagline.orEmpty(),
                        posterUrl = posterUrl(details.poster_path),
                        rating = String.format("%.1f", details.vote_average),
                        releaseDate = details.first_air_date.orEmpty(),
                        status = details.status.orEmpty(),
                        genres = details.genres.map { it.name },
                        language = details.spoken_languages.firstOrNull()?.english_name
                            ?: details.original_language.orEmpty(),
                        countries = details.production_countries.mapNotNull { it.name },
                        runtimeLabel = runtimeLabel
                    )
                }
                TitleDetailsApiResponse.Success(data)
            } else {
                TitleDetailsApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            TitleDetailsApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun getWatchProviders(
        itemId: Int,
        mediaType: String,
        countryCode: String
    ): ProvidersApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return ProvidersApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }
        val safeMediaType = if (mediaType.equals("tv", ignoreCase = true)) "tv" else "movie"
        val requestedCountry = countryCode.uppercase().ifBlank { "US" }

        return try {
            val response = client.get("${BASE_URL}$safeMediaType/$itemId/watch/providers") {
                parameter("api_key", apiKey)
            }
            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<WatchProvidersResponse>(body)
                if (parsed.results.isEmpty()) {
                    return ProvidersApiResponse.Error("No providers found")
                }

                val selectedCountryCode = when {
                    parsed.results.containsKey(requestedCountry) -> requestedCountry
                    parsed.results.containsKey("US") -> "US"
                    else -> parsed.results.keys.first()
                }

                val countryData = parsed.results[selectedCountryCode]
                    ?: return ProvidersApiResponse.Error("No providers found")

                fun mapProviders(list: List<com.piggylabs.nexscene.data.model.ProviderItemDto>): List<ProviderInfoDto> {
                    return list
                        .sortedBy { it.display_priority }
                        .distinctBy { it.provider_id }
                        .map {
                            ProviderInfoDto(
                                id = it.provider_id,
                                name = it.provider_name,
                                logoUrl = posterUrl(it.logo_path)?.replace(POSTER_BASE_URL, LOGO_BASE_URL)
                            )
                        }
                }

                ProvidersApiResponse.Success(
                    TitleWatchProvidersDto(
                        countryCode = selectedCountryCode,
                        countryLink = countryData.link,
                        watch = mapProviders(countryData.flatrate),
                        buy = mapProviders(countryData.buy),
                        rent = mapProviders(countryData.rent)
                    )
                )
            } else {
                ProvidersApiResponse.Error("Error: ${response.status}")
            }
        } catch (e: Exception) {
            ProvidersApiResponse.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }
}
