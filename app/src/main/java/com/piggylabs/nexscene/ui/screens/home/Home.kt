package com.piggylabs.nexscene.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.piggylabs.nexscene.data.model.MovieDto
import com.piggylabs.nexscene.data.model.TvDto
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.ui.theme.LocalAppColors
import com.piggylabs.nexscene.ui.theme.appColors

data class PosterCard(
    val id: Int,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>,
    val badge: String? = null,
    val posterUrl: String? = null,
    val overview: String = "",
    val mediaType: String = "movie"
)

private val fallbackMovieCards = listOf(
    PosterCard(157336, "Interstellar", "2014 • Christopher Nolan", listOf(Color(0xFF12343D), Color(0xFF1E5D6B), Color(0xFFF4D28A)), "7.7", overview = "A team of explorers travel through a wormhole in space.", mediaType = "movie"),
    PosterCard(414906, "The Batman", "2022 • Matt Reeves", listOf(Color(0xFF1D212D), Color(0xFF2A2F3F), Color(0xFF57606D)), overview = "Batman uncovers corruption in Gotham City.", mediaType = "movie"),
    PosterCard(872585, "Oppenheimer", "2023 • Christopher Nolan", listOf(Color(0xFF2E2018), Color(0xFF5A3625), Color(0xFFCB8E52)), overview = "The story of J. Robert Oppenheimer.", mediaType = "movie")
)

private val fallbackTvCards = listOf(
    PosterCard(1402, "Succession", "4 Seasons • Drama", listOf(Color(0xFF0E3A46), Color(0xFF114F5E), Color(0xFF1B7184)), "NEW", overview = "A family controls one of the biggest media conglomerates.", mediaType = "tv"),
    PosterCard(100088, "The Last of Us", "1 Season • Action", listOf(Color(0xFF2F4232), Color(0xFF6B8468), Color(0xFFC4C5AF)), overview = "Joel and Ellie survive in a post-apocalyptic world.", mediaType = "tv"),
    PosterCard(70523, "Dark", "3 Seasons • Thriller", listOf(Color(0xFF23212B), Color(0xFF3D3A46), Color(0xFF6D6776)), overview = "A family saga with a supernatural twist in a German town.", mediaType = "tv")
)

private val posterGradients = listOf(
    listOf(Color(0xFF12343D), Color(0xFF1E5D6B), Color(0xFFF4D28A)),
    listOf(Color(0xFF1D212D), Color(0xFF2A2F3F), Color(0xFF57606D)),
    listOf(Color(0xFF2E2018), Color(0xFF5A3625), Color(0xFFCB8E52)),
    listOf(Color(0xFF0E3A46), Color(0xFF114F5E), Color(0xFF1B7184))
)

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(navController: NavHostController) {
    val palette = LocalAppColors.current
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    val movieDynamicCards = state.movies.toMoviePosterCards(viewModel::posterUrl)
    val tvDynamicCards = state.tvShows.toTvPosterCards(viewModel::posterUrl)
    val movieCards = if (movieDynamicCards.isEmpty()) fallbackMovieCards else movieDynamicCards.take(10)
    val tvCards = if (tvDynamicCards.isEmpty()) fallbackTvCards else tvDynamicCards.take(10)

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.neutral)
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.movies.isEmpty() && state.tvShows.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.movies.isEmpty() && state.tvShows.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error ?: "Error", color = Color.White)
                        Button(onClick = viewModel::loadPopularMovies) { Text("Retry") }
                    }
                }

                else -> {
                    HomeScreenComponent(navController, movieCards, tvCards)
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun HomeScreenComponent(
    navController: NavHostController,
    movieCards: List<PosterCard>,
    tvCards: List<PosterCard>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                appColors().neutral
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 0.dp)
        ) {
            item { TopHeader() }
            item {
                HeroCard(
                    featuredCard = movieCards.firstOrNull(),
                    onOpenTitle = {
                        movieCards.firstOrNull()?.let { selected ->
                            navController.navigate(selected.toTitleDetailsRoute())
                        }
                    }
                )
            }
            item { SectionTitle(title = "Trending Movies", trailing = "Explore All") }
            item { HorizontalPosterRow(cards = movieCards, onCardClick = { navController.navigate(it.toTitleDetailsRoute()) }) }
            item { EditorsPickCard(palette = appColors()) }
            item { PremiumCard(palette = appColors()) }
            item { SectionTitle(title = "Trending TV Shows", trailing = "View All") }
            item { HorizontalPosterRow(cards = tvCards, onCardClick = { navController.navigate(it.toTitleDetailsRoute()) }) }
        }
    }
}

private fun List<MovieDto>.toMoviePosterCards(posterUrlBuilder: (String?) -> String?): List<PosterCard> {
    return mapIndexed { index, movie ->
        PosterCard(
            id = movie.id,
            title = movie.title,
            subtitle = "IMDB • Rating ${String.format("%.1f", movie.vote_average)}",
            gradient = posterGradients[index % posterGradients.size],
            badge = String.format("%.1f", movie.vote_average),
            posterUrl = posterUrlBuilder(movie.poster_path),
            overview = movie.overview,
            mediaType = "movie"
        )
    }
}

private fun List<TvDto>.toTvPosterCards(posterUrlBuilder: (String?) -> String?): List<PosterCard> {
    return mapIndexed { index, tv ->
        PosterCard(
            id = tv.id,
            title = tv.name,
            subtitle = "TMDB TV • Rating ${String.format("%.1f", tv.vote_average)}",
            gradient = posterGradients[index % posterGradients.size],
            badge = String.format("%.1f", tv.vote_average),
            posterUrl = posterUrlBuilder(tv.poster_path),
            overview = tv.overview,
            mediaType = "tv"
        )
    }
}

private fun PosterCard.toTitleDetailsRoute(): String {
    return TitleDetails.createRoute(
        title = title,
        subtitle = subtitle,
        rating = badge.orEmpty(),
        overview = overview,
        posterUrl = posterUrl,
        mediaType = mediaType,
        itemId = id
    )
}

@Composable
private fun TopHeader() {
    val profilePhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (!profilePhotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Menu",
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
        }

        Text(
            text = "NexScene",
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    listOf(appColors().primary, Color(0xFFFFDEA8))
                )
            ),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Menu",
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun HeroCard(
    featuredCard: PosterCard?,
    onOpenTitle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .background(Color.Transparent)
            .clip(shape = RoundedCornerShape(0.dp))
            .clickable { onOpenTitle() }
            .padding(horizontal = 0.dp, vertical = 6.dp)
    ) {
        if (featuredCard?.posterUrl != null) {
            AsyncImage(
                model = featuredCard.posterUrl,
                contentDescription = featuredCard.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF2A8A8B),
                                Color(0xFF1D3E43),
                                Color(0xFF0B0C12)
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
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.BottomStart)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Chip(label = "EXCLUSIVE", bg = Color(0xFFF6D24E), fg = Color.Black)
                Text(
                    text = featuredCard?.subtitle ?: "PG-13 • 2h 46m • Sci-Fi",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = featuredCard?.title ?: "DUNE: PART TWO",
                color = Color.White,
                fontSize = 32.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Now featuring trending picks from TMDB.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFBA20), appColors().primary)))
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Watch Trailer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text("Add to Watchlist", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(label = "All Genres", bg = Color(0XFFFFB800), fg = Color(0xFF2A0B42))
        Chip(label = "Action", bg = Color.White.copy(alpha = 0.08f), fg = Color.White)
        Chip(label = "Sci-Fi", bg = Color.White.copy(alpha = 0.08f), fg = Color.White)
    }

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun SectionTitle(title: String, trailing: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = "CURATED", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 2.sp)
            Text(text = title, color = Color.White, fontSize = 28.sp, lineHeight = 48.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(text = trailing, color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp, lineHeight = 48.sp)
    }
}

@Composable
private fun HorizontalPosterRow(
    cards: List<PosterCard>,
    onCardClick: (PosterCard) -> Unit
) {
    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cards) { card ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(card.gradient))
                        .border(0.1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { onCardClick(card) }
                        .padding(0.dp)
                ) {
                    if (card.posterUrl != null) {
                        AsyncImage(
                            model = card.posterUrl,
                            contentDescription = card.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    if (card.badge != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(text = "★ ${card.badge}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(text = card.title.limitChars(22), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun EditorsPickCard(palette: com.piggylabs.nexscene.ui.theme.AppColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF102217), Color(0xFF18281D), Color(0xFF0D0E12))))
            .border(1.dp, palette.primary.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "EDITOR'S PICK", color = Color.White.copy(alpha = 0.72f), letterSpacing = 1.sp, fontSize = 10.sp)
            Text(text = "THE ART OF\nCINEMATOGRAPHY", color = Color.White, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "Discover the visionaries behind\nthe lens in our exclusive weekly\ndeep-dive series.",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun PremiumCard(palette: com.piggylabs.nexscene.ui.theme.AppColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF2B103F), Color(0xFF3A1C58), Color(0xFF4A2B62))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFD9B2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF2B0C3F), modifier = Modifier.size(12.dp))
            }
            Text(text = "Premium Perks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text(
                text = "Unlock 4K streaming, early access, and\nzero ads with a Pro subscription.",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Text(text = "Upgrade Now  ->", color = Color(0xFFE6C6FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(78.dp)
                .clip(RoundedCornerShape(topStart = 18.dp))
                .background(palette.primary.copy(alpha = 0.25f))
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun Chip(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun String.limitChars(maxChars: Int): String {
    return if (this.length > maxChars) {
        this.take(maxChars) + "..."
    } else {
        this
    }
}
