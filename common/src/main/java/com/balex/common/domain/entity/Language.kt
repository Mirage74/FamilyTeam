package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val symbol: String,
    val description: String
) {
    companion object {
        val DEFAULT_LANGUAGE = Language("en", "English")
    }
}
