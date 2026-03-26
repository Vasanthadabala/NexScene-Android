package com.piggylabs.nexscene.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<TitleCardDto> = emptyList(),
    val trending: List<TitleCardDto> = emptyList(),
    val error: String? = null
)

class SearchViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            loadTrending()
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query, error = null)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
                return@launch
            }
            searchMovies(query)
        }
    }

    fun searchNow() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch { searchMovies(query) }
    }

    private suspend fun searchMovies(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val moviesDeferred = viewModelScope.async { repository.searchMovies(query) }
        val tvDeferred = viewModelScope.async { repository.searchTvShows(query) }

        val movieResponse = moviesDeferred.await()
        val tvResponse = tvDeferred.await()

        val movieResults = if (movieResponse is MovieApiResponse.Success) {
            movieResponse.data.map { movie ->
                TitleCardDto(
                    id = movie.id,
                    title = movie.title,
                    subtitle = "Movie",
                    rating = String.format("%.1f", movie.vote_average),
                    posterUrl = repository.posterUrl(movie.poster_path),
                    overview = movie.overview,
                    mediaType = "movie"
                )
            }
        } else emptyList()

        val tvResults = if (tvResponse is TvApiResponse.Success) {
            tvResponse.data.map { tv ->
                TitleCardDto(
                    id = tv.id,
                    title = tv.name,
                    subtitle = "TV Show",
                    rating = String.format("%.1f", tv.vote_average),
                    posterUrl = repository.posterUrl(tv.poster_path),
                    overview = tv.overview,
                    mediaType = "tv"
                )
            }
        } else emptyList()

        val merged = (movieResults + tvResults)
            .sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
            .distinctBy { "${it.mediaType}-${it.id}" }

        val errorMessage = when {
            movieResponse is MovieApiResponse.Error && tvResponse is TvApiResponse.Error ->
                "${movieResponse.message}\n${tvResponse.message}"
            movieResponse is MovieApiResponse.Error -> movieResponse.message
            tvResponse is TvApiResponse.Error -> tvResponse.message
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            results = merged,
            error = errorMessage
        )
    }

    private suspend fun loadTrending() {
        val moviesDeferred = viewModelScope.async { repository.getPopularMovies() }
        val tvDeferred = viewModelScope.async { repository.getPopularTvShows() }

        val movieResponse = moviesDeferred.await()
        val tvResponse = tvDeferred.await()

        val movieResults = if (movieResponse is MovieApiResponse.Success) {
            movieResponse.data.map { movie ->
                TitleCardDto(
                    id = movie.id,
                    title = movie.title,
                    subtitle = "Movie",
                    rating = String.format("%.1f", movie.vote_average),
                    posterUrl = repository.posterUrl(movie.poster_path),
                    overview = movie.overview,
                    mediaType = "movie"
                )
            }
        } else emptyList()

        val tvResults = if (tvResponse is TvApiResponse.Success) {
            tvResponse.data.map { tv ->
                TitleCardDto(
                    id = tv.id,
                    title = tv.name,
                    subtitle = "TV Show",
                    rating = String.format("%.1f", tv.vote_average),
                    posterUrl = repository.posterUrl(tv.poster_path),
                    overview = tv.overview,
                    mediaType = "tv"
                )
            }
        } else emptyList()

        val trending = (movieResults + tvResults)
            .sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
            .distinctBy { "${it.mediaType}-${it.id}" }
            .take(10)

        _uiState.value = _uiState.value.copy(trending = trending)
    }

    fun posterUrl(path: String?): String? = repository.posterUrl(path)
}
