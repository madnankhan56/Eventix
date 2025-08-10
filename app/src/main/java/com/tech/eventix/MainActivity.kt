package com.tech.eventix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tech.eventix.ui.EventDetailsScreen
import com.tech.eventix.ui.EventsScreen
import com.tech.eventix.ui.theme.EventixTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Events : Screen("events")
    object EventDetails : Screen("eventDetails")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            EventixTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.Events.route) {
                        composable(Screen.Events.route) {
                            EventsScreen(onEventClick = {
                                navController.navigate(Screen.EventDetails.route)
                            })
                        }
                        composable(Screen.EventDetails.route) {
                            EventDetailsScreen()
                        }
                    }
                }
            }
        }
    }
}