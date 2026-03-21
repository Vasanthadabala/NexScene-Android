package com.piggylabs.nexscene

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.piggylabs.nexscene.navigation.AppNavigation
import com.piggylabs.nexscene.ui.theme.NexSceneTheme
import com.piggylabs.nexscene.ui.theme.appColors

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexSceneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .background(appColors().background)
                ) {
                    AppNavigation(context = applicationContext)
                }
            }
        }
    }
}