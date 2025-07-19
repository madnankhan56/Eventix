package com.tech.eventix.repository

import com.tech.eventix.domain.Event
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun getEvents(page: Int? = null, size: Int? = null): Flow<ResultState<List<Event>>>
} 