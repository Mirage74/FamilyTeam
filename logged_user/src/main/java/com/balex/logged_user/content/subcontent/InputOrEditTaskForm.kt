package com.balex.logged_user.content.subcontent

import android.content.Context
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.data.repository.TaskMode
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.common.R as commonR
import com.balex.logged_user.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputOrEditTaskForm(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    isMyTask: Boolean,
    taskMode: TaskMode,
    deviceToken: String,
    context: Context,
) {

    if (state.user.availableTasksToAdd > 0 || state.isEditTaskClicked) {


        var description by remember { mutableStateOf(TextFieldValue("")) }
        var selectedDateInMillis by remember { mutableLongStateOf(0L) }
        var selectedTimeInMillis by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisDate1 by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisTime1 by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisDate2 by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisTime2 by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisDate3 by remember { mutableLongStateOf(0L) }
        var selectedAlarmInMillisTime3 by remember { mutableLongStateOf(0L) }
        var isCheckBoxSelected1 by remember { mutableStateOf(false) }
        var isCheckBoxSelected2 by remember { mutableStateOf(false) }
        var isCheckBoxSelected3 by remember { mutableStateOf(false) }

        var expanded by remember { mutableStateOf(false) }
        var selectedUser by remember { mutableStateOf<String?>(null) }

        var reminderInMillis1 =
            context.resources.getInteger(R.integer.alarm_default_in_min_1) * MILLIS_IN_MINUTE
        var reminderInMillis2 =
            context.resources.getInteger(R.integer.alarm_default_in_hour_2) * MILLIS_IN_HOUR
        var reminderInMillis3 =
            context.resources.getInteger(R.integer.alarm_default_in_days_3) * MILLIS_IN_DAY

        val errorInputText = context.getString(
            R.string.wrong_input_data_for_one_task,
            Task.MIN_CUTOFF_TIME_FROM_NOW_IN_MINUTES,
            Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MINUTES,
            Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MINUTES
        )

        Column(

            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            var currentCutoff = reminderInMillis3 + MILLIS_IN_DAY
            if (taskMode == TaskMode.EDIT) {
                description = description.copy(text = state.taskForEdit.task.description)
                currentCutoff = state.taskForEdit.task.cutoffTime
                selectedUser = state.taskForEdit.taskOwner

                if (state.taskForEdit.task.alarmTime1 != Task.NO_ALARM) {
                    reminderInMillis1 = state.taskForEdit.task.alarmTime1
                    isCheckBoxSelected1 = true
                }
                if (state.taskForEdit.task.alarmTime2 != Task.NO_ALARM) {
                    reminderInMillis2 = state.taskForEdit.task.alarmTime1
                    isCheckBoxSelected2 = true
                }
                if (state.taskForEdit.task.alarmTime3 != Task.NO_ALARM) {
                    reminderInMillis3 = state.taskForEdit.task.alarmTime1
                    isCheckBoxSelected3 = true
                }
            }

            GreetingRow(state)

            val usersListWithoutMe = state.usersNicknamesList.filter { it != state.user.nickName }

            if (state.isWrongTaskData) {
                Text(
                    text = errorInputText,
                    color = Color.Red
                )
            }

            Text(context.getString(R.string.task_description))
            OutlinedTextField(
                value = description,
                onValueChange = {
                    if (it.text.length <= context.resources.getInteger(R.integer.max_task_description_length)) {
                        description = it
                    }
                },
                label = { Text(context.getString(R.string.task_description)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            DateAndTimePickers(
                isEditMode = taskMode == TaskMode.EDIT,
                shiftTimeInMillis = currentCutoff,
                onDateSelected = { selectedDateInMillis = it },
                onTimeSelected = { selectedTimeInMillis = it },
                textTitle = context.getString(R.string.select_date_and_time_for_task),
                showCheckBox = NO_CHECK_BOX,
                isCheckBoxSelected = true,
                context = context
            )


            if (state.user.availableFCM > 0) {
                DateAndTimePickers(
                    isEditMode = taskMode == TaskMode.EDIT,
                    shiftTimeInMillis = reminderInMillis1,
                    onDateSelected = { selectedAlarmInMillisDate1 = it },
                    onTimeSelected = { selectedAlarmInMillisTime1 = it },
                    onCheck = { isCheckBoxSelected1 = it },
                    textTitle = context.getString(R.string.select_date_and_time_for_alarm1),
                    showCheckBox = YES_CHECK_BOX,
                    isCheckBoxSelected = isCheckBoxSelected1,
                    context = context
                )
            }

            if (state.user.availableFCM > 1) {
                DateAndTimePickers(
                    isEditMode = taskMode == TaskMode.EDIT,
                    shiftTimeInMillis = reminderInMillis2,
                    onDateSelected = { selectedAlarmInMillisDate2 = it },
                    onTimeSelected = { selectedAlarmInMillisTime2 = it },
                    onCheck = { isCheckBoxSelected2 = it },
                    textTitle = context.getString(R.string.select_date_and_time_for_alarm2),
                    showCheckBox = YES_CHECK_BOX,
                    isCheckBoxSelected = isCheckBoxSelected2,
                    context = context
                )
            }


            if (state.user.availableFCM > 2) {
                DateAndTimePickers(
                    isEditMode = taskMode == TaskMode.EDIT,
                    shiftTimeInMillis = reminderInMillis3,
                    onDateSelected = { selectedAlarmInMillisDate3 = it },
                    onTimeSelected = { selectedAlarmInMillisTime3 = it },
                    onCheck = { isCheckBoxSelected3 = it },
                    textTitle = context.getString(R.string.select_date_and_time_for_alarm3),
                    showCheckBox = YES_CHECK_BOX,
                    isCheckBoxSelected = isCheckBoxSelected3,
                    context = context
                )
            }

            if (!isMyTask) {
                if (taskMode == TaskMode.ADD) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedUser ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("choose user") },
                            modifier = Modifier.menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            usersListWithoutMe.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user) },
                                    onClick = {
                                        selectedUser = user
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(text = state.taskForEdit.taskOwner )
                }
            }


            Button(
                onClick = {

                    var task = if (taskMode == TaskMode.ADD) {
                        Task(
                            description = description.text,
                            cutoffTime = selectedDateInMillis + selectedTimeInMillis
                        )
                    } else {
                        Task(
                            id = state.taskForEdit.task.id,
                            description = description.text,
                            cutoffTime = selectedDateInMillis + selectedTimeInMillis
                        )
                    }
                    if (isCheckBoxSelected1) {
                        task =
                            task.copy(
                                alarmTime1 = selectedAlarmInMillisDate1 + selectedAlarmInMillisTime1
                            )
                    }
                    if (isCheckBoxSelected2) {
                        task =
                            task.copy(alarmTime2 = selectedAlarmInMillisDate2 + selectedAlarmInMillisTime2)
                    }
                    if (isCheckBoxSelected3) {
                        task =
                            task.copy(alarmTime3 = selectedAlarmInMillisDate3 + selectedAlarmInMillisTime3)
                    }
                    if (isMyTask) {
                        component.onClickAddNewTaskOrEditForMeToFirebase(task.copy(), taskMode, deviceToken)
                    } else {
                        val otherUser = selectedUser
                        if (otherUser != null) {
                            val externalTask = ExternalTask(
                                task = task,
                                taskOwner = otherUser
                            )
                            component.onClickAddNewTaskOrEditForOtherUserToFirebase(
                                externalTask.copy(),
                                taskMode,
                                deviceToken
                            )
                        }
                    }

                },
                modifier = Modifier.align(Alignment.End)
            ) {
                val textButton = if (taskMode == TaskMode.ADD) {
                    context.getString(R.string.add_button_text)
                } else {
                    context.getString(R.string.save_edited_task_button_text)
                }
                Text(textButton)
            }
        }
    } else {
        Text(context.getString(R.string.no_available_tasks))
    }
}


@Composable
fun DateAndTimePickers(
    isEditMode: Boolean = false,
    shiftTimeInMillis: Long,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Long) -> Unit,
    onCheck: ((Boolean) -> Unit)? = null,
    textTitle: String,
    showCheckBox: Boolean,
    isCheckBoxSelected: Boolean = false,
    context: Context
) {
    Text(textTitle)
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        //.padding(horizontal = dimensionResource(id = R.dimen.time_padding_size).value.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        var initialDate = Calendar.getInstance(TimeZone.getDefault()).timeInMillis
        initialDate += context.resources.getInteger(R.integer.task_default_in_min) * MILLIS_IN_MINUTE + shiftTimeInMillis


        if (isEditMode && isCheckBoxSelected) {
            initialDate = shiftTimeInMillis
        }


        var isChecked by remember { mutableStateOf(isCheckBoxSelected) }
        if (showCheckBox) {

            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onCheck?.invoke(it)
                }
            )
        }

        DatePickerFieldToModal(
            defaultDateInMillis = initialDate,
            onDateSelected = onDateSelected,
            context = context,
            modifier = Modifier
                .weight(4f)
                .padding(end = dimensionResource(id = R.dimen.time_padding_size).value.dp)
        )
        TimePickerForNewTask(
            defaultTimeInMillis = initialDate % MILLIS_IN_DAY,
            onTimeSelected = onTimeSelected,
            modifier = Modifier
                .weight(3f),
            context = context
        )
    }
}

@Composable
fun DatePickerFieldToModal(
    defaultDateInMillis: Long,
    onDateSelected: (Long) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableLongStateOf(defaultDateInMillis) }
    var showModal by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = selectedDate.let {
            onDateSelected(it)
            convertMillisToDate(it)
        } ?: "",
        maxLines = 1,
        singleLine = true,
        onValueChange = { },

        textStyle = TextStyle(
            fontSize = dimensionResource(id = R.dimen.alarm_date_and_time_text_size).value.sp
        ),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            //.fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            defaultDateInMillis = selectedDate,
            onDateSelected = {
                focusManager.clearFocus()
                if (it != null) {
                    selectedDate = it
                }

            },
            onDismiss = {
                focusManager.clearFocus()
                showModal = false
            },
            context = context
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    defaultDateInMillis: Long,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val datePickerState = rememberDatePickerState(defaultDateInMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(context.getString(commonR.string.button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(commonR.string.button_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun convertMillisToTime(millis: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerForNewTask(
    defaultTimeInMillis: Long,
    onTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    var selectedTime by remember { mutableLongStateOf(defaultTimeInMillis) }
    var showDialWithDialog by remember { mutableStateOf(false) }



    OutlinedTextField(
        value = selectedTime.let {
            onTimeSelected(it)
            convertMillisToTime(it)
        } ?: "",
        maxLines = 1,
        singleLine = true,
        onValueChange = { },
        textStyle = TextStyle(
            fontSize = dimensionResource(id = R.dimen.alarm_date_and_time_text_size).value.sp
        ),
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select time")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedTime) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showDialWithDialog = true
                    }
                }
            }
    )

    if (showDialWithDialog) {
        DialWithDialog(
            defaultTimeInMillis = selectedTime,
            onDismiss = {
                showDialWithDialog = false

            },
            onConfirm = { time ->
                selectedTime = time.hour * MILLIS_IN_HOUR + time.minute * MILLIS_IN_MINUTE
                showDialWithDialog = false

            },
            context = context
        )
    }

}

private fun getCurrentHour(dateInMillis: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateInMillis
    }
    return calendar.get(Calendar.HOUR_OF_DAY)
}

private fun getCurrentMinute(dateInMillis: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateInMillis
    }
    return calendar.get(Calendar.MINUTE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialog(
    defaultTimeInMillis: Long,
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    //val currentTime = Calendar.getInstance()
    val focusManager = LocalFocusManager.current

    val timePickerState = rememberTimePickerState(
        initialHour = getCurrentHour(defaultTimeInMillis),
        initialMinute = getCurrentMinute(defaultTimeInMillis),
        is24Hour = true
    )

    TimePickerDialog(
        onDismiss = {
            onDismiss()
            focusManager.clearFocus()
        },
        onConfirm = {
            onConfirm(timePickerState)
            focusManager.clearFocus()
        },
        context = context
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    context: Context,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(context.getString(commonR.string.button_dismiss))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(context.getString(commonR.string.button_ok))
            }
        },
        text = { content() }
    )

}

const val MILLIS_IN_MINUTE = 60 * 1_000L
const val MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60
const val MILLIS_IN_DAY = MILLIS_IN_HOUR * 24
const val NO_CHECK_BOX = false
const val YES_CHECK_BOX = true

