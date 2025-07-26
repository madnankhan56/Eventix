package com.tech.eventix.uistate

sealed class EventsScreenUiState {
    data class Success(
        val events: List<EventUiState>,
        val page: Int,
        val onLoadMoreEvent: (Int) -> Unit,
        val isLoadingMore: Boolean = false,
        val paginationError: String? = null
    ) : EventsScreenUiState()

    data class Error(val message: String) : EventsScreenUiState()
    data object Loading : EventsScreenUiState()
}