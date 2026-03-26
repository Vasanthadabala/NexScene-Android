package com.piggylabs.nexscene.ui.screens.wishlist

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.piggylabs.nexscene.data.local.TitleUserState
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.ui.theme.AppColors
import com.piggylabs.nexscene.ui.theme.LocalAppColors
import com.piggylabs.nexscene.ui.theme.appColors

private enum class WishFilter { All, Movies, TvSeries }

@ExperimentalMaterial3Api
@Composable
fun WishListScreen(navController: NavHostController) {
    val palette = LocalAppColors.current
    val viewModel: WishListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var filter by rememberSaveable { mutableStateOf(WishFilter.All) }
    var watchedOnly by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initLocal(context)
    }

    val filteredWatchlist = uiState.watchlistItems.filterBy(filter)
    val filteredWatched = uiState.watchedItems.filterBy(filter)

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.neutral)
                .padding(innerPadding)
        ) {
            WishListScreenComponent(
                navController = navController,
                palette = palette,
                filter = filter,
                watchedOnly = watchedOnly,
                watchlistItems = filteredWatchlist,
                watchedItems = filteredWatched,
                onFilterChange = { filter = it },
                onToggleWatchedOnly = { watchedOnly = !watchedOnly },
                onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                onUnmarkWatched = viewModel::unmarkWatched
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun WishListScreenComponent(
    navController: NavHostController,
    palette: AppColors,
    filter: WishFilter,
    watchedOnly: Boolean,
    watchlistItems: List<TitleUserState>,
    watchedItems: List<TitleUserState>,
    onFilterChange: (WishFilter) -> Unit,
    onToggleWatchedOnly: () -> Unit,
    onRemoveFromWatchlist: (Int, String) -> Unit,
    onUnmarkWatched: (Int, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF211B11),
                        Color(0xFF362F24),
                        Color(0xFF211B11)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WishHeader() }
            item {
                FilterTabs(
                    selected = filter,
                    onSelect = onFilterChange
                )
            }
            item {
                ShowWatchedToggle(
                    watchedOnly = watchedOnly,
                    onToggle = onToggleWatchedOnly
                )
            }

            if (!watchedOnly) {
                item {
                    SectionTitle("MY WATCHLIST")
                }

                if (watchlistItems.isEmpty()) {
                    item { EmptyStateText("No watchlist items yet") }
                } else {
                    items(watchlistItems.chunked(2)) { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { item ->
                                WishCard(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                    actionIcon = Icons.Default.Close,
                                    actionDescription = "Remove from watchlist",
                                    onAction = { onRemoveFromWatchlist(item.itemId, item.mediaType) },
                                    onOpen = {
                                        navController.navigate(
                                            TitleDetails.createRoute(
                                                title = item.title.ifBlank { "Untitled" },
                                                subtitle = item.mediaType.uppercase(),
                                                rating = if (item.userRating > 0) item.userRating.toString() else "",
                                                overview = "",
                                                posterUrl = item.posterUrl,
                                                mediaType = item.mediaType,
                                                itemId = item.itemId
                                            )
                                        )
                                    }
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (watchedOnly) {
                item {
                    SectionTitle("WATCHED")
                }
                if (watchedItems.isEmpty()) {
                    item { EmptyStateText("No watched items yet") }
                } else {
                    items(watchedItems.chunked(2)) { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { item ->
                                WishCard(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                    actionIcon = Icons.Default.Check,
                                    actionDescription = "Unmark watched",
                                    onAction = { onUnmarkWatched(item.itemId, item.mediaType) },
                                    onOpen = {
                                        navController.navigate(
                                            TitleDetails.createRoute(
                                                title = item.title.ifBlank { "Untitled" },
                                                subtitle = item.mediaType.uppercase(),
                                                rating = if (item.userRating > 0) item.userRating.toString() else "",
                                                overview = "",
                                                posterUrl = item.posterUrl,
                                                mediaType = item.mediaType,
                                                itemId = item.itemId
                                            )
                                        )
                                    }
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "WATCHLIST",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun EmptyStateText(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.68f),
        fontSize = 14.sp
    )
}

@Composable
private fun FilterTabs(
    selected: WishFilter,
    onSelect: (WishFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabPill("All", selected == WishFilter.All, Modifier.weight(1f)) { onSelect(WishFilter.All) }
        TabPill("Movies", selected == WishFilter.Movies, Modifier.weight(1f)) { onSelect(WishFilter.Movies) }
        TabPill("TV Series", selected == WishFilter.TvSeries, Modifier.weight(1f)) { onSelect(WishFilter.TvSeries) }
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) appColors().primary else appColors().neutral)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.82f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ShowWatchedToggle(watchedOnly: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WATCHED ONLY",
            color = Color.White.copy(alpha = 0.84f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 34.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (watchedOnly) appColors().primary else Color.White.copy(alpha = 0.18f))
                .padding(4.dp)
                .clickable(onClick = onToggle)
        ) {
            Box(
                modifier = Modifier
                    .align(if (watchedOnly) Alignment.CenterEnd else Alignment.CenterStart)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECE5F5))
            )
        }
    }
}

@Composable
private fun WishCard(
    item: TitleUserState,
    modifier: Modifier = Modifier,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionDescription: String,
    onAction: () -> Unit,
    onOpen: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A2533), Color(0xFF2D3E52), Color(0xFF121921))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .clickable(onClick = onOpen)
        ) {
            if (!item.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionDescription,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
        Text(
            text = item.title.ifBlank { "Untitled" },
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = buildMeta(item),
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            letterSpacing = 0.6.sp
        )
    }
}

private fun buildMeta(item: TitleUserState): String {
    val media = if (item.mediaType.equals("tv", ignoreCase = true)) "TV SERIES" else "MOVIE"
    val rated = if (item.userRating > 0) " • YOUR RATING ${item.userRating}/10" else ""
    return "$media$rated"
}

private fun List<TitleUserState>.filterBy(filter: WishFilter): List<TitleUserState> {
    return when (filter) {
        WishFilter.All -> this
        WishFilter.Movies -> filter { it.mediaType.equals("movie", ignoreCase = true) }
        WishFilter.TvSeries -> filter { it.mediaType.equals("tv", ignoreCase = true) }
    }
}
