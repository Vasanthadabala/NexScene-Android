package com.piggylabs.nexscene.data.api

import android.util.Log
import com.piggylabs.nexscene.BuildConfig
import com.piggylabs.nexscene.data.model.CastDto
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.CreditsResponse
import com.piggylabs.nexscene.data.model.MovieDto
import com.piggylabs.nexscene.data.model.MovieResponse
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.model.TvDto
import com.piggylabs.nexscene.data.model.TvResponse
import com.piggylabs.nexscene.data.model.VideoResponse
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
    data class Success(val videoKey: String) : TrailerApiResponse()
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

object MovieApi {
    private val client = KtorClientProvider.client
    private val json = KtorClientProvider.json

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

    fun posterUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return "$POSTER_BASE_URL$path"
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

    suspend fun getMovieTrailer(movieId: Int): TrailerApiResponse {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) {
            return TrailerApiResponse.Error("TMDB API key missing. Add TMDB_API_KEY in local.properties")
        }

        return try {
            val response = client.get("${BASE_URL}movie/$movieId/videos") {
                parameter("api_key", apiKey)
            }

            val body = response.bodyAsText()
            if (response.status == HttpStatusCode.OK) {
                val parsed = json.decodeFromString<VideoResponse>(body)
                val trailer = parsed.results.firstOrNull {
                    it.type == "Trailer" && it.site == "YouTube"
                }

                if (trailer != null) {
                    TrailerApiResponse.Success(trailer.key)
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
}
