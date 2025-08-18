package com.tech.eventix.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventDetailsScreenUiState
import com.tech.eventix.uistate.EventDetailUiState
import com.tech.eventix.viewmodel.EventDetailsViewModel

@Composable
fun EventDetailsScreen(
    onBackClick: () -> Unit = {},
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        when (uiState) {
            is EventDetailsScreenUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is EventDetailsScreenUiState.Error -> {
                Text(
                    text = uiState.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is EventDetailsScreenUiState.Success -> {
                val eventUi = uiState.event.toListUi()
                EventDetailsContent(event = eventUi, modifier = Modifier.fillMaxSize())

                EventDetailsTopBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onBackClick = onBackClick
                )
                EventDetailsBottomBar(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

private fun EventDetailUiState.toListUi(): EventUiState =
    EventUiState(id = "", name = name, image = image, dateTime = dateTime, location = location)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = onShareClick) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun EventDetailsBottomBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$62.08",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Aug 10 • 6:30 PM",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Get tickets")
            }
        }
    }
}

@Composable
fun EventDetailsContent(event: EventUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp) // Padding for the bottom bar
    ) {
        item {
            EventImageHeader(event)
        }
        item {
            EventInfoSection(event)
        }
        item {
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
        }
        item {
            OverviewSection()
        }
    }
}

@Composable
fun EventImageHeader(event: EventUiState) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)) {
        Image(
            painter = rememberAsyncImagePainter(event.image),
            contentDescription = event.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFC8E6C9) // A light green color from the image
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.HourglassTop, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = Color.DarkGray
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "SALES END SOON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun EventInfoSection(event: EventUiState) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = event.name,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = event.location,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.dateTime,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        FriendsGoing()
    }
}

@Composable
fun FriendsGoing() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { }
    ) {
        // Placeholder for friend images
        Row {
             Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray))
             Box(modifier = Modifier.size(32.dp).offset(x = (-12).dp).clip(CircleShape).background(Color.Gray))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("See if friends are going", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
fun OverviewSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Overview",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Experience a musical journey of music + mindfulness this August as MindTravel creator Murray Hidary brings live-piano compositions to Miami!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Text(
                text = "Read more",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEventDetailsScreen() {
    EventDetailsScreen()
}