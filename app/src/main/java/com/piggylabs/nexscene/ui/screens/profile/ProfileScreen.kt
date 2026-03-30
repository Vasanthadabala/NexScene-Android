package com.piggylabs.nexscene.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.piggylabs.nexscene.data.local.db.AppDataBase
import com.piggylabs.nexscene.data.local.entity.TitleStateEntity
import com.piggylabs.nexscene.navigation.Settings
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.theme.appColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ProfileBackground = Color(0xFF120B02)
private val AccentYellow = Color(0xFFFFC107)
private val WarmText = Color(0xFFDFC79F)

data class TrackerProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = "You",
    val watchedCount: Int = 0,
    val watchlistCount: Int = 0,
    val averageRating: Double = 0.0,
    val ratedCount: Int = 0,
    val recentWatched: List<TitleStateEntity> = emptyList(),
    val topRated: List<TitleStateEntity> = emptyList()
)

@ExperimentalMaterial3Api
@Composable
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(name = "back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            ProfileScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ProfileScreenComponent(navController: NavHostController) {
    val userPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    val uiState = produceState(initialValue = TrackerProfileUiState()) {
        value = withContext(Dispatchers.IO) {
            val db = AppDataBase.getDatabase(navController.context)
            val prefs = navController.context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
            val userName = prefs.getString("userName", "You") ?: "You"

            val all = db.titleStateDao().getAllStates()
            val watched = all.filter { it.watched }
            val watchlist = all.filter { it.inWatchlist }
            val rated = all.filter { it.userRating > 0 }
            val average = if (rated.isEmpty()) 0.0 else rated.map { it.userRating }.average()
            val recentWatched = watched.sortedByDescending { it.updatedAt }.take(8)
            val topRated = rated.sortedByDescending { it.userRating }.take(8)

            TrackerProfileUiState(
                isLoading = false,
                userName = userName,
                watchedCount = watched.size,
                watchlistCount = watchlist.size,
                averageRating = average,
                ratedCount = rated.size,
                recentWatched = recentWatched,
                topRated = topRated
            )
        }
    }.value

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentYellow)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF132037)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFFE2EDF8),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = uiState.userName,
                        color = Color.White,
                        fontSize = 42.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Track ratings, watchlist, and watched progress",
                        color = appColors().secondary,
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            StatCard("TITLES WATCHED", uiState.watchedCount.toString(), Icons.Default.Visibility)
            Spacer(modifier = Modifier.height(10.dp))
            StatCard("AVERAGE RATING", String.format("%.1f", uiState.averageRating), Icons.Default.Star)
            Spacer(modifier = Modifier.height(10.dp))
            StatCard("WATCHLIST COUNT", uiState.watchlistCount.toString(), Icons.Default.Bookmark)
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Text("Recently Watched", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.recentWatched.isEmpty()) {
                EmptyStateCard("No watched titles yet")
            }
        }

        items(uiState.recentWatched) { item ->
            TrackerTitleRow(
                title = item.title.ifBlank { "Untitled" },
                subtitle = "${item.mediaType.uppercase()} • Rated ${item.userRating.coerceIn(0, 10)}/10"
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text("Top Rated By You", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.topRated.isEmpty()) {
                EmptyStateCard("Rate titles to see your top picks")
            }
        }

        items(uiState.topRated) { item ->
            TrackerTitleRow(
                title = item.title.ifBlank { "Untitled" },
                subtitle = "${item.mediaType.uppercase()} • Your rating ${item.userRating.coerceIn(0, 10)}/10",
                score = item.userRating
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().cards),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(text = title, color = Color.White, fontSize = 12.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, color = appColors().secondary, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, tint = appColors().primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TrackerTitleRow(
    title: String,
    subtitle: String,
    score: Int? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().cards),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF081019)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFFFE2A6),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, color = WarmText, fontSize = 12.sp, lineHeight = 14.sp)
            }

            if (score != null) {
                Text(
                    text = "★ ${score.coerceIn(0, 10)}/10",
                    color = AccentYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().cards),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
