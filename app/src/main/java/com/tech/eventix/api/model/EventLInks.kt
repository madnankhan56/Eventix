package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EventLinks(
    val self: Link? = null,
    val attractions: List<Link>? = null,
    val venues: List<Link>? = null
)

@Serializable
data class Link(
    val href: String? = null
)