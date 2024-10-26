package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class LanguagesList(
    @SerializedName("languages")
    val languages: List<Language> = listOf(
        Language("en", "English"),
        Language("de", "Deutsch"),
        Language("fr", "Français"),
        Language("it", "Italiano"),
        Language("es", "Español"),
        Language("ru", "Русский")
    )
)
