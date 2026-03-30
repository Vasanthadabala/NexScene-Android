package com.piggylabs.nexscene.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
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
    val topRatedMovies: List<MovieDto> = emptyList(),
    val topRatedTvShows: List<TvDto> = emptyList(),
    val dramaMovies: List<MovieDto> = emptyList(),
    val dramaTvShows: List<TvDto> = emptyList(),
    val comedyMovies: List<MovieDto> = emptyList(),
    val comedyTvShows: List<TvDto> = emptyList(),
    val actionMovies: List<MovieDto> = emptyList(),
    val actionTvShows: List<TvDto> = emptyList(),
    val horrorMovies: List<MovieDto> = emptyList(),
    val horrorTvShows: List<TvDto> = emptyList(),
    val sciFiMovies: List<MovieDto> = emptyList(),
    val fantasyMovies: List<MovieDto> = emptyList(),
    val fantasyTvShows: List<TvDto> = emptyList(),
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
            val topMoviesDeferred = async { repository.getTopRatedMovies() }
            val topTvDeferred = async { repository.getTopRatedTvShows() }
            val dramaDeferred = async { repository.discoverMoviesByGenre(18) }
            val dramaTvDeferred = async { repository.discoverTvByGenre(18) }
            val comedyDeferred = async { repository.discoverMoviesByGenre(35) }
            val comedyTvDeferred = async { repository.discoverTvByGenre(35) }
            val actionDeferred = async { repository.discoverMoviesByGenre(28) }
            val actionTvDeferred = async { repository.discoverTvByGenre(10759) }
            val horrorDeferred = async { repository.discoverMoviesByGenre(27) }
            val horrorTvDeferred = async { repository.discoverTvByGenre(9648) }
            val sciFiDeferred = async { repository.discoverMoviesByGenre(878) }
            val fantasyMoviesDeferred = async { repository.discoverMoviesByGenre(14) }
            val fantasyTvDeferred = async { repository.discoverTvByGenre(10765) }

            val moviesResponse = moviesDeferred.await()
            val tvResponse = tvDeferred.await()
            val topMoviesResponse = topMoviesDeferred.await()
            val topTvResponse = topTvDeferred.await()
            val dramaResponse = dramaDeferred.await()
            val dramaTvResponse = dramaTvDeferred.await()
            val comedyResponse = comedyDeferred.await()
            val comedyTvResponse = comedyTvDeferred.await()
            val actionResponse = actionDeferred.await()
            val actionTvResponse = actionTvDeferred.await()
            val horrorResponse = horrorDeferred.await()
            val horrorTvResponse = horrorTvDeferred.await()
            val sciFiResponse = sciFiDeferred.await()
            val fantasyMoviesResponse = fantasyMoviesDeferred.await()
            val fantasyTvResponse = fantasyTvDeferred.await()

            val movies = if (moviesResponse is MovieApiResponse.Success) moviesResponse.data else emptyList()
            val tvShows = if (tvResponse is TvApiResponse.Success) tvResponse.data else emptyList()
            val topRatedMovies = if (topMoviesResponse is MovieApiResponse.Success) topMoviesResponse.data else emptyList()
            val topRatedTvShows = if (topTvResponse is TvApiResponse.Success) topTvResponse.data else emptyList()
            val dramaMovies = if (dramaResponse is MovieApiResponse.Success) dramaResponse.data else emptyList()
            val dramaTvShows = if (dramaTvResponse is TvApiResponse.Success) dramaTvResponse.data else emptyList()
            val comedyMovies = if (comedyResponse is MovieApiResponse.Success) comedyResponse.data else emptyList()
            val comedyTvShows = if (comedyTvResponse is TvApiResponse.Success) comedyTvResponse.data else emptyList()
            val actionMovies = if (actionResponse is MovieApiResponse.Success) actionResponse.data else emptyList()
            val actionTvShows = if (actionTvResponse is TvApiResponse.Success) actionTvResponse.data else emptyList()
            val horrorMovies = if (horrorResponse is MovieApiResponse.Success) horrorResponse.data else emptyList()
            val horrorTvShows = if (horrorTvResponse is TvApiResponse.Success) horrorTvResponse.data else emptyList()
            val sciFiMovies = if (sciFiResponse is MovieApiResponse.Success) sciFiResponse.data else emptyList()
            val fantasyMovies = if (fantasyMoviesResponse is MovieApiResponse.Success) fantasyMoviesResponse.data else emptyList()
            val fantasyTvShows = if (fantasyTvResponse is TvApiResponse.Success) fantasyTvResponse.data else emptyList()

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
                topRatedMovies = topRatedMovies,
                topRatedTvShows = topRatedTvShows,
                dramaMovies = dramaMovies,
                dramaTvShows = dramaTvShows,
                comedyMovies = comedyMovies,
                comedyTvShows = comedyTvShows,
                actionMovies = actionMovies,
                actionTvShows = actionTvShows,
                horrorMovies = horrorMovies,
                horrorTvShows = horrorTvShows,
                sciFiMovies = sciFiMovies,
                fantasyMovies = fantasyMovies,
                fantasyTvShows = fantasyTvShows,
                error = errorMessage
            )
        }
    }

    fun posterUrl(path: String?): String? = repository.posterUrl(path)

    fun fetchTrailer(
        itemId: Int,
        mediaType: String,
        onResult: (videoId: String?, youtubeUrl: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val response = repository.getTrailer(itemId = itemId, mediaType = mediaType)) {
                is TrailerApiResponse.Success -> onResult(response.videoId, response.youtubeUrl)
                is TrailerApiResponse.Error -> onResult(null, null)
            }
        }
    }
}
