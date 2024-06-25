package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Language(
    val symbol: String,
    val description: String
): Parcelable {
    companion object {
        val DEFAULT_LANGUAGE = Language("en", "English")
    }
}
