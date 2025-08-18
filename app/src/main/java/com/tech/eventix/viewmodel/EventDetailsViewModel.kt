package com.tech.eventix.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.eventix.domain.EventDetail
import com.tech.eventix.uistate.EventDetailUiState
import com.tech.eventix.uistate.EventDetailsScreenUiState
import com.tech.eventix.usecase.GetEventDetailsUseCase
import com.tech.eventix.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventDetailsUseCase: GetEventDetailsUseCase
) : ViewModel() {

    private val eventId: String? = savedStateHandle[EVENT_ID_KEY]

    val uiState: StateFlow<EventDetailsScreenUiState> = createUiStateStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = EventDetailsScreenUiState.Loading
        )

    private fun createUiStateStream(): Flow<EventDetailsScreenUiState> {
        val id = eventId ?: return flowOf(EventDetailsScreenUiState.Error("Event ID not provided"))

        return getEventDetailsUseCase(id).map { result ->
            when (result) {
                is ResultState.Success -> EventDetailsScreenUiState.Success(result.data.toUiState())
                is ResultState.Error -> EventDetailsScreenUiState.Error(result.getErrorMessage())
            }
        }
    }

    private fun EventDetail.toUiState(): EventDetailUiState {
        val dateTimeCombined = listOf(date, time).filter { it.isNotEmpty() }.joinToString(", ")
        
        val location = listOfNotNull(venue?.name, venue?.city)
            .filter { it.isNotEmpty() }
            .joinToString(", ")

        return EventDetailUiState(
            name = name,
            image = imageUrl.orEmpty(),
            dateTime = dateTimeCombined,
            location = location,
            price = price,
            info = info,
            seatmapUrl = seatmapUrl,
            products = products,
            genre = genre,
            ticketLimit = ticketLimit,
            ageRestrictions = ageRestrictions
        )
    }

    companion object {
        const val EVENT_ID_KEY = "eventId"
    }
}