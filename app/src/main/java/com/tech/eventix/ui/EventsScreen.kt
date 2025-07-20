package com.tech.eventix.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventsScreenUiState
import com.tech.eventix.viewmodel.EventViewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EventsScreen(
    viewModel: EventViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.eventsScreenUiState.collectAsState()
    EventsScreenContent(uiState = uiState, modifier = modifier)
}

@Composable
fun EventsScreenContent(
    uiState: EventsScreenUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is EventsScreenUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EventsScreenUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is EventsScreenUiState.Success -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.events) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(event.image),
                contentDescription = event.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.dateTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEventsScreenContent_Success() {
    EventsScreenContent(
        uiState = EventsScreenUiState.Success(
            events = listOf(
                EventUiState(
                    name = "Sample Event Name",
                    image = "https://placehold.co/600x400",
                    dateTime = "Sat, 19 July, 7:30 pm",
                    location = "Yankee Stadium, Bronx"
                ),
                EventUiState(
                    name = "Another Event",
                    image = "https://placehold.co/600x400",
                    dateTime = "Sun, 20 July, 8:00 pm",
                    location = "Astros Park, Houston"
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEventsScreenContent_Error() {
    EventsScreenContent(
        uiState = EventsScreenUiState.Error("Something went wrong loading events.")
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEventsScreenContent_Loading() {
    EventsScreenContent(
        uiState = EventsScreenUiState.Loading
    )
} 