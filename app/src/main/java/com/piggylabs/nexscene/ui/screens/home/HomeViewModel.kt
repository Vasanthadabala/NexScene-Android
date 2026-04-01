package com.piggylabs.nexscene.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.model.MovieDto
import com.piggylabs.nexscene.data.model.TvDto
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.UnknownHostException

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
    private companion object {
        const val REQUEST_TIMEOUT_MS = 45_000L
    }

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var sectionRetryJob: Job? = null
    private var homeLoadJob: Job? = null

    init {
        loadHomeContent()
    }

    fun loadPopularMovies() = loadHomeContent()

    fun loadHomeContent(force: Boolean = false) {
        if (homeLoadJob?.isActive == true) return

        val hasBaseData = _uiState.value.movies.isNotEmpty() &&
            _uiState.value.tvShows.isNotEmpty() &&
            _uiState.value.topRatedMovies.isNotEmpty() &&
            _uiState.value.topRatedTvShows.isNotEmpty()
        if (hasBaseData && !force) return

        homeLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val previous = _uiState.value

                // 1) Load trending first
                val moviesResponse = safeMovieCall { repository.getPopularMovies() }
                val tvResponse = safeTvCall { repository.getPopularTvShows() }

                val movies = if (moviesResponse is MovieApiResponse.Success) moviesResponse.data else emptyList()
                val tvShows = if (tvResponse is TvApiResponse.Success) tvResponse.data else emptyList()

                _uiState.value = HomeUiState(
                    isLoading = true,
                    movies = if (movies.isNotEmpty()) movies else previous.movies,
                    tvShows = if (tvShows.isNotEmpty()) tvShows else previous.tvShows,
                    topRatedMovies = previous.topRatedMovies,
                    topRatedTvShows = previous.topRatedTvShows,
                    dramaMovies = previous.dramaMovies,
                    dramaTvShows = previous.dramaTvShows,
                    comedyMovies = previous.comedyMovies,
                    comedyTvShows = previous.comedyTvShows,
                    actionMovies = previous.actionMovies,
                    actionTvShows = previous.actionTvShows,
                    horrorMovies = previous.horrorMovies,
                    horrorTvShows = previous.horrorTvShows,
                    sciFiMovies = previous.sciFiMovies,
                    fantasyMovies = previous.fantasyMovies,
                    fantasyTvShows = previous.fantasyTvShows,
                    error = when {
                        moviesResponse is MovieApiResponse.Error && tvResponse is TvApiResponse.Error ->
                            "${moviesResponse.message}\n${tvResponse.message}"
                        moviesResponse is MovieApiResponse.Error -> moviesResponse.message
                        tvResponse is TvApiResponse.Error -> tvResponse.message
                        else -> null
                    }
                )

                // 2) Then load top-rated
                val topMoviesResponse = safeMovieCall { repository.getTopRatedMovies() }
                val topTvResponse = safeTvCall { repository.getTopRatedTvShows() }
                val topRatedMovies = if (topMoviesResponse is MovieApiResponse.Success) topMoviesResponse.data else emptyList()
                val topRatedTvShows = if (topTvResponse is TvApiResponse.Success) topTvResponse.data else emptyList()
                val afterTrending = _uiState.value

                _uiState.value = HomeUiState(
                    isLoading = false,
                    movies = afterTrending.movies,
                    tvShows = afterTrending.tvShows,
                    topRatedMovies = if (topRatedMovies.isNotEmpty()) topRatedMovies else afterTrending.topRatedMovies,
                    topRatedTvShows = if (topRatedTvShows.isNotEmpty()) topRatedTvShows else afterTrending.topRatedTvShows,
                    dramaMovies = afterTrending.dramaMovies,
                    dramaTvShows = afterTrending.dramaTvShows,
                    comedyMovies = afterTrending.comedyMovies,
                    comedyTvShows = afterTrending.comedyTvShows,
                    actionMovies = afterTrending.actionMovies,
                    actionTvShows = afterTrending.actionTvShows,
                    horrorMovies = afterTrending.horrorMovies,
                    horrorTvShows = afterTrending.horrorTvShows,
                    sciFiMovies = afterTrending.sciFiMovies,
                    fantasyMovies = afterTrending.fantasyMovies,
                    fantasyTvShows = afterTrending.fantasyTvShows,
                    error = when {
                        topMoviesResponse is MovieApiResponse.Error && topTvResponse is TvApiResponse.Error ->
                            "${topMoviesResponse.message}\n${topTvResponse.message}"
                        topMoviesResponse is MovieApiResponse.Error -> topMoviesResponse.message
                        topTvResponse is TvApiResponse.Error -> topTvResponse.message
                        else -> afterTrending.error
                    }
                )

                retryFailedHomeBaseSections(
                    moviesResponse = moviesResponse,
                    tvResponse = tvResponse,
                    topMoviesResponse = topMoviesResponse,
                    topTvResponse = topTvResponse
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load home data"
                )
            } finally {
                homeLoadJob = null
            }
        }
    }

    fun loadMoviesTabContent(force: Boolean = false) {
        val hasMovieTabData = _uiState.value.dramaMovies.isNotEmpty() &&
            _uiState.value.comedyMovies.isNotEmpty() &&
            _uiState.value.actionMovies.isNotEmpty() &&
            _uiState.value.horrorMovies.isNotEmpty() &&
            _uiState.value.sciFiMovies.isNotEmpty() &&
            _uiState.value.fantasyMovies.isNotEmpty()
        if (hasMovieTabData && !force) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // One-by-one order: Drama -> Comedy -> Action -> Horror -> Sci-Fi/Fantasy
                val dramaResponse = safeMovieCall { repository.discoverMoviesByGenre(18) }
                if (dramaResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(dramaMovies = dramaResponse.data, error = null)
                }

                val comedyResponse = safeMovieCall { repository.discoverMoviesByGenre(35) }
                if (comedyResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(comedyMovies = comedyResponse.data, error = null)
                }

                val actionResponse = safeMovieCall { repository.discoverMoviesByGenre(28) }
                if (actionResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(actionMovies = actionResponse.data, error = null)
                }

                val horrorResponse = safeMovieCall { repository.discoverMoviesByGenre(27) }
                if (horrorResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(horrorMovies = horrorResponse.data, error = null)
                }

                val sciFiResponse = safeMovieCall { repository.discoverMoviesByGenre(878) }
                if (sciFiResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(sciFiMovies = sciFiResponse.data, error = null)
                }

                val fantasyMoviesResponse = safeMovieCall { repository.discoverMoviesByGenre(14) }
                if (fantasyMoviesResponse is MovieApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(fantasyMovies = fantasyMoviesResponse.data, error = null)
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

                retryFailedMovieTabSections(
                    dramaResponse = dramaResponse,
                    comedyResponse = comedyResponse,
                    actionResponse = actionResponse,
                    horrorResponse = horrorResponse,
                    sciFiResponse = sciFiResponse,
                    fantasyMoviesResponse = fantasyMoviesResponse
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load movie categories"
                )
            }
        }
    }

    fun loadTvTabContent(force: Boolean = false) {
        val hasTvTabData = _uiState.value.dramaTvShows.isNotEmpty() &&
            _uiState.value.comedyTvShows.isNotEmpty() &&
            _uiState.value.actionTvShows.isNotEmpty() &&
            _uiState.value.horrorTvShows.isNotEmpty() &&
            _uiState.value.fantasyTvShows.isNotEmpty()
        if (hasTvTabData && !force) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // One-by-one order: Drama -> Comedy -> Action -> Horror -> Sci-Fi/Fantasy
                val dramaTvResponse = safeTvCall { repository.discoverTvByGenre(18) }
                if (dramaTvResponse is TvApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(dramaTvShows = dramaTvResponse.data, error = null)
                }

                val comedyTvResponse = safeTvCall { repository.discoverTvByGenre(35) }
                if (comedyTvResponse is TvApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(comedyTvShows = comedyTvResponse.data, error = null)
                }

                val actionTvResponse = safeTvCall { repository.discoverTvByGenre(10759) }
                if (actionTvResponse is TvApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(actionTvShows = actionTvResponse.data, error = null)
                }

                val horrorTvResponse = safeTvCall { repository.discoverTvByGenre(9648) }
                if (horrorTvResponse is TvApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(horrorTvShows = horrorTvResponse.data, error = null)
                }

                val fantasyTvResponse = safeTvCall { repository.discoverTvByGenre(10765) }
                if (fantasyTvResponse is TvApiResponse.Success) {
                    _uiState.value = _uiState.value.copy(fantasyTvShows = fantasyTvResponse.data, error = null)
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

                retryFailedTvTabSections(
                    dramaTvResponse = dramaTvResponse,
                    comedyTvResponse = comedyTvResponse,
                    actionTvResponse = actionTvResponse,
                    horrorTvResponse = horrorTvResponse,
                    fantasyTvResponse = fantasyTvResponse
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load TV categories"
                )
            }
        }
    }

    private suspend fun safeMovieCall(block: suspend () -> MovieApiResponse): MovieApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            MovieApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: UnknownHostException) {
            MovieApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains("Unable to resolve host", ignoreCase = true)) {
                MovieApiResponse.Error("No internet connection. Please check your network.")
            } else {
                MovieApiResponse.Error(message.ifBlank { "Unexpected error" })
            }
        }
    }

    private suspend fun safeTvCall(block: suspend () -> TvApiResponse): TvApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            TvApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: UnknownHostException) {
            TvApiResponse.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains("Unable to resolve host", ignoreCase = true)) {
                TvApiResponse.Error("No internet connection. Please check your network.")
            } else {
                TvApiResponse.Error(message.ifBlank { "Unexpected error" })
            }
        }
    }

    private fun shouldRetryError(message: String): Boolean {
        return !message.contains("No internet connection", ignoreCase = true) &&
            !message.contains("Unable to resolve host", ignoreCase = true)
    }

    private fun retryFailedHomeBaseSections(
        moviesResponse: MovieApiResponse,
        tvResponse: TvApiResponse,
        topMoviesResponse: MovieApiResponse,
        topTvResponse: TvApiResponse
    ) {
        val tasks = mutableListOf<suspend () -> Unit>()
        if (moviesResponse is MovieApiResponse.Error && shouldRetryError(moviesResponse.message)) {
            tasks += {
                when (val retry = safeMovieCall { repository.getPopularMovies() }) {
                    is MovieApiResponse.Success -> _uiState.value = _uiState.value.copy(movies = retry.data, error = null)
                    is MovieApiResponse.Error -> Unit
                }
            }
        }
        if (tvResponse is TvApiResponse.Error && shouldRetryError(tvResponse.message)) {
            tasks += {
                when (val retry = safeTvCall { repository.getPopularTvShows() }) {
                    is TvApiResponse.Success -> _uiState.value = _uiState.value.copy(tvShows = retry.data, error = null)
                    is TvApiResponse.Error -> Unit
                }
            }
        }
        if (topMoviesResponse is MovieApiResponse.Error && shouldRetryError(topMoviesResponse.message)) {
            tasks += {
                when (val retry = safeMovieCall { repository.getTopRatedMovies() }) {
                    is MovieApiResponse.Success -> _uiState.value = _uiState.value.copy(topRatedMovies = retry.data, error = null)
                    is MovieApiResponse.Error -> Unit
                }
            }
        }
        if (topTvResponse is TvApiResponse.Error && shouldRetryError(topTvResponse.message)) {
            tasks += {
                when (val retry = safeTvCall { repository.getTopRatedTvShows() }) {
                    is TvApiResponse.Success -> _uiState.value = _uiState.value.copy(topRatedTvShows = retry.data, error = null)
                    is TvApiResponse.Error -> Unit
                }
            }
        }
        runRetryTasksSequentially(tasks)
    }

    private fun retryFailedMovieTabSections(
        dramaResponse: MovieApiResponse,
        comedyResponse: MovieApiResponse,
        actionResponse: MovieApiResponse,
        horrorResponse: MovieApiResponse,
        sciFiResponse: MovieApiResponse,
        fantasyMoviesResponse: MovieApiResponse
    ) {
        val tasks = mutableListOf<suspend () -> Unit>()
        if (dramaResponse is MovieApiResponse.Error && shouldRetryError(dramaResponse.message)) {
            tasks += { retryGenreMovie(18) { data -> _uiState.value = _uiState.value.copy(dramaMovies = data, error = null) } }
        }
        if (comedyResponse is MovieApiResponse.Error && shouldRetryError(comedyResponse.message)) {
            tasks += { retryGenreMovie(35) { data -> _uiState.value = _uiState.value.copy(comedyMovies = data, error = null) } }
        }
        if (actionResponse is MovieApiResponse.Error && shouldRetryError(actionResponse.message)) {
            tasks += { retryGenreMovie(28) { data -> _uiState.value = _uiState.value.copy(actionMovies = data, error = null) } }
        }
        if (horrorResponse is MovieApiResponse.Error && shouldRetryError(horrorResponse.message)) {
            tasks += { retryGenreMovie(27) { data -> _uiState.value = _uiState.value.copy(horrorMovies = data, error = null) } }
        }
        if (sciFiResponse is MovieApiResponse.Error && shouldRetryError(sciFiResponse.message)) {
            tasks += { retryGenreMovie(878) { data -> _uiState.value = _uiState.value.copy(sciFiMovies = data, error = null) } }
        }
        if (fantasyMoviesResponse is MovieApiResponse.Error && shouldRetryError(fantasyMoviesResponse.message)) {
            tasks += { retryGenreMovie(14) { data -> _uiState.value = _uiState.value.copy(fantasyMovies = data, error = null) } }
        }
        runRetryTasksSequentially(tasks)
    }

    private fun retryFailedTvTabSections(
        dramaTvResponse: TvApiResponse,
        comedyTvResponse: TvApiResponse,
        actionTvResponse: TvApiResponse,
        horrorTvResponse: TvApiResponse,
        fantasyTvResponse: TvApiResponse
    ) {
        val tasks = mutableListOf<suspend () -> Unit>()
        if (dramaTvResponse is TvApiResponse.Error && shouldRetryError(dramaTvResponse.message)) {
            tasks += { retryGenreTv(18) { data -> _uiState.value = _uiState.value.copy(dramaTvShows = data, error = null) } }
        }
        if (comedyTvResponse is TvApiResponse.Error && shouldRetryError(comedyTvResponse.message)) {
            tasks += { retryGenreTv(35) { data -> _uiState.value = _uiState.value.copy(comedyTvShows = data, error = null) } }
        }
        if (actionTvResponse is TvApiResponse.Error && shouldRetryError(actionTvResponse.message)) {
            tasks += { retryGenreTv(10759) { data -> _uiState.value = _uiState.value.copy(actionTvShows = data, error = null) } }
        }
        if (horrorTvResponse is TvApiResponse.Error && shouldRetryError(horrorTvResponse.message)) {
            tasks += { retryGenreTv(9648) { data -> _uiState.value = _uiState.value.copy(horrorTvShows = data, error = null) } }
        }
        if (fantasyTvResponse is TvApiResponse.Error && shouldRetryError(fantasyTvResponse.message)) {
            tasks += { retryGenreTv(10765) { data -> _uiState.value = _uiState.value.copy(fantasyTvShows = data, error = null) } }
        }
        runRetryTasksSequentially(tasks)
    }

    private fun runRetryTasksSequentially(tasks: List<suspend () -> Unit>) {
        if (tasks.isEmpty()) return
        sectionRetryJob?.cancel()
        sectionRetryJob = viewModelScope.launch {
            tasks.forEach { task ->
                task()
                delay(450)
            }
        }
    }

    private suspend fun retryGenreMovie(genreId: Int, onSuccess: (List<MovieDto>) -> Unit) {
        when (val retry = safeMovieCall { repository.discoverMoviesByGenre(genreId) }) {
            is MovieApiResponse.Success -> onSuccess(retry.data)
            is MovieApiResponse.Error -> Unit
        }
    }

    private suspend fun retryGenreTv(genreId: Int, onSuccess: (List<TvDto>) -> Unit) {
        when (val retry = safeTvCall { repository.discoverTvByGenre(genreId) }) {
            is TvApiResponse.Success -> onSuccess(retry.data)
            is TvApiResponse.Error -> Unit
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
