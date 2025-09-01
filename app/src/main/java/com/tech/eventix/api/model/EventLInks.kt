package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EventLinks(
    val self: Link,
    val attractions: List<Link>?,
    val venues: List<Link>?
)

@Serializable
data class Link(
    val href: String
)