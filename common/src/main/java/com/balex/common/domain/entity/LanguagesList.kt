package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class LanguagesList(
    val languages: List<Language> = listOf(
        Language("en", "English"),
        Language("de", "Deutsch"),
        Language("fr", "Français"),
        Language("it", "Italiano"),
        Language("es", "Español"),
        Language("ru", "Русский")
    )
)
