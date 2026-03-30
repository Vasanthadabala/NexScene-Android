package com.piggylabs.nexscene.ui.screens.title_details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.local.TitleStateLocalDataSource
import com.piggylabs.nexscene.data.local.TitleUserState
import com.piggylabs.nexscene.data.api.CastApiResponse
import com.piggylabs.nexscene.data.api.ProvidersApiResponse
import com.piggylabs.nexscene.data.api.SimilarApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.model.TitleDetailsDto
import com.piggylabs.nexscene.data.model.TitleWatchProvidersDto
import com.piggylabs.nexscene.data.api.TitleDetailsApiResponse
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TitleDetailsUiState(
    val isLoading: Boolean = false,
    val cast: List<CastPerson> = emptyList(),
    val similar: List<TitleCardDto> = emptyList(),
    val details: TitleDetailsDto? = null,
    val providers: TitleWatchProvidersDto? = null,
    val trailerVideoId: String? = null,
    val trailerUrl: String? = null,
    val userRating: Int = 0,
    val inWatchlist: Boolean = false,
    val watched: Boolean = false,
    val error: String? = null
)

class TitleDetailsViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TitleDetailsUiState())
    val uiState: StateFlow<TitleDetailsUiState> = _uiState.asStateFlow()
    private var localDataSource: TitleStateLocalDataSource? = null

    fun initLocal(context: Context) {
        if (localDataSource == null) {
            localDataSource = TitleStateLocalDataSource(context.applicationContext)
        }
    }

    fun load(
        itemId: Int,
        mediaType: String,
        title: String,
        posterUrl: String?,
        countryCode: String
    ) {
        if (itemId == 0) return
        val local = localDataSource ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val castDeferred = async { repository.getLeadingCast(itemId = itemId, mediaType = mediaType) }
            val similarDeferred = async { repository.getSimilarTitles(itemId = itemId, mediaType = mediaType) }
            val detailsDeferred = async { repository.getTitleDetails(itemId = itemId, mediaType = mediaType) }
            val trailerDeferred = async { repository.getTrailer(itemId = itemId, mediaType = mediaType) }
            val providersDeferred = async {
                repository.getWatchProviders(
                    itemId = itemId,
                    mediaType = mediaType,
                    countryCode = countryCode
                )
            }
            val localDeferred = async { local.getState(itemId = itemId, mediaType = mediaType) }

            val castResponse = castDeferred.await()
            val similarResponse = similarDeferred.await()
            val detailsResponse = detailsDeferred.await()
            val trailerResponse = trailerDeferred.await()
            val providersResponse = providersDeferred.await()
            val savedState = localDeferred.await()

            val cast = if (castResponse is CastApiResponse.Success) castResponse.data else emptyList()
            val similar = if (similarResponse is SimilarApiResponse.Success) similarResponse.data else emptyList()
            val detailsData = (detailsResponse as? TitleDetailsApiResponse.Success)?.data
            val providersData = (providersResponse as? ProvidersApiResponse.Success)?.data
            val trailerVideoId = (trailerResponse as? TrailerApiResponse.Success)?.videoId
            val trailerUrl = (trailerResponse as? TrailerApiResponse.Success)?.youtubeUrl

            val errorMessage = when {
                detailsResponse is TitleDetailsApiResponse.Error ->
                    detailsResponse.message
                castResponse is CastApiResponse.Error &&
                    similarResponse is SimilarApiResponse.Error &&
                    trailerResponse is TrailerApiResponse.Error ->
                    "${castResponse.message}\n${similarResponse.message}\n${trailerResponse.message}"
                castResponse is CastApiResponse.Error && similarResponse is SimilarApiResponse.Error ->
                    "${castResponse.message}\n${similarResponse.message}"
                castResponse is CastApiResponse.Error -> castResponse.message
                similarResponse is SimilarApiResponse.Error -> similarResponse.message
                trailerResponse is TrailerApiResponse.Error -> trailerResponse.message
                providersResponse is ProvidersApiResponse.Error -> providersResponse.message
                else -> null
            }

            _uiState.value = TitleDetailsUiState(
                isLoading = false,
                cast = cast,
                similar = similar,
                details = detailsData,
                providers = providersData,
                trailerVideoId = trailerVideoId,
                trailerUrl = trailerUrl,
                userRating = savedState.userRating,
                inWatchlist = savedState.inWatchlist,
                watched = savedState.watched,
                error = errorMessage
            )

            // ensure title/poster are persisted for this key
            local.upsert(
                savedState.copy(
                    title = title.ifBlank { savedState.title },
                    posterUrl = posterUrl ?: savedState.posterUrl
                )
            )
        }
    }

    fun setRating(itemId: Int, mediaType: String, title: String, posterUrl: String?, rating: Int) {
        val local = localDataSource ?: return
        val safeRating = rating.coerceIn(0, 10)
        viewModelScope.launch {
            val old = local.getState(itemId = itemId, mediaType = mediaType)
            val markWatched = safeRating > 0 || old.watched
            local.upsert(
                old.copy(
                    title = title.ifBlank { old.title },
                    posterUrl = posterUrl ?: old.posterUrl,
                    userRating = safeRating,
                    watched = markWatched
                )
            )
            _uiState.value = _uiState.value.copy(
                userRating = safeRating,
                watched = markWatched
            )
        }
    }

    fun toggleWatchlist(itemId: Int, mediaType: String, title: String, posterUrl: String?) {
        val local = localDataSource ?: return
        viewModelScope.launch {
            val old = local.getState(itemId = itemId, mediaType = mediaType)
            val updated = old.copy(
                title = title.ifBlank { old.title },
                posterUrl = posterUrl ?: old.posterUrl,
                inWatchlist = !old.inWatchlist
            )
            local.upsert(updated)
            _uiState.value = _uiState.value.copy(inWatchlist = updated.inWatchlist)
        }
    }

    fun toggleWatched(itemId: Int, mediaType: String, title: String, posterUrl: String?) {
        val local = localDataSource ?: return
        viewModelScope.launch {
            val old = local.getState(itemId = itemId, mediaType = mediaType)
            val updated = old.copy(
                title = title.ifBlank { old.title },
                posterUrl = posterUrl ?: old.posterUrl,
                watched = !old.watched
            )
            local.upsert(updated)
            _uiState.value = _uiState.value.copy(watched = updated.watched)
        }
    }
}
