package com.piggylabs.nexscene.navigation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.nexscene.navigation.bottomBarItems
import com.piggylabs.nexscene.ui.theme.appColors

@Composable
fun BottomBar(navController: NavHostController){
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    selectedItemIndex = bottomBarItems.indexOfFirst { it.route == currentRoute }
    val barShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val barSurfaceColor = appColors().neutral

    Card(
        modifier = Modifier
            .clip(barShape)
            .border(
                width = 1.dp,
                Color.Gray.copy(alpha = 0.5f),
                barShape
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = barSurfaceColor
        ),
        shape = barShape
    ) {
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.clip(barShape)
        ) {
            bottomBarItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedItemIndex == index,
                    onClick = {
                        selectedItemIndex = index
                        navController.navigate(item.route){
                            popUpTo(item.route){
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W500
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = ""
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = appColors().primary.copy(alpha = 0.25f),
                        selectedIconColor = appColors().primary,
                        unselectedIconColor = Color.DarkGray,
                        selectedTextColor = appColors().primary,
                        unselectedTextColor = Color.DarkGray,

                        ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}
