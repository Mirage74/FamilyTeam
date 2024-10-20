package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val description: String = "",
    val cutoffTime: Long = 0,
    val alarmTime1: Long = NO_ALARM,
    val alarmTime2: Long = NO_ALARM,
    val alarmTime3: Long = NO_ALARM
) {
    companion object {
        const val NO_ALARM = -1L
        const val MIN_CUTOFF_TIME_FROM_NOW_IN_MINUTES = 30
        const val MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MINUTES = 5
        const val MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MINUTES = 60
        private const val MILLIS_IN_MINUTE = 60_000L
        const val MIN_CUTOFF_TIME_FROM_NOW_IN_MILLIS =
            MIN_CUTOFF_TIME_FROM_NOW_IN_MINUTES * MILLIS_IN_MINUTE
        const val MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MILLIS =
            MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MINUTES * MILLIS_IN_MINUTE
        const val MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MILLIS =
            MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MINUTES * MILLIS_IN_MINUTE
    }
}
