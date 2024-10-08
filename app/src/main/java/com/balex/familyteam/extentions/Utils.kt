package com.balex.familyteam.extentions

fun String.formatStringFirstLetterUppercase(): String {
    return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}