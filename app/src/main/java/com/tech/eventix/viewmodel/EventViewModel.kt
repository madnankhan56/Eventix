package com.tech.eventix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.eventix.domain.Event
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventsScreenUiState
import com.tech.eventix.usecase.BrowseEventsUseCase
import com.tech.eventix.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val browseEventsUseCase: BrowseEventsUseCase
) : ViewModel() {

    private val eventsQuerySignal = MutableStateFlow(EventQuery(0, null) )

    val eventsScreenUiState: StateFlow<EventsScreenUiState> = createEventUiStateStream().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = EventsScreenUiState.Loading
    )

    private fun createEventUiStateStream(): Flow<EventsScreenUiState> =
        this.eventsQuerySignal.transform { (page, keyword) ->
            val currentState = eventsScreenUiState.value
            if (currentState is EventsScreenUiState.Success && page > 0) {
                emit(currentState.copy(isLoadingMore = true, paginationError = null))
            }

            val previousEvents = (currentState as? EventsScreenUiState.Success)?.events ?: emptyList()

            emitAll(browseEventsUseCase(page = page, keyword = keyword).map { result ->
                when (result) {
                    is ResultState.Success -> buildSuccessState(page, result.data, previousEvents)
                    is ResultState.Error -> buildErrorOrPaginatedErrorState(page, result.getErrorMessage(), previousEvents)
                }
            })
        }

    private fun buildSuccessState(page: Int, events: List<Event>, previousEvents: List<EventUiState>): EventsScreenUiState.Success {
        val newEvents = events.map { it.toUiState() }
        val accumulatedEvents = if (page == 0) newEvents else previousEvents + newEvents

        return EventsScreenUiState.Success(
            events = accumulatedEvents,
            page = page,
            onLoadNextPage = { loadNextPage() },
            isLoadingMore = false,
            paginationError = null,
            onSearch = { keyword -> search(keyword) }
        )
    }

    private fun buildErrorOrPaginatedErrorState(page: Int, errorMessage: String, previousEvents: List<EventUiState>): EventsScreenUiState {
        return if (page == 0) {
            EventsScreenUiState.Error(errorMessage)
        } else {
            EventsScreenUiState.Success(
                events = previousEvents,
                page = page - 1,
                onLoadNextPage = { loadNextPage() },
                isLoadingMore = false,
                paginationError = errorMessage,
                onSearch = { keyword -> search(keyword) }
            )
        }
    }

    private fun loadNextPage() {
        val currentState = eventsScreenUiState.value
        if (currentState is EventsScreenUiState.Success) {
            viewModelScope.launch {
                this@EventViewModel.eventsQuerySignal.emit(EventQuery(currentState.page + 1, this@EventViewModel.eventsQuerySignal.value.keyword))
            }
        }
    }

    private fun search(keyword: String) {
        viewModelScope.launch {
            val searchKeyword = keyword.trim().takeIf { it.isNotEmpty() }
            this@EventViewModel.eventsQuerySignal.emit(EventQuery(0, searchKeyword))
        }
    }
}

private fun Event.toUiState(): EventUiState {
    return EventUiState(
        name = this.name,
        image = this.imageUrl.orEmpty(),
        dateTime = listOf(this.date, this.time).filter { it.isNotEmpty() }.joinToString(", "),
        location = listOfNotNull(this.venue?.name, this.venue?.city)
            .filter { it.isNotEmpty() }
            .joinToString(", ")
    )
} 

private data class EventQuery(
    val page: Int,
    val keyword: String?
)