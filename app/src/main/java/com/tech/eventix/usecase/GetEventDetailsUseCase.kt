package com.tech.eventix.usecase

import com.tech.eventix.domain.EventDetail
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventDetailsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(eventId: String): Flow<ResultState<EventDetail>> {
        return eventRepository.getEventDetails(eventId)
    }
}