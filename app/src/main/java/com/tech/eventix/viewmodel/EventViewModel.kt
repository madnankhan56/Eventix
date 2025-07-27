package com.tech.eventix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.eventix.domain.Event
import com.tech.eventix.uistate.EventUiState
import com.tech.eventix.uistate.EventsScreenUiState
import com.tech.eventix.usecase.GetEventsUseCase
import com.tech.eventix.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val loadNextPageSignal = MutableSharedFlow<Int>()

    private val loadEventSignal: Flow<Int> = flow {
        emit(0)
        emitAll(loadNextPageSignal)
    }

    val eventsScreenUiState: StateFlow<EventsScreenUiState> = createEventUiStateStream().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = EventsScreenUiState.Loading
    )

    private fun createEventUiStateStream(): Flow<EventsScreenUiState> =
        loadEventSignal.transform { page ->
            val currentState = eventsScreenUiState.value
            if (currentState is EventsScreenUiState.Success) {
                emit(currentState.copy(isLoadingMore = true, paginationError = null))
            }

            emitAll(getEventsUseCase(page = page).map { result ->
                when (result) {
                    is ResultState.Success -> buildSuccessState(page, result.data)
                    is ResultState.Error -> buildErrorOrPaginatedErrorState(page, result.getErrorMessage())
                }
            })
        }

    private fun buildSuccessState(page: Int, events: List<Event>): EventsScreenUiState.Success {
        val newEvents = events.map { it.toUiState() }
        val previousEvents = (eventsScreenUiState.value as? EventsScreenUiState.Success)?.events ?: emptyList()
        val accumulatedEvents = if (page == 0) newEvents else previousEvents + newEvents

        return EventsScreenUiState.Success(
            events = accumulatedEvents,
            page = page,
            onLoadNextPage = { loadNextPage() },
            isLoadingMore = false,
            paginationError = null
        )
    }

    private fun buildErrorOrPaginatedErrorState(page: Int, errorMessage: String): EventsScreenUiState {
        return if (page == 0) {
            EventsScreenUiState.Error(errorMessage)
        } else {
            val previousEvents = (eventsScreenUiState.value as? EventsScreenUiState.Success)?.events ?: emptyList()
            EventsScreenUiState.Success(
                events = previousEvents,
                page = page - 1,
                onLoadNextPage = { loadNextPage() },
                isLoadingMore = false,
                paginationError = errorMessage
            )
        }
    }

    private fun loadNextPage() {
        val currentState = eventsScreenUiState.value
        if (currentState is EventsScreenUiState.Success) {
            viewModelScope.launch {
                loadNextPageSignal.emit(currentState.page + 1)
            }
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