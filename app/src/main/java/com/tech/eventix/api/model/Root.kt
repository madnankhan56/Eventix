package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Root(
    val _embedded: RootEmbedded,
    val _links: RootLinks,
    val page: Page
){
    fun getEvents() = _embedded.events

    fun getLinks() = _links
}

@Serializable
data class RootEmbedded(
    val events: List<NetworkEvent>
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
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)