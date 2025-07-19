package com.tech.eventix.api.model

data class Classification(
    val primary: Boolean,
    val segment: IdName,
    val genre: IdName,
    val subGenre: IdName,
    val type: IdName,
    val subType: IdName,
    val family: Boolean
)

data class IdName(
    val id: String,
    val name: String
)

