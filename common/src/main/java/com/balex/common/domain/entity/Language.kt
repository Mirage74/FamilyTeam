package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("description")
    val description: String
) {
    companion object {
        val DEFAULT_LANGUAGE = Language("en", "English")
    }
}
