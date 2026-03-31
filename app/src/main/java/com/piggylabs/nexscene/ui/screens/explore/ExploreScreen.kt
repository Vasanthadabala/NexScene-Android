package com.piggylabs.nexscene.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.piggylabs.nexscene.data.model.TitleCardDto
import com.piggylabs.nexscene.navigation.TitleDetails
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.screens.home.limitChars
import com.piggylabs.nexscene.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(
    navController: NavHostController,
    title: String,
    source: String,
    mediaType: String,
    movieGenreId: Int,
    tvGenreId: Int
) {
    val viewModel: ExploreViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(source, mediaType, movieGenreId, tvGenreId) {
        viewModel.load(
            source = source,
            mediaType = mediaType,
            movieGenreId = movieGenreId,
            tvGenreId = tvGenreId
        )
    }

    Scaffold(
        topBar = { TopBar(name = title.ifBlank { "Explore" }, navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Text(
                        text = "Loading...",
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Unable to load",
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    ExploreGrid(navController = navController, items = uiState.items)
                }
            }
        }
    }
}

@Composable
private fun ExploreGrid(
    navController: NavHostController,
    items: List<TitleCardDto>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(items, key = { "${it.mediaType}-${it.id}" }) { item ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(0.1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
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
                ) {
                    if (item.posterUrl != null) {
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
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
                Text(item.title.limitChars(20), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(item.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}
