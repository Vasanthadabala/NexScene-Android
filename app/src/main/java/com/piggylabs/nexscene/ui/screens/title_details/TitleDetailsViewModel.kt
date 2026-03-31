package com.piggylabs.nexscene.ui.screens.title_details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.piggylabs.nexscene.data.local.TitleStateLocalDataSource
import com.piggylabs.nexscene.data.local.TitleUserState
import com.piggylabs.nexscene.data.api.CastApiResponse
import com.piggylabs.nexscene.data.api.ProvidersApiResponse
import com.piggylabs.nexscene.data.api.SimilarApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.CommunityRatingSummary
import com.piggylabs.nexscene.data.model.CommunityReview
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.model.TitleDetailsDto
import com.piggylabs.nexscene.data.model.TitleWatchProvidersDto
import com.piggylabs.nexscene.data.api.TitleDetailsApiResponse
import com.piggylabs.nexscene.data.repository.CommunityReviewRepository
import com.piggylabs.nexscene.data.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class CommunityReviewSort {
    LATEST,
    TOP_RATED,
    LOW_RATED
}

data class TitleDetailsUiState(
    val isLoading: Boolean = false,
    val cast: List<CastPerson> = emptyList(),
    val similar: List<TitleCardDto> = emptyList(),
    val details: TitleDetailsDto? = null,
    val providers: TitleWatchProvidersDto? = null,
    val trailerVideoId: String? = null,
    val trailerUrl: String? = null,
    val communityAverageRating: Double = 0.0,
    val communityRatingCount: Int = 0,
    val communityReviews: List<CommunityReview> = emptyList(),
    val communityReviewSort: CommunityReviewSort = CommunityReviewSort.LATEST,
    val isSubmittingCommunityReview: Boolean = false,
    val communitySubmitStatus: String? = null,
    val currentUserId: String = "",
    val userRating: Int = 0,
    val inWatchlist: Boolean = false,
    val watched: Boolean = false,
    val error: String? = null
)

class TitleDetailsViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val communityRepository: CommunityReviewRepository = CommunityReviewRepository()
) : ViewModel() {
    private companion object {
        const val REQUEST_TIMEOUT_MS = 10_000L
    }

    private val _uiState = MutableStateFlow(TitleDetailsUiState())
    val uiState: StateFlow<TitleDetailsUiState> = _uiState.asStateFlow()
    private var localDataSource: TitleStateLocalDataSource? = null
    private var reviewsListener: ListenerRegistration? = null
    private var summaryListener: ListenerRegistration? = null
    private var latestCommunityReviews: List<CommunityReview> = emptyList()

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
        observeCommunity(itemId = itemId, mediaType = mediaType)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val castDeferred = async { safeCastCall { repository.getLeadingCast(itemId = itemId, mediaType = mediaType) } }
                val similarDeferred = async { safeSimilarCall { repository.getSimilarTitles(itemId = itemId, mediaType = mediaType) } }
                val detailsDeferred = async { safeDetailsCall { repository.getTitleDetails(itemId = itemId, mediaType = mediaType) } }
                val trailerDeferred = async { safeTrailerCall { repository.getTrailer(itemId = itemId, mediaType = mediaType) } }
                val providersDeferred = async {
                    safeProvidersCall {
                        repository.getWatchProviders(
                            itemId = itemId,
                            mediaType = mediaType,
                            countryCode = countryCode
                        )
                    }
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
                    communityAverageRating = _uiState.value.communityAverageRating,
                    communityRatingCount = _uiState.value.communityRatingCount,
                    communityReviews = _uiState.value.communityReviews,
                    communityReviewSort = _uiState.value.communityReviewSort,
                    isSubmittingCommunityReview = _uiState.value.isSubmittingCommunityReview,
                    communitySubmitStatus = _uiState.value.communitySubmitStatus,
                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
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
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load title details"
                )
            }
        }
    }

    fun submitCommunityReview(
        itemId: Int,
        mediaType: String,
        rating: Int,
        comment: String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                communitySubmitStatus = "Sign in to add your review."
            )
            return
        }
        if (itemId == 0 || mediaType.isBlank()) return
        if (rating !in 1..10) {
            _uiState.value = _uiState.value.copy(
                communitySubmitStatus = "Choose a rating between 1 and 10."
            )
            return
        }
        if (comment.trim().isBlank()) {
            _uiState.value = _uiState.value.copy(
                communitySubmitStatus = "Write a short review before posting."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmittingCommunityReview = true,
                communitySubmitStatus = null
            )
            val result = communityRepository.submitOrUpdateReview(
                itemId = itemId,
                mediaType = mediaType,
                userId = user.uid,
                userName = user.displayName.orEmpty().ifBlank { "Anonymous" },
                userPhotoUrl = user.photoUrl?.toString(),
                rating = rating,
                comment = comment
            )

            _uiState.value = _uiState.value.copy(
                isSubmittingCommunityReview = false,
                communitySubmitStatus = if (result.isSuccess) {
                    "Review posted."
                } else {
                    result.exceptionOrNull()?.message ?: "Unable to post review."
                }
            )
        }
    }

    fun clearCommunitySubmitStatus() {
        if (_uiState.value.communitySubmitStatus != null) {
            _uiState.value = _uiState.value.copy(communitySubmitStatus = null)
        }
    }

    fun setCommunityReviewSort(sort: CommunityReviewSort) {
        if (_uiState.value.communityReviewSort == sort) return
        _uiState.value = _uiState.value.copy(
            communityReviewSort = sort,
            communityReviews = sortReviews(latestCommunityReviews, sort)
        )
    }

    fun deleteCommunityReview(itemId: Int, mediaType: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _uiState.value = _uiState.value.copy(
                communitySubmitStatus = "Sign in to manage your review."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmittingCommunityReview = true,
                communitySubmitStatus = null
            )
            val result = communityRepository.deleteReview(
                itemId = itemId,
                mediaType = mediaType,
                userId = user.uid
            )
            _uiState.value = _uiState.value.copy(
                isSubmittingCommunityReview = false,
                communitySubmitStatus = if (result.isSuccess) {
                    "Comment deleted. Rating kept."
                } else {
                    result.exceptionOrNull()?.message ?: "Unable to delete review."
                }
            )
        }
    }

    fun setRating(
        itemId: Int,
        mediaType: String,
        title: String,
        posterUrl: String?,
        rating: Int,
        shouldAutoMarkWatched: Boolean
    ) {
        val local = localDataSource ?: return
        val safeRating = rating.coerceIn(0, 10)
        viewModelScope.launch {
            val old = local.getState(itemId = itemId, mediaType = mediaType)
            val markWatched = old.watched || (shouldAutoMarkWatched && safeRating > 0)
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

            // Keep community rating in sync in real time, even before comment is posted.
            syncCommunityRatingRealtime(
                itemId = itemId,
                mediaType = mediaType,
                rating = safeRating
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

    private fun observeCommunity(itemId: Int, mediaType: String) {
        reviewsListener?.remove()
        summaryListener?.remove()

        reviewsListener = communityRepository.observeReviews(
            itemId = itemId,
            mediaType = mediaType,
            onResult = { reviews ->
                latestCommunityReviews = reviews
                _uiState.value = _uiState.value.copy(
                    communityReviews = sortReviews(reviews, _uiState.value.communityReviewSort),
                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                )
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    error = _uiState.value.error ?: message
                )
            }
        )

        summaryListener = communityRepository.observeSummary(
            itemId = itemId,
            mediaType = mediaType,
            onResult = { summary: CommunityRatingSummary ->
                _uiState.value = _uiState.value.copy(
                    communityAverageRating = summary.averageRating,
                    communityRatingCount = summary.ratingCount
                )
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    error = _uiState.value.error ?: message
                )
            }
        )
    }

    private suspend fun syncCommunityRatingRealtime(
        itemId: Int,
        mediaType: String,
        rating: Int
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (rating !in 1..10) return

        val result = communityRepository.submitOrUpdateReview(
            itemId = itemId,
            mediaType = mediaType,
            userId = user.uid,
            userName = user.displayName.orEmpty().ifBlank { "Anonymous" },
            userPhotoUrl = user.photoUrl?.toString(),
            rating = rating,
            comment = ""
        )

        if (result.isFailure) {
            _uiState.value = _uiState.value.copy(
                communitySubmitStatus = result.exceptionOrNull()?.message ?: "Unable to sync rating."
            )
        }
    }

    private fun sortReviews(
        reviews: List<CommunityReview>,
        sort: CommunityReviewSort
    ): List<CommunityReview> {
        return when (sort) {
            CommunityReviewSort.LATEST -> reviews.sortedByDescending { it.updatedAtMillis }
            CommunityReviewSort.TOP_RATED -> reviews.sortedWith(
                compareByDescending<CommunityReview> { it.rating }
                    .thenByDescending { it.updatedAtMillis }
            )
            CommunityReviewSort.LOW_RATED -> reviews.sortedWith(
                compareBy<CommunityReview> { it.rating }
                    .thenByDescending { it.updatedAtMillis }
            )
        }
    }

    override fun onCleared() {
        reviewsListener?.remove()
        summaryListener?.remove()
        super.onCleared()
    }

    private suspend fun safeCastCall(block: suspend () -> CastApiResponse): CastApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            CastApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CastApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun safeSimilarCall(block: suspend () -> SimilarApiResponse): SimilarApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            SimilarApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SimilarApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun safeDetailsCall(block: suspend () -> TitleDetailsApiResponse): TitleDetailsApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            TitleDetailsApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TitleDetailsApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun safeTrailerCall(block: suspend () -> TrailerApiResponse): TrailerApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            TrailerApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TrailerApiResponse.Error(e.message ?: "Unexpected error")
        }
    }

    private suspend fun safeProvidersCall(block: suspend () -> ProvidersApiResponse): ProvidersApiResponse {
        return try {
            withTimeout(REQUEST_TIMEOUT_MS) { block() }
        } catch (e: TimeoutCancellationException) {
            ProvidersApiResponse.Error("Request timed out. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ProvidersApiResponse.Error(e.message ?: "Unexpected error")
        }
    }
}
