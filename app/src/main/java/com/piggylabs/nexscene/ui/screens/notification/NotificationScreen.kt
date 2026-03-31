package com.piggylabs.nexscene.ui.screens.notification

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.navigation.components.TopBar
import com.piggylabs.nexscene.ui.theme.appColors

private data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val unread: Boolean = true
)

@ExperimentalMaterial3Api
@Composable
fun NotificationScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(name = "back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            NotificationScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun NotificationScreenComponent(navController: NavHostController) {
    val notifications = remember {
        mutableStateListOf<NotificationItem>()
    }
    var showUnreadOnly by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { it.unread }
    val visibleItems = if (showUnreadOnly) notifications.filter { it.unread } else notifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Notifications",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Mark all read",
                color = appColors().primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        notifications.replaceAll { it.copy(unread = false) }
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                label = "All (${notifications.size})",
                selected = !showUnreadOnly,
                onClick = { showUnreadOnly = false }
            )
            FilterChip(
                label = "Unread ($unreadCount)",
                selected = showUnreadOnly,
                onClick = { showUnreadOnly = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (visibleItems.isEmpty()) {
            EmptyNotificationState()
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibleItems, key = { it.id }) { item ->
                NotificationCard(
                    item = item,
                    onToggleRead = {
                        val index = notifications.indexOfFirst { it.id == item.id }
                        if (index >= 0) notifications[index] = notifications[index].copy(unread = !notifications[index].unread)
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onToggleRead: () -> Unit
) {
    val unreadGlow = if (item.unread) appColors().primary.copy(alpha = 0.2f) else Color.Transparent
    val cardBrush = Brush.verticalGradient(
        if (item.unread) {
            listOf(Color(0xFF1E222C), Color(0xFF171B24), Color(0xFF12151D))
        } else {
            listOf(Color(0xFF191D24), Color(0xFF151922), Color(0xFF12151D))
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBrush)
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(unreadGlow),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.unread) appColors().primary else Color.White.copy(alpha = 0.84f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.timestamp,
                color = Color.White.copy(alpha = 0.54f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleRead() }
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (item.unread) appColors().primary else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (item.unread) "Mark as read" else "Mark unread",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) appColors().primary else Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = if (selected) 0f else 0.1f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyNotificationState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = appColors().primary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("You're all caught up", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("New alerts will show up here.", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
        }
    }
}
