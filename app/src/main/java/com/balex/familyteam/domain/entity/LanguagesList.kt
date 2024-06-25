package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LanguagesList(
    val languages: List<Language> = listOf(
        Language("en", "English"),
        Language("de", "Deutsch"),
        Language("fr", "Français"),
        Language("it", "Italiano"),
        Language("es", "Español"),
        Language("ru", "Русский")
    )
): Parcelable
