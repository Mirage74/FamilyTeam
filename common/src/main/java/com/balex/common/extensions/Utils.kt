package com.balex.common.extensions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.balex.common.domain.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun ComponentContext.componentScope() = CoroutineScope(
    Dispatchers.Main.immediate + SupervisorJob()
).apply {
    lifecycle.doOnDestroy { cancel() }
}


fun Calendar.formattedFullDate(): String {
    val format = SimpleDateFormat("EEEE | d MMM y", Locale.getDefault())
    return format.format(time)
}

fun Calendar.formattedShortDayOfWeek(): String {
    val format = SimpleDateFormat("EEE", Locale.getDefault())
    return format.format(time)
}

fun String.formatStringFirstLetterUppercase(): String {
    return this.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun String.formatStringPhoneDelLeadNullAndAddPlus(): String {
    return if (Regex(REGEX_PATTERN_ONLY_NUMBERS).matches(this)) {
        "+" + this.trimStart('0')
    } else
        this

}


fun User.calendar(): Calendar
    {
        return Calendar.getInstance().apply {
            time = Date(this@calendar.lastTimeAvailableFCMWasUpdated)
        }
    }