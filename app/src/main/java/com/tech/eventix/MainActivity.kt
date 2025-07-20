package com.tech.eventix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.tech.eventix.ui.EventsScreen
import com.tech.eventix.ui.theme.EventixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventixTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    EventsScreen()
                }
            }
        }
    }
}