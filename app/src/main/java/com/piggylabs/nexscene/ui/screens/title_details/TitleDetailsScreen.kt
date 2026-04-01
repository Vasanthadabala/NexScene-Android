package com.piggylabs.nexscene.ui.screens.title_details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.piggylabs.nexscene.data.local.db.AppDataBase
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.CommunityReview
import com.piggylabs.nexscene.data.model.ProviderInfoDto
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.data.model.TitleDetailsDto
import com.piggylabs.nexscene.data.model.TitleWatchProvidersDto
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.screens.home.limitChars
import com.piggylabs.nexscene.ui.theme.AppColors
import com.piggylabs.nexscene.ui.theme.appColors
import java.util.Locale

data class TitleItemDetails(
    val id: Int = 0,
    val title: String = "",
    val subtitle: String = "",
    val rating: String = "",
    val overview: String = "",
    val posterUrl: String? = null,
    val mediaType: String = ""
)

@ExperimentalMaterial3Api
@Composable
fun TitleDetailsScreen(
    navController: NavHostController,
    details: TitleItemDetails
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val showBadges = prefs.getBoolean("show_watched_badge", true)
    val shouldAutoMarkWatched = prefs.getBoolean("auto_mark_watched", true)
    val watchedItems by AppDataBase.getDatabase(context)
        .titleStateDao()
        .observeWatchedItems()
        .collectAsState(initial = emptyList())
    val watchedKeys = remember(watchedItems) {
        watchedItems.map { "${it.mediaType}-${it.itemId}" }.toSet()
    }
    val viewModel: TitleDetailsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(details.id, details.mediaType) {
        val countryCode = Locale.getDefault().country
        Log.d("Country_code", countryCode)
        viewModel.initLocal(context)
        viewModel.load(
            itemId = details.id,
            mediaType = details.mediaType,
            title = details.title,
            posterUrl = details.posterUrl,
            countryCode = countryCode.ifBlank { "US" }
        )
    }

    val castData = uiState.cast
    val similarData = uiState.similar

    Scaffold(
        topBar = { TopBar(name = "back", navController = navController) }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            TitleDetailsScreenComponent(
                navController = navController,
                details = details,
                titleDetails = uiState.details,
                providers = uiState.providers,
                castData = castData,
                isCastLoading = uiState.isCastLoading,
                similarData = similarData,
                isSimilarLoading = uiState.isSimilarLoading,
                trailerVideoId = uiState.trailerVideoId,
                trailerUrl = uiState.trailerUrl,
                communityAverageRating = uiState.communityAverageRating,
                communityRatingCount = uiState.communityRatingCount,
                communityReviews = uiState.communityReviews,
                communityReviewSort = uiState.communityReviewSort,
                isSubmittingCommunityReview = uiState.isSubmittingCommunityReview,
                communitySubmitStatus = uiState.communitySubmitStatus,
                currentUserId = uiState.currentUserId,
                userRating = uiState.userRating,
                inWatchlist = uiState.inWatchlist,
                watched = uiState.watched,
                onRate = { rating ->
                    viewModel.setRating(
                        itemId = details.id,
                        mediaType = details.mediaType,
                        title = details.title,
                        posterUrl = details.posterUrl,
                        rating = rating,
                        shouldAutoMarkWatched = shouldAutoMarkWatched
                    )
                },
                onToggleWatchlist = {
                    viewModel.toggleWatchlist(
                        itemId = details.id,
                        mediaType = details.mediaType,
                        title = details.title,
                        posterUrl = details.posterUrl
                    )
                },
                onToggleWatched = {
                    viewModel.toggleWatched(
                        itemId = details.id,
                        mediaType = details.mediaType,
                        title = details.title,
                        posterUrl = details.posterUrl
                    )
                },
                onSubmitCommunityReview = { rating, comment ->
                    viewModel.submitCommunityReview(
                        itemId = details.id,
                        mediaType = details.mediaType,
                        rating = rating,
                        comment = comment
                    )
                },
                onClearCommunityStatus = viewModel::clearCommunitySubmitStatus,
                onDeleteCommunityReview = {
                    viewModel.deleteCommunityReview(
                        itemId = details.id,
                        mediaType = details.mediaType
                    )
                },
                onCommunityReviewSortChanged = viewModel::setCommunityReviewSort,
                watchedKeys = watchedKeys,
                showBadges = showBadges
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TitleDetailsScreenComponent(
    navController: NavHostController,
    details: TitleItemDetails,
    titleDetails: TitleDetailsDto?,
    providers: TitleWatchProvidersDto?,
    castData: List<CastPerson>,
    isCastLoading: Boolean,
    similarData: List<TitleCardDto>,
    isSimilarLoading: Boolean,
    trailerVideoId: String?,
    trailerUrl: String?,
    communityAverageRating: Double,
    communityRatingCount: Int,
    communityReviews: List<CommunityReview>,
    communityReviewSort: CommunityReviewSort,
    isSubmittingCommunityReview: Boolean,
    communitySubmitStatus: String?,
    currentUserId: String,
    userRating: Int,
    inWatchlist: Boolean,
    watched: Boolean,
    onRate: (Int) -> Unit,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit,
    onSubmitCommunityReview: (Int, String) -> Unit,
    onClearCommunityStatus: () -> Unit,
    onDeleteCommunityReview: () -> Unit,
    onCommunityReviewSortChanged: (CommunityReviewSort) -> Unit,
    watchedKeys: Set<String>,
    showBadges: Boolean
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus(force = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                appColors().neutral
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroSection(
                    details = details,
                    titleDetails = titleDetails,
                    trailerVideoId = trailerVideoId,
                    trailerUrl = trailerUrl,
                    communityAverageRating = communityAverageRating,
                    communityRatingCount = communityRatingCount
                )
            }

            item {
                ActionButtons(
                    palette = appColors(),
                    userRating = userRating,
                    inWatchlist = inWatchlist,
                    watched = watched,
                    onRate = onRate,
                    onToggleWatchlist = onToggleWatchlist,
                    onToggleWatched = onToggleWatched
                )
            }

            item {
                PlatformSection(
                    providers = providers,
                    title = titleDetails?.title?.ifBlank { details.title } ?: details.title
                )
            }

            item {
                CastSection(
                    castData = castData,
                    isLoading = isCastLoading
                )
            }

            item {
                CommunityReviewsSection(
                    context = context,
                    reviews = communityReviews,
                    selectedSort = communityReviewSort,
                    currentUserId = currentUserId,
                    currentUserRating = userRating,
                    isSubmitting = isSubmittingCommunityReview,
                    submitStatus = communitySubmitStatus,
                    onSubmit = onSubmitCommunityReview,
                    onClearStatus = onClearCommunityStatus,
                    onDeleteOwnReview = onDeleteCommunityReview,
                    onSortChanged = onCommunityReviewSortChanged
                )
            }

            item {
                SimilarTitlesSection(
                    navController = navController,
                    similarData = similarData,
                    isLoading = isSimilarLoading,
                    watchedKeys = watchedKeys,
                    showBadges = showBadges
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    details: TitleItemDetails,
    titleDetails: TitleDetailsDto?,
    trailerVideoId: String?,
    trailerUrl: String?,
    communityAverageRating: Double,
    communityRatingCount: Int
) {
    val context = LocalContext.current
    val displayTitle = titleDetails?.title?.ifBlank { details.title } ?: details.title
    val displayPosterUrl = titleDetails?.posterUrl ?: details.posterUrl
    val displayRating = titleDetails?.rating?.ifBlank { details.rating } ?: details.rating
    val displayOverview = titleDetails?.overview?.ifBlank { details.overview } ?: details.overview
    val displayTagline = titleDetails?.tagline.orEmpty()
    val releaseDate = titleDetails?.releaseDate.orEmpty()
    val status = titleDetails?.status.orEmpty()
    val runtimeLabel = titleDetails?.runtimeLabel.orEmpty()
    val language = titleDetails?.language.orEmpty()
    val genres = titleDetails?.genres?.joinToString(", ").orEmpty()
    val countries = titleDetails?.countries?.joinToString(", ").orEmpty()

    Column(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(0.dp)
        ) {
            if (!displayPosterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = displayPosterUrl,
                    contentDescription = displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF3D5964),
                                    Color(0xFF1D252C),
                                    Color(0xFF0B0C11)
                                )
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.16f),
                                Color.Black.copy(alpha = 0.34f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    if (details.mediaType.equals(
                            "tv",
                            ignoreCase = true
                        )
                    ) "FEATURE SERIES" else "FEATURE FILM",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = displayTitle.ifBlank { "OPPENHEIMER" },
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                    lineHeight = 32.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (displayTagline.isNotBlank()) {
                    Text(
                        text = "\"$displayTagline\"",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }



                Spacer(modifier = Modifier.height(16.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .clickable {
                        openTrailerInYoutube(
                            context = context,
                            trailerVideoId = trailerVideoId,
                            trailerUrl = trailerUrl
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF3D3200),
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ){

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaItem(details.mediaType.uppercase().ifBlank { "MOVIE" })
                RatingPill(
                    label = "TMDB",
                    value = displayRating.ifBlank { "8.4" },
                    tint = Color(0xFFFFD675)
                )
                RatingPill(
                    label = "USER",
                    value = if (communityRatingCount > 0) {
                        "${String.format("%.1f", communityAverageRating)} (${communityRatingCount})"
                    } else {
                        "No ratings"
                    },
                    tint = Color(0xFF8AE6FF)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (
                releaseDate.isNotBlank() ||
                status.isNotBlank() ||
                runtimeLabel.isNotBlank() ||
                language.isNotBlank() ||
                genres.isNotBlank() ||
                countries.isNotBlank()
            ) {
                Text(
                    "DETAILS",
                    color = appColors().primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (releaseDate.isNotBlank()) {
                    Text("Release: $releaseDate", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                if (status.isNotBlank()) {
                    Text("Status: $status", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                if (runtimeLabel.isNotBlank()) {
                    Text("Runtime: $runtimeLabel", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                if (language.isNotBlank()) {
                    Text("Language: $language", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                if (genres.isNotBlank()) {
                    Text("Genres: $genres", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                if (countries.isNotBlank()) {
                    Text("Countries: $countries", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                "STORYLINE",
                color = appColors().primary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                displayOverview.ifBlank {
                    "Overview not available."
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Read More",
                color = appColors().primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
    }
}

private fun openTrailerInYoutube(
    context: android.content.Context,
    trailerVideoId: String?,
    trailerUrl: String?
) {
    val fallbackUrl = trailerUrl ?: trailerVideoId?.let { "https://www.youtube.com/watch?v=$it" } ?: return
    val appUri = trailerVideoId?.let { Uri.parse("vnd.youtube:$it") }

    try {
        if (appUri != null) {
            context.startActivity(Intent(Intent.ACTION_VIEW, appUri))
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
        }
    } catch (_: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
    }
}

@Composable
private fun MetaItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.65f))
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
    }
}

@Composable
private fun RatingPill(
    label: String,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            "$label $value",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CommunityReviewsSection(
    context: Context,
    reviews: List<CommunityReview>,
    selectedSort: CommunityReviewSort,
    currentUserId: String,
    currentUserRating: Int,
    isSubmitting: Boolean,
    submitStatus: String?,
    onSubmit: (Int, String) -> Unit,
    onClearStatus: () -> Unit,
    onDeleteOwnReview: () -> Unit,
    onSortChanged: (CommunityReviewSort) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var comment by rememberSaveable { mutableStateOf("") }
    var isEditingOwnReview by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(submitStatus) {
        if (submitStatus == "Sign in to add your review.") {
            Toast.makeText(context, submitStatus, Toast.LENGTH_SHORT).show()
            onClearStatus()
        }
        if (submitStatus == "Review posted.") {
            val toastMessage = if (isEditingOwnReview) "Review updated" else "Review posted"
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            comment = ""
            isEditingOwnReview = false
        }
        if (submitStatus == "Review deleted.") {
            Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
            comment = ""
            isEditingOwnReview = false
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "COMMUNITY REVIEWS",
            color = appColors().primary,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = 1.2.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReviewSortChip(
                label = "Latest",
                selected = selectedSort == CommunityReviewSort.LATEST,
                onClick = { onSortChanged(CommunityReviewSort.LATEST) }
            )
            ReviewSortChip(
                label = "Top Rated",
                selected = selectedSort == CommunityReviewSort.TOP_RATED,
                onClick = { onSortChanged(CommunityReviewSort.TOP_RATED) }
            )
            ReviewSortChip(
                label = "Low Rated",
                selected = selectedSort == CommunityReviewSort.LOW_RATED,
                onClick = { onSortChanged(CommunityReviewSort.LOW_RATED) }
            )
        }

        if (isEditingOwnReview) {
            Text(
                "Editing your review",
                color = Color(0xFF8AE6FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = if (currentUserRating > 0) {
                "Your rating: $currentUserRating/10"
            } else {
                "Give Rating first, then post your review."
            },
            color = if (currentUserRating > 0) Color.White.copy(alpha = 0.86f) else Color(0xFFFFB3B3),
            fontSize = 12.sp
        )

        BasicTextField(
            value = comment,
            onValueChange = {
                comment = it.take(500)
                onClearStatus()
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 14.sp
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (comment.isBlank()) {
                    Text(
                        "Share your experience with this title...",
                        color = Color.White.copy(alpha = 0.52f),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${comment.length}/500",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSubmitting) Color(0xFF5B5B5B) else appColors().primary)
                    .clickable(enabled = !isSubmitting) {
                        keyboardController?.hide()
                        onSubmit(currentUserRating, comment)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    if (isSubmitting) {
                        "Posting..."
                    } else if (isEditingOwnReview) {
                        "Update Review"
                    } else {
                        "Post Review"
                    },
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        if (!submitStatus.isNullOrBlank() &&
            submitStatus != "Sign in to add your review." &&
            submitStatus != "Review posted." &&
            submitStatus != "Review deleted."
        ) {
            Text(
                submitStatus,
                color = Color(0xFFFF9C9C),
                fontSize = 12.sp
            )
        }

        if (reviews.isEmpty()) {
            Text(
                "No reviews yet. Be the first one to share.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 13.sp
            )
        } else {
            reviews.forEach { review ->
                val isOwnReview = review.userId.isNotBlank() && review.userId == currentUserId
                CommunityReviewCard(
                    review = review,
                    isOwnReview = isOwnReview,
                    onEditOwnReview = {
                        comment = review.comment
                        isEditingOwnReview = true
                        onClearStatus()
                    },
                    onDeleteOwnReview = {
                        onDeleteOwnReview()
                    }
                )
            }
        }
    }
}

@Composable
private fun CommunityReviewCard(
    review: CommunityReview,
    isOwnReview: Boolean,
    onEditOwnReview: () -> Unit,
    onDeleteOwnReview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!review.userPhotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = review.userPhotoUrl,
                contentDescription = review.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    review.userName.firstOrNull()?.uppercase() ?: "A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.userName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "★ ${review.rating}/10",
                    color = Color(0xFFFFD675),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatReviewTimestamp(review.updatedAtMillis),
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 11.sp
                )
            }
            Text(
                review.comment,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            if (isOwnReview) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Edit",
                        color = Color(0xFF8AE6FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onEditOwnReview)
                    )
                    Text(
                        "Delete",
                        color = Color(0xFFFF9C9C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onDeleteOwnReview)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewSortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) appColors().primary.copy(alpha = 0.8f)
                else Color.White.copy(alpha = 0.09f)
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatReviewTimestamp(updatedAtMillis: Long): String {
    if (updatedAtMillis <= 0L) return "now"
    val diff = (System.currentTimeMillis() - updatedAtMillis).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "now"
        diff < hour -> "${diff / minute}m ago"
        diff < day -> "${diff / hour}h ago"
        else -> "${diff / day}d ago"
    }
}

@Composable
private fun ActionButtons(
    palette: AppColors,
    userRating: Int,
    inWatchlist: Boolean,
    watched: Boolean,
    onRate: (Int) -> Unit,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton(
            label = if (inWatchlist) "Remove from Watchlist" else "Add to Watchlist",
            leadingIcon = Icons.Default.Bookmark,
            bg = Brush.horizontalGradient(listOf(Color(0xFFFFBA20), palette.primary)),
            border = Color.Transparent,
            textColor = Color(0xFF2A0945),
            onClick = onToggleWatchlist
        )

        ActionButton(
            label = if (watched) "Marked as Watched" else "Mark as Watched",
            leadingIcon = Icons.Default.Check,
            bg = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))),
            border = Color.White.copy(alpha = 0.12f),
            textColor = Color.White,
            onClick = onToggleWatched
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RATE /10", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.size(10.dp))
            repeat(10) { index ->
                val value = index + 1
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Rate $value",
                    tint = if (value <= userRating) Color(0xFFFFD675) else Color.White.copy(alpha = 0.35f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRate(value) }
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = userRating.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    leadingIcon: ImageVector,
    bg: Brush,
    border: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(leadingIcon, contentDescription = null, tint = textColor, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(label, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun PlatformSection(
    providers: TitleWatchProvidersDto?,
    title: String
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("AVAILABLE ON", color = appColors().primary, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 1.5.sp)

        if (providers == null) {
            Text(
                "Provider information unavailable.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            return
        }

        if (providers.watch.isNotEmpty()) {
            Text("WATCH (${providers.countryCode})", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 1.sp)
            ProviderList(
                providers = providers.watch,
                onProviderTap = { provider ->
                    openProviderSearchOrFallback(
                        context = context,
                        providerName = provider.name,
                        title = title
                    )
                }
            )
        }
        if (providers.buy.isNotEmpty()) {
            Text("BUY", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 1.sp)
            ProviderList(
                providers = providers.buy,
                onProviderTap = { provider ->
                    openProviderSearchOrFallback(
                        context = context,
                        providerName = provider.name,
                        title = title
                    )
                }
            )
        }
        if (providers.rent.isNotEmpty()) {
            Text("RENT", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 1.sp)
            ProviderList(
                providers = providers.rent,
                onProviderTap = { provider ->
                    openProviderSearchOrFallback(
                        context = context,
                        providerName = provider.name,
                        title = title
                    )
                }
            )
        }

        if (providers.watch.isEmpty() && providers.buy.isEmpty() && providers.rent.isEmpty()) {
            Text(
                "No providers available for ${providers.countryCode}.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ProviderList(
    providers: List<ProviderInfoDto>,
    onProviderTap: (ProviderInfoDto) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(providers, key = { it.id }) { provider ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .clickable { onProviderTap(provider) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProviderLogo(provider)
                Spacer(modifier = Modifier.size(8.dp))
                Text(provider.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

private fun openProviderSearchOrFallback(
    context: android.content.Context,
    providerName: String,
    title: String
) {
    val normalized = providerName.lowercase()
    val query = Uri.encode(title)
    val packages = providerPackageCandidates(normalized)
    val deepLinks = providerDeepLinks(normalized, query)
    val webLinks = providerWebLinks(normalized, query)

    if (tryOpenDeepLinksInApp(context, deepLinks, packages)) return
    if (tryOpenProviderApp(context, packages)) return
    tryOpenInBrowser(context, webLinks)
}

private fun providerPackageCandidates(normalizedProviderName: String): List<String> {
    return when {
        normalizedProviderName.contains("netflix") -> listOf("com.netflix.mediaclient")
        normalizedProviderName.contains("apple tv") || normalizedProviderName.contains("apple tv store") ->
            listOf("com.apple.atve.androidtv.appletv")
        normalizedProviderName.contains("prime video") || normalizedProviderName.contains("amazon prime video") ->
            listOf("com.amazon.avod.thirdpartyclient")
        normalizedProviderName.contains("hotstar") || normalizedProviderName.contains("jiohotstar") ->
            listOf("com.jiohotstar", "in.startv.hotstar")
        else -> emptyList()
    }
}

private fun providerDeepLinks(normalizedProviderName: String, query: String): List<String> {
    return when {
        normalizedProviderName.contains("netflix") ->
            listOf("nflx://www.netflix.com/search?q=$query")
        normalizedProviderName.contains("apple tv") || normalizedProviderName.contains("apple tv store") ->
            listOf("https://tv.apple.com/search?term=$query")
        normalizedProviderName.contains("prime video") || normalizedProviderName.contains("amazon prime video") ->
            listOf("primevideo://search/$query")
        normalizedProviderName.contains("hotstar") || normalizedProviderName.contains("jiohotstar") ->
            listOf("hotstar://search?q=$query")
        else -> emptyList()
    }
}

private fun providerWebLinks(normalizedProviderName: String, query: String): List<String> {
    return when {
        normalizedProviderName.contains("netflix") ->
            listOf("https://www.netflix.com/search?q=$query")
        normalizedProviderName.contains("apple tv") || normalizedProviderName.contains("apple tv store") ->
            listOf("https://tv.apple.com/search?term=$query")
        normalizedProviderName.contains("prime video") || normalizedProviderName.contains("amazon prime video") ->
            listOf("https://www.primevideo.com/search/ref=atv_nb_sr?phrase=$query")
        normalizedProviderName.contains("hotstar") || normalizedProviderName.contains("jiohotstar") ->
            listOf("https://www.hotstar.com/in/search?q=$query")
        else -> emptyList()
    }
}

private fun tryOpenDeepLinksInApp(
    context: android.content.Context,
    links: List<String>,
    packages: List<String>
): Boolean {
    for (link in links) {
        val uri = Uri.parse(link)
        for (packageName in packages) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, uri).setPackage(packageName)
                )
                return true
            } catch (_: Exception) {
                // try next package/url
            }
        }
    }
    return false
}

private fun tryOpenProviderApp(
    context: android.content.Context,
    packages: List<String>
): Boolean {
    val packageManager = context.packageManager
    return packages.any { packageName ->
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }
}

private fun tryOpenInBrowser(
    context: android.content.Context,
    links: List<String>
): Boolean {
    return links.any { link ->
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Composable
private fun ProviderLogo(provider: ProviderInfoDto) {
    if (!provider.logoUrl.isNullOrBlank()) {
        AsyncImage(
            model = provider.logoUrl,
            contentDescription = provider.name,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF5A6270)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                provider.name.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CastSection(
    castData: List<CastPerson>,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("LEADING CAST", color = appColors().primary, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 1.5.sp)
            Text("See All", color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp)
        }

        if (isLoading && castData.isEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(6) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 90.dp, height = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 70.dp, height = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }
            return
        }

        if (castData.isEmpty()) {
            Text(
                "Cast information unavailable.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(castData) { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (member.profileUrl != null) {
                            AsyncImage(
                                model = member.profileUrl,
                                contentDescription = member.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(126.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(126.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF1B1E26), Color(0xFF5D626D), Color(0xFFE8E8E8))))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(member.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(member.role, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimilarTitlesSection(
    navController: NavHostController,
    similarData: List<TitleCardDto>,
    isLoading: Boolean,
    watchedKeys: Set<String>,
    showBadges: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "SIMILAR TITLES",
            color = appColors().primary,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (isLoading && similarData.isEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 150.dp, height = 220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(0.1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }
            return
        }

        if (similarData.isEmpty()) {
            Text(
                "No similar titles available.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            return
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(similarData) { title ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 220.dp)
                            .clickable {
                                navController.navigate(
                                    TitleDetails.createRoute(
                                        title = title.title,
                                        subtitle = title.subtitle,
                                        rating = title.rating,
                                        overview = title.overview,
                                        posterUrl = title.posterUrl,
                                        mediaType = title.mediaType.ifBlank { "movie" },
                                        itemId = title.id
                                    )
                                )
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .border(0.1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1B1E26), Color(0xFF3C4C64), Color(0xFF828A98))
                                )
                            )
                    ) {
                        if (title.posterUrl != null) {
                            AsyncImage(
                                model = title.posterUrl,
                                contentDescription = title.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        if (showBadges) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(title.rating, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (showBadges && watchedKeys.contains("${title.mediaType}-${title.id}")) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.9f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("WATCHED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(title.title.limitChars(18), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(title.subtitle, color = Color.White.copy(alpha = 0.76f), fontSize = 10.sp)
                }
            }
        }
    }
}
