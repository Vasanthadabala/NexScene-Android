package com.piggylabs.nexscene.ui.screens.title_details

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.piggylabs.nexscene.data.model.CastPerson
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.screens.home.limitChars
import com.piggylabs.nexscene.ui.theme.AppColors
import com.piggylabs.nexscene.ui.theme.LocalAppColors
import com.piggylabs.nexscene.ui.theme.appColors

private data class CastMember(val name: String, val role: String)
private data class SimilarTitle(val title: String, val genre: String, val rating: String, val colors: List<Color>)

private val cast = listOf(
    CastMember("Cillian Murphy", "J. Robert Oppenheimer"),
    CastMember("Emily Blunt", "Kitty Oppenheimer"),
    CastMember("Matt Damon", "Leslie Groves")
)

private val similarTitles = listOf(
    SimilarTitle("Interstellar", "Sci-Fi, Adventure", "8.7", listOf(Color(0xFF143746), Color(0xFF2C6A7A), Color(0xFFF0CE8E))),
    SimilarTitle("The Prestige", "Drama, Mystery", "8.5", listOf(Color(0xFF153B45), Color(0xFF204E56), Color(0xFF2D2F38))),
    SimilarTitle("Dunkirk", "Action, History", "7.8", listOf(Color(0xFF245C60), Color(0xFF5BA3A2), Color(0xFFD9DCA8))),
    SimilarTitle("Inception", "Sci-Fi, Action", "8.8", listOf(Color(0xFF214A5A), Color(0xFF487D8C), Color(0xFFD9D2A9)))
)

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
    val viewModel: TitleDetailsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(details.id, details.mediaType) {
        viewModel.initLocal(context)
        viewModel.load(
            itemId = details.id,
            mediaType = details.mediaType,
            title = details.title,
            posterUrl = details.posterUrl
        )
    }

    val castData = if (uiState.cast.isEmpty()) {
        cast.map { CastPerson(id = 0, name = it.name, role = it.role, profileUrl = null) }
    } else {
        uiState.cast
    }
    val similarData = if (uiState.similar.isEmpty()) {
        similarTitles.map {
            TitleCardDto(
                id = 0,
                title = it.title,
                subtitle = it.genre,
                rating = it.rating,
                posterUrl = null,
                overview = "",
                mediaType = details.mediaType.ifBlank { "movie" }
            )
        }
    } else {
        uiState.similar
    }

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
                castData = castData,
                similarData = similarData,
                userRating = uiState.userRating,
                inWatchlist = uiState.inWatchlist,
                watched = uiState.watched,
                onRate = { rating ->
                    viewModel.setRating(
                        itemId = details.id,
                        mediaType = details.mediaType,
                        title = details.title,
                        posterUrl = details.posterUrl,
                        rating = rating
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
                }
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TitleDetailsScreenComponent(
    navController: NavHostController,
    details: TitleItemDetails,
    castData: List<CastPerson>,
    similarData: List<TitleCardDto>,
    userRating: Int,
    inWatchlist: Boolean,
    watched: Boolean,
    onRate: (Int) -> Unit,
    onToggleWatchlist: () -> Unit,
    onToggleWatched: () -> Unit
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
            contentPadding = PaddingValues(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroSection( details = details)
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
                PlatformSection()
            }

            item {
                CastSection(castData)
            }

            item {
                SimilarTitlesSection(
                    navController = navController,
                    similarData = similarData
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    details: TitleItemDetails
) {
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
            if (!details.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = details.posterUrl,
                    contentDescription = details.title,
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
                    text = details.title.ifBlank { "OPPENHEIMER" },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "IMDB Rating",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD675),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            details.rating.ifBlank { "8.4" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "STORYLINE",
                color = appColors().primary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                details.overview.ifBlank {
                    "The story of American scientist J.\nRobert Oppenheimer and his role in the\ndevelopment of the atomic bomb."
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
private fun PlatformSection() {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("AVAILABLE ON", color = appColors().primary, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 1.5.sp)
        PlatformPill("Netflix", "STREAM ON", Color(0xFFE50914))
        PlatformPill("Prime Video", "BUY/RENT", Color(0xFF4C8DF4))
        PlatformPill("Disney+", "WATCH ON", Color(0xFF4B50D5))
    }
}

@Composable
private fun PlatformPill(name: String, sub: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(sub, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, letterSpacing = 0.6.sp)
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun CastSection(castData: List<CastPerson>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("LEADING CAST", color = appColors().primary, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 1.5.sp)
            Text("See All", color = Color.White.copy(alpha = 0.86f), fontSize = 14.sp)
        }

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

@Composable
private fun SimilarTitlesSection(
    navController: NavHostController,
    similarData: List<TitleCardDto>
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
                                if (title.id != 0 && title.mediaType.isNotBlank()) {
                                    navController.navigate(
                                        TitleDetails.createRoute(
                                            title = title.title,
                                            subtitle = title.subtitle,
                                            rating = title.rating,
                                            overview = title.overview,
                                            posterUrl = title.posterUrl,
                                            mediaType = title.mediaType,
                                            itemId = title.id
                                        )
                                    )
                                }
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
                    Text(title.title.limitChars(18), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(title.subtitle, color = Color.White.copy(alpha = 0.76f), fontSize = 10.sp)
                }
            }
        }
    }
}
