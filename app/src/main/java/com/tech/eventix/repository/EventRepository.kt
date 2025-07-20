package com.tech.eventix.repository

import com.tech.eventix.domain.Event
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface EventRepository {
     fun getEvents(page: Int, size: Int): Flow<ResultState<List<Event>>>
} 