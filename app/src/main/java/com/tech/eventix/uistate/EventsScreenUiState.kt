package com.tech.eventix.uistate

sealed class EventsScreenUiState {
    data class Success(val events: List<EventUiState>) : EventsScreenUiState()
    data class Error(val message: String) : EventsScreenUiState()
    data object Loading : EventsScreenUiState()
} 