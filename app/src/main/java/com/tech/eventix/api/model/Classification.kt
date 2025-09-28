package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Classification(
    val primary: Boolean? = null,
    val segment: IdName? = null,
    val genre: IdName? = null,
    val subGenre: IdName? = null,
    val type: IdName? = null,
    val subType: IdName? = null,
    val family: Boolean? = null
)

@Serializable
data class IdName(
    val id: String? = null,
    val name: String? = null
)

