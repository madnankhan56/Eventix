package com.tech.eventix.api.model

data class EventLinks(
    val self: Link,
    val attractions: List<Link>?,
    val venues: List<Link>?
)

data class Link(
    val href: String
)