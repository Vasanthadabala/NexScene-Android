package com.piggylabs.nexscene.ui.screens.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class ExploreUiState(
    val isLoading: Boolean = false,
    val items: List<TitleCardDto> = emptyList(),
    val error: String? = null
)

class ExploreViewModel : ViewModel() {
    private companion object {
        const val TARGET_ITEMS = 80
        const val PAGES_TO_LOAD = 3
        const val REQUEST_TIMEOUT_MS = 45_000L
        const val TAG = "EXPLORE_CLOUD"
    }

    private val repository = MovieRepository()
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun load(source: String, mediaType: String, movieGenreId: Int, tvGenreId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val normalizedSource = source.lowercase().ifBlank { "popular" }
                val normalizedMedia = mediaType.lowercase().ifBlank { "mixed" }

                val items = when (normalizedSource) {
                    "popular" -> loadPopular(normalizedMedia)
                    "top_rated" -> loadTopRated(normalizedMedia)
                    else -> loadByGenre(normalizedMedia, movieGenreId, tvGenreId)
                }

                _uiState.value = ExploreUiState(
                    isLoading = false,
                    items = items,
                    error = if (items.isEmpty()) "No titles found." else null
                )
                Log.d(
                    TAG,
                    "Load success source=$normalizedSource media=$normalizedMedia items=${items.size}"
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load titles"
                )
                Log.e(TAG, "Load failed source=$source media=$mediaType message=${e.message}", e)
            }
        }
    }

    private suspend fun loadPopular(mediaType: String): List<TitleCardDto> {
        return when (mediaType) {
            "tv" -> fetchTvPages { page -> repository.getPopularTvShows(page) }
            "mixed" -> mergeMovieAndTv(
                movieResponses = fetchMovieResponses { page -> repository.getPopularMovies(page) },
                tvResponses = fetchTvResponses { page -> repository.getPopularTvShows(page) }
            )
            else -> fetchMoviePages { page -> repository.getPopularMovies(page) }
        }
    }

    private suspend fun loadTopRated(mediaType: String): List<TitleCardDto> {
        return when (mediaType) {
            "tv" -> fetchTvPages { page -> repository.getTopRatedTvShows(page) }
            "mixed" -> mergeMovieAndTv(
                movieResponses = fetchMovieResponses { page -> repository.getTopRatedMovies(page) },
                tvResponses = fetchTvResponses { page -> repository.getTopRatedTvShows(page) }
            )
            else -> fetchMoviePages { page -> repository.getTopRatedMovies(page) }
        }
    }

    private suspend fun loadByGenre(
        mediaType: String,
        movieGenreId: Int,
        tvGenreId: Int
    ): List<TitleCardDto> {
        return when (mediaType) {
            "tv" -> {
                if (tvGenreId == 0) emptyList()
                else fetchTvPages { page -> repository.discoverTvByGenre(tvGenreId, page) }
            }
            "mixed" -> mergeMovieAndTv(
                movieResponses = if (movieGenreId == 0) emptyList()
                else fetchMovieResponses { page -> repository.discoverMoviesByGenre(movieGenreId, page) },
                tvResponses = if (tvGenreId == 0) emptyList()
                else fetchTvResponses { page -> repository.discoverTvByGenre(tvGenreId, page) }
            )
            else -> {
                if (movieGenreId == 0) emptyList()
                else fetchMoviePages { page -> repository.discoverMoviesByGenre(movieGenreId, page) }
            }
        }
    }

    private suspend fun mergeMovieAndTv(
        movieResponses: List<MovieApiResponse>,
        tvResponses: List<TvApiResponse>
    ): List<TitleCardDto> {
        val movies = movieResponses.flatMap(::mapMovieResponse)
        val tvShows = tvResponses.flatMap(::mapTvResponse)
        return (movies + tvShows)
            .distinctBy { "${it.mediaType}-${it.id}" }
            .sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
            .take(TARGET_ITEMS)
    }

    private suspend fun fetchMoviePages(
        fetch: suspend (Int) -> MovieApiResponse
    ): List<TitleCardDto> {
        return fetchMovieResponses(fetch)
            .flatMap(::mapMovieResponse)
            .distinctBy { "${it.mediaType}-${it.id}" }
            .take(TARGET_ITEMS)
    }

    private suspend fun fetchTvPages(
        fetch: suspend (Int) -> TvApiResponse
    ): List<TitleCardDto> {
        return fetchTvResponses(fetch)
            .flatMap(::mapTvResponse)
            .distinctBy { "${it.mediaType}-${it.id}" }
            .take(TARGET_ITEMS)
    }

    private suspend fun fetchMovieResponses(
        fetch: suspend (Int) -> MovieApiResponse
    ): List<MovieApiResponse> {
        val responses = mutableListOf<MovieApiResponse>()
        for (page in 1..PAGES_TO_LOAD) {
            val firstTry = safeMovieCall { fetch(page) }
            if (firstTry is MovieApiResponse.Success) {
                responses += firstTry
            } else {
                delay(500)
                responses += safeMovieCall { fetch(page) }
            }
        }
        return responses
    }

    private suspend fun fetchTvResponses(
        fetch: suspend (Int) -> TvApiResponse
    ): List<TvApiResponse> {
        val responses = mutableListOf<TvApiResponse>()
        for (page in 1..PAGES_TO_LOAD) {
            val firstTry = safeTvCall { fetch(page) }
            if (firstTry is TvApiResponse.Success) {
                responses += firstTry
            } else {
                delay(500)
                responses += safeTvCall { fetch(page) }
            }
        }
        return responses
    }

    private suspend fun safeMovieCall(block: suspend () -> MovieApiResponse): MovieApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            MovieApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MovieApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun safeTvCall(block: suspend () -> TvApiResponse): TvApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            TvApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TvApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private fun mapMovieResponse(response: MovieApiResponse): List<TitleCardDto> {
        val movies = (response as? MovieApiResponse.Success)?.data ?: emptyList()
        return movies.map { movie ->
            TitleCardDto(
                id = movie.id,
                title = movie.title,
                subtitle = "MOVIE",
                rating = String.format("%.1f", movie.vote_average),
                overview = movie.overview,
                posterUrl = repository.posterUrl(movie.poster_path),
                mediaType = "movie"
            )
        }
    }

    private fun mapTvResponse(response: TvApiResponse): List<TitleCardDto> {
        val tvShows = (response as? TvApiResponse.Success)?.data ?: emptyList()
        return tvShows.map { tv ->
            TitleCardDto(
                id = tv.id,
                title = tv.name,
                subtitle = "TV SHOW",
                rating = String.format("%.1f", tv.vote_average),
                overview = tv.overview,
                posterUrl = repository.posterUrl(tv.poster_path),
                mediaType = "tv"
            )
        }
    }
}
