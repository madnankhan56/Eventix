package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Root(
    val _embedded: RootEmbedded? = null,
    val _links: RootLinks? = null,
    val page: Page? = null
){
    fun getEvents() = _embedded?.events ?: emptyList()

    fun getLinks() = _links
}

@Serializable
data class RootEmbedded(
    val events: List<NetworkEvent> = emptyList()
)

@Serializable
data class RootLinks(
    val first: Link? = null,
    val prev: Link? = null,
    val self: Link? = null,
    val next: Link? = null,
    val last: Link? = null
)

@Serializable
data class Page(
    val size: Int? = null,
    val totalElements: Int? = null,
    val totalPages: Int? = null,
    val number: Int? = null
)