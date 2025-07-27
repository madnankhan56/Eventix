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

    // Used to run flows on init and also on command
    private val loadEventSignal: Flow<Int> = flow {
        emit(0)
        emitAll(loadNextPageSignal)
    }

    val eventsScreenUiState: StateFlow<EventsScreenUiState> = createEventUiStateStream().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = EventsScreenUiState.Loading
    )

    private fun createEventUiStateStream(): Flow<EventsScreenUiState> {
        return loadEventSignal.transform { page ->
            // Emit loading state first

            val currentState = eventsScreenUiState.value
            if (currentState is EventsScreenUiState.Success) {
                emit(currentState.copy(isLoadingMore = true, paginationError = null))
            }

            
            // Then emit the API result
            emitAll(getEventsUseCase(page = page).map { result ->
                when (result) {
                    is ResultState.Success -> {
                        val newEvents = result.data.map { it.toUiState() }
                        val previousEvents = when (val currentState = eventsScreenUiState.value) {
                            is EventsScreenUiState.Success -> currentState.events
                            else -> emptyList()
                        }
                        val accumulatedEvents = if (page == 0) newEvents else previousEvents + newEvents
                        
                        EventsScreenUiState.Success(
                            events = accumulatedEvents,
                            page = page,
                            onLoadNextPage = {
                                val currentState = eventsScreenUiState.value
                                if (currentState is EventsScreenUiState.Success) {
                                    viewModelScope.launch {
                                        loadNextPageSignal.emit(currentState.page + 1)
                                    }
                                }
                            },
                            isLoadingMore = false,
                            paginationError = null
                        )
                    }
                    is ResultState.Error -> {
                        if (page == 0) {
                            EventsScreenUiState.Error(result.getErrorMessage())
                        } else {
                            // Keep existing events but show pagination error
                            val previousEvents = when (val currentState = eventsScreenUiState.value) {
                                is EventsScreenUiState.Success -> currentState.events
                                else -> emptyList()
                            }
                            EventsScreenUiState.Success(
                                events = previousEvents,
                                page = page - 1,
                                onLoadNextPage = {
                                    val currentState = eventsScreenUiState.value
                                    if (currentState is EventsScreenUiState.Success) {
                                        viewModelScope.launch {
                                            loadNextPageSignal.emit(currentState.page + 1)
                                        }
                                    }
                                },
                                isLoadingMore = false,
                                paginationError = result.getErrorMessage()
                            )
                        }
                    }
                }
            })
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