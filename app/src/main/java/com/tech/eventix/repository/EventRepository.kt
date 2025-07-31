package com.tech.eventix.repository

import com.tech.eventix.domain.Event
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface EventRepository {
     fun getEvents(page: Int, size: Int, keyword: String? = null): Flow<ResultState<List<Event>>>
} 