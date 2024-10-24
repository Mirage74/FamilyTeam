package com.balex.common.extensions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.Task
import com.balex.common.domain.entity.ToDoList
import com.balex.common.domain.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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


fun User.calendarLastTimeAvailableFCMWasUpdated(): Calendar {
    return Calendar.getInstance().apply {
        time = Date(this@calendarLastTimeAvailableFCMWasUpdated.lastTimeAvailableFCMWasUpdated)
    }
}



fun Task.checkData(): Boolean {
    var isCompareAlarm1AndAlarm2Correct = true
    var isCompareAlarm1AndAlarm3Correct = true
    var isCompareAlarm2AndAlarm3Correct = true
    var isCompareAlarmAndCutoffTimeCorrect1 = true
    var isCompareAlarmAndCutoffTimeCorrect2 = true
    var isCompareAlarmAndCutoffTimeCorrect3 = true

    if (this.alarmTime1 != Task.NO_ALARM) {
        isCompareAlarmAndCutoffTimeCorrect1 =
            this.cutoffTime - this.alarmTime1 >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MILLIS
    }

    if (this.alarmTime1 != Task.NO_ALARM) {
        isCompareAlarmAndCutoffTimeCorrect2 =
            this.cutoffTime - this.alarmTime2 >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MILLIS
    }

    if (this.alarmTime1 != Task.NO_ALARM) {
        isCompareAlarmAndCutoffTimeCorrect3 =
            this.cutoffTime - this.alarmTime3 >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MILLIS
    }

    if ((this.alarmTime1 != Task.NO_ALARM) && (this.alarmTime2 != Task.NO_ALARM)) {
        isCompareAlarm1AndAlarm2Correct =
            abs(this.alarmTime1 - this.alarmTime2) >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MILLIS
    }

    if ((this.alarmTime1 != Task.NO_ALARM) && (this.alarmTime3 != Task.NO_ALARM)) {
        isCompareAlarm1AndAlarm3Correct =
            abs(this.alarmTime1 - this.alarmTime3) >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MILLIS
    }

    if ((this.alarmTime2 != Task.NO_ALARM) && (this.alarmTime3 != Task.NO_ALARM)) {
        isCompareAlarm2AndAlarm3Correct =
            abs(this.alarmTime2 - this.alarmTime3) >= Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MILLIS
    }

    return this.cutoffTime - System.currentTimeMillis() >= Task.MIN_CUTOFF_TIME_FROM_NOW_IN_MILLIS &&
            isCompareAlarmAndCutoffTimeCorrect1 && isCompareAlarmAndCutoffTimeCorrect2 && isCompareAlarmAndCutoffTimeCorrect3 &&
            isCompareAlarm1AndAlarm2Correct && isCompareAlarm1AndAlarm3Correct && isCompareAlarm2AndAlarm3Correct
}

fun Task.toExternalTask(taskOwner: String): ExternalTask {
    return ExternalTask(this, taskOwner)
}

fun PrivateTasks.toExternalTasks(taskOwner: String): ExternalTasks {
    return ExternalTasks(externalTasks = this.privateTasks.map { it.toExternalTask(taskOwner) })
}

fun ToDoList.allMyTasks(myNickName: String): ExternalTasks {
    return ExternalTasks(
        externalTasks = this.thingsToDoShared.externalTasks.toMutableList().apply {addAll(this@allMyTasks.thingsToDoPrivate.toExternalTasks(
            myNickName
        ).externalTasks)}.sortedBy { it.task.cutoffTime })

//        externalTasks = this.thingsToDoShared.externalTasks.union(
//            this.thingsToDoPrivate.toExternalTasks(
//                myNickName
//            ).externalTasks
//        ).toList().sortedBy { it.task.cutoffTime })
}