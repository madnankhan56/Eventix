package com.tech.eventix.uistate


sealed interface EventDetailsScreenUiState {
    object Loading : EventDetailsScreenUiState
    data class Success(val event: EventDetailUiState) : EventDetailsScreenUiState
    data class Error(val message: String) : EventDetailsScreenUiState
}