package com.piggylabs.nexscene.ui.screens.search

import android.content.Context
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.piggylabs.nexscene.data.local.db.AppDataBase
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.navigation.Explore
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.ui.screens.home.limitChars
import com.piggylabs.nexscene.ui.theme.LocalAppColors
import com.piggylabs.nexscene.ui.theme.appColors

private data class GenreCard(
    val name: String,
    val posterUrl: String,
    val movieGenreId: Int,
    val tvGenreId: Int
)
data class TrendCard(
    val id: Int = 0,
    val mediaType: String = "",
    val title: String,
    val meta: String,
    val rating: String,
    val gradient: List<Color>,
    val overview: String = "",
    val posterUrl: String? = null,
    val watched: Boolean = false
)

private val genres = listOf(
    GenreCard("ACTION", "https://image.tmdb.org/t/p/w500/NNxYkU70HPurnNCSiCjYAmacwm.jpg", 28, 10759),
    GenreCard("COMEDY", "https://image.tmdb.org/t/p/w500/hlK0e0wAQ3VLuJcsfIYPvb4JVud.jpg", 35, 35),
    GenreCard("HORROR", "https://image.tmdb.org/t/p/w500/9E2y5Q7WlCVNEhP5GiVTjhEhx1o.jpg", 27, 9648),
    GenreCard("ROMANCE", "https://image.tmdb.org/t/p/w500/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg", 10749, 10749)
)

private val trendGradients = listOf(
    listOf(Color(0xFF1398A8), Color(0xFF53D5D4), Color(0xFF173943)),
    listOf(Color(0xFFE8E8E8), Color(0xFF527B8F), Color(0xFF101E2B)),
    listOf(Color(0xFFDADADA), Color(0xFF9EC2CC), Color(0xFF1D4250))
)

@ExperimentalMaterial3Api
@Composable
fun SearchScreen(navController: NavHostController) {
    val palette = LocalAppColors.current
    val viewModel: SearchViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val showBadges = context
        .getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getBoolean("show_watched_badge", true)
    val watchedItems by AppDataBase.getDatabase(context)
        .titleStateDao()
        .observeWatchedItems()
        .collectAsState(initial = emptyList())
    val watchedKeys = remember(watchedItems) {
        watchedItems.map { "${it.mediaType}-${it.itemId}" }.toSet()
    }

    val recentItems = state.results.take(5).map { it.title }
    val trends = if (state.results.isNotEmpty()) {
        state.results.take(10).mapIndexed { index, item ->
            TrendCard(
                id = item.id,
                mediaType = item.mediaType,
                title = item.title,
                meta = "${item.subtitle.uppercase()}",
                rating = item.rating,
                gradient = trendGradients[index % trendGradients.size],
                overview = item.overview,
                posterUrl = item.posterUrl,
                watched = watchedKeys.contains("${item.mediaType}-${item.id}")
            )
        }
    } else {
        state.trending.take(10).mapIndexed { index, item ->
            TrendCard(
                id = item.id,
                mediaType = item.mediaType,
                title = item.title,
                meta = "${item.subtitle.uppercase()}",
                rating = item.rating,
                gradient = trendGradients[index % trendGradients.size],
                overview = item.overview,
                posterUrl = item.posterUrl,
                watched = watchedKeys.contains("${item.mediaType}-${item.id}")
            )
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.neutral)
                .padding(innerPadding)
        ) {
            SearchScreenComponent(
                navController = navController,
                query = state.query,
                error = state.error,
                isLoading = state.isLoading,
                isTrendingLoading = state.isTrendingLoading,
                results = state.results,
                recentItems = recentItems,
                trends = trends,
                watchedKeys = watchedKeys,
                showBadges = showBadges,
                onQueryChange = viewModel::onQueryChanged,
                onSearch = viewModel::searchNow
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun SearchScreenComponent(
    navController: NavHostController,
    query: String,
    error: String?,
    isLoading: Boolean,
    isTrendingLoading: Boolean,
    results: List<TitleCardDto>,
    recentItems: List<String>,
    trends: List<TrendCard>,
    watchedKeys: Set<String>,
    showBadges: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val palette = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                appColors().neutral
            )
    ) {
        if (query.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchInputBar(query = query, onQueryChange = onQueryChange, onSearch = onSearch)
                when {
                    isLoading -> Text("Searching...", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    error != null -> Text(error, color = Color(0xFFFF7A7A), fontSize = 13.sp)
                    results.isEmpty() -> Text("No results found", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    else -> SearchResultsGrid(
                        navController = navController,
                        results = results,
                        watchedKeys = watchedKeys,
                        showBadges = showBadges
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SearchInputBar(query = query, onQueryChange = onQueryChange, onSearch = onSearch) }
            if (error != null) {
                item { Text(error, color = Color(0xFFFF7A7A), fontSize = 13.sp) }
            }
            if (recentItems.isNotEmpty()) {
                item { RecentlySearchedSection(recentItems) }
            }
            item { ExploreGenresSection(navController = navController) }
            if (isTrendingLoading || trends.isNotEmpty()) {
                item {
                    TrendingSection(
                        navController = navController,
                        trends = trends,
                        isLoading = isTrendingLoading,
                        watchedKeys = watchedKeys,
                        showBadges = showBadges
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsGrid(
    navController: NavHostController,
    results: List<TitleCardDto>,
    watchedKeys: Set<String>,
    showBadges: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(results, key = { "${it.mediaType}-${it.id}" }) { item ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable {
                            navController.navigate(
                                TitleDetails.createRoute(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    rating = item.rating,
                                    overview = item.overview,
                                    posterUrl = item.posterUrl,
                                    mediaType = item.mediaType,
                                    itemId = item.id
                                )
                            )
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(0.1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    if (item.posterUrl != null) {
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                    if (showBadges) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(item.rating, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (showBadges && watchedKeys.contains("${item.mediaType}-${item.id}")) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("WATCHED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(item.title.limitChars(18), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("${item.subtitle}", color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(10.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 16.sp
            ),
            decorationBox = { innerTextField ->
                if (query.isBlank()) {
                    Text(
                        text = "Search movies, TV shows.",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotBlank()) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Clear",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .padding(1.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
private fun RecentlySearchedSection(recentItems: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recently Searched", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("CLEAR ALL", color = appColors().primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(recentItems) { item ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("↻", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ExploreGenresSection(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Explore Genres", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        for (row in genres.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { genre ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
                            .clickable {
                                navController.navigate(
                                    Explore.createRoute(
                                        title = "${genre.name} Picks",
                                        source = "genre",
                                        mediaType = "mixed",
                                        movieGenreId = genre.movieGenreId,
                                        tvGenreId = genre.tvGenreId
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = genre.posterUrl,
                            contentDescription = genre.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(30.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                        )
                        Text(
                            genre.name,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingSection(
    navController: NavHostController,
    trends: List<TrendCard>,
    isLoading: Boolean,
    watchedKeys: Set<String>,
    showBadges: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trending Now", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        }

        if (isLoading && trends.isEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(6) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 170.dp, height = 255.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(0.1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 130.dp, height = 14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trends) { card ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 170.dp, height = 255.dp)
                                .clickable {
                                    if (card.id != 0 && card.mediaType.isNotBlank()) {
                                        navController.navigate(
                                            TitleDetails.createRoute(
                                                title = card.title,
                                                subtitle = card.meta,
                                                rating = card.rating,
                                                overview = card.overview,
                                                posterUrl = card.posterUrl,
                                                mediaType = card.mediaType,
                                                itemId = card.id
                                            )
                                        )
                                    }
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.verticalGradient(card.gradient))
                                .border(0.1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(0.dp)
                        ) {
                            if (card.posterUrl != null) {
                                AsyncImage(
                                    model = card.posterUrl,
                                    contentDescription = card.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Black.copy(alpha = 0.12f),
                                                    Color.Black.copy(alpha = 0.42f)
                                                )
                                            )
                                        )
                                )
                            }
                            if (showBadges) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.32f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("★", color = Color.White, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(card.rating, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (showBadges && (card.watched || watchedKeys.contains("${card.mediaType}-${card.id}"))) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.9f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("WATCHED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(card.title.limitChars(18), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
