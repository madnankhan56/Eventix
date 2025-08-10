package com.tech.eventix.domain

data class Event(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val date: String,
    val time: String,
    val venue: Venue?,
    val test: Boolean
) 