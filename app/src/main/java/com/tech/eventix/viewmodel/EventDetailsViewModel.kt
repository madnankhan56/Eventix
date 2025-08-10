package com.tech.eventix.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.eventix.uistate.EventDetailsScreenUiState
import com.tech.eventix.domain.EventDetail
import com.tech.eventix.usecase.GetEventDetailsUseCase
import com.tech.eventix.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEventDetailsUseCase: GetEventDetailsUseCase
) : ViewModel() {

    private val eventId: String? = savedStateHandle[EVENT_ID_KEY]

    private val _uiState = MutableStateFlow<EventDetailsScreenUiState>(EventDetailsScreenUiState.Loading)
    val uiState: StateFlow<EventDetailsScreenUiState> = _uiState.asStateFlow()

    init {
        if (eventId == null) {
            _uiState.value = EventDetailsScreenUiState.Error("Event ID not provided")
        } else {
            fetchDetails()
        }
    }

    fun retry() {
        fetchDetails()
    }

    private fun fetchDetails() {
        _uiState.value = EventDetailsScreenUiState.Loading
        val id = eventId ?: return
        viewModelScope.launch {
            getEventDetailsUseCase(id).collectLatest { result ->
                when (result) {
                    is ResultState.Success -> _uiState.value = EventDetailsScreenUiState.Success(result.data)
                    is ResultState.Error -> _uiState.value = EventDetailsScreenUiState.Error(result.getErrorMessage())
                }
            }
        }
    }

    companion object {
        const val EVENT_ID_KEY = "eventId"
    }
}