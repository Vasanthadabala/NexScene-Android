package com.piggylabs.nexscene.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.model.MovieDto
import com.piggylabs.nexscene.data.model.TvDto
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val movies: List<MovieDto> = emptyList(),
    val tvShows: List<TvDto> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadPopularMovies() = loadHomeContent()

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val moviesDeferred = async { repository.getPopularMovies() }
            val tvDeferred = async { repository.getPopularTvShows() }

            val moviesResponse = moviesDeferred.await()
            val tvResponse = tvDeferred.await()

            val movies = if (moviesResponse is MovieApiResponse.Success) moviesResponse.data else emptyList()
            val tvShows = if (tvResponse is TvApiResponse.Success) tvResponse.data else emptyList()

            val errorMessage = when {
                moviesResponse is MovieApiResponse.Error && tvResponse is TvApiResponse.Error ->
                    "${moviesResponse.message}\n${tvResponse.message}"
                moviesResponse is MovieApiResponse.Error -> moviesResponse.message
                tvResponse is TvApiResponse.Error -> tvResponse.message
                else -> null
            }

            _uiState.value = HomeUiState(
                isLoading = false,
                movies = movies,
                tvShows = tvShows,
                error = errorMessage
            )
        }
    }

    fun posterUrl(path: String?): String? = repository.posterUrl(path)
}
