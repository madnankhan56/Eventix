package com.tech.eventix.repository

import com.tech.eventix.domain.EventDomain

interface EventRepository {
    suspend fun getEvents(page: Int? = null, size: Int? = null): List<EventDomain>
} 