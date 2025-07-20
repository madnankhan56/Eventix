package com.tech.eventix.api.model

data class Root(
    val _embedded: RootEmbedded,
    val _links: RootLinks,
    val page: Page
){
    fun getEvents() = _embedded.events

    fun getLinks() = _links
}

data class RootEmbedded(
    val events: List<NetworkEvent>
)

data class RootLinks(
    val first: Link,
    val prev: Link,
    val self: Link,
    val next: Link,
    val last: Link
)

data class Page(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)