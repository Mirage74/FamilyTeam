package com.balex.logged_user.content

import android.content.Context
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.balex.common.R
import com.balex.common.domain.entity.Task
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun InputMyNewTaskForm(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    context: Context
) {

    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDateInMillis by remember { mutableStateOf(0L) }
    var selectedTimeInMillis by remember { mutableStateOf(0L) }
    var selectedAlarmInMillis1 by remember { mutableStateOf(0L) }
    var selectedAlarmInMillis2 by remember { mutableStateOf(0L) }
    var selectedAlarmInMillis3 by remember { mutableStateOf(0L) }

    val errorInputText = context.getString(
        R.string.wrong_input_data_for_one_task,
        Task.MIN_CUTOFF_TIME_FROM_NOW_IN_MINUTES,
        Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MINUTES,
        Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MINUTES
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = {
                if (it.text.length <= 30) {
                    description = it
                }
            },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Open modal picker on click")
        DatePickerFieldToModal(onDateSelected = {
            selectedDateInMillis = it
        })
        TimePickerForNewTask(
            onTimeSelected = {
                selectedTimeInMillis = it
            },
            onAlarmTimeSelected1 = {
                selectedAlarmInMillis1 = it
            },
            onAlarmTimeSelected2 = {
                selectedAlarmInMillis2 = it
            },
            onAlarmTimeSelected3 = {
                selectedAlarmInMillis3 = it
            }
        )

        Button(
            onClick = {
//                val task = Task()
//                component.onClickAddNewTaskForMeToFirebase(task)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("OK")
        }
    }
}


@Composable
fun DatePickerFieldToModal(onDateSelected: (Long) -> Unit, modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let {
            onDateSelected(it)
            convertMillisToDate(it)
        } ?: "",
        onValueChange = { },
        placeholder = { Text("MM/DD/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
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
            onDateSelected = { selectedDate = it },
            onDismiss = { showModal = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun convertMillisToTime(millis: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
fun getTimeInMillis(timeState: TimePickerState?): Long {
    return if (timeState == null) {
        0
    } else {
        timeState.hour * MILLIS_IN_HOUR + timeState.minute * MILLIS_IN_MINUTE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerForNewTask(
    onTimeSelected: (Long) -> Unit,
    onAlarmTimeSelected1: (Long) -> Unit,
    onAlarmTimeSelected2: (Long) -> Unit,
    onAlarmTimeSelected3: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialWithDialog by remember { mutableStateOf(false) }
    //var selectedTime: TimePickerState? by remember { mutableStateOf(null) }
    var selectedTime by remember { mutableStateOf<Long?>(null) }


    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            OutlinedTextField(
                value = selectedTime?.let {
                    onTimeSelected(it)
                    convertMillisToTime(it)
                } ?: "",
                onValueChange = { },
                placeholder = { Text("HH:MM") },
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


        }


        if (showDialWithDialog) {
            DialWithDialogExample(
                onDismiss = {
                    showDialWithDialog = false

                },
                onConfirm = { time ->
                    selectedTime = time.hour * MILLIS_IN_HOUR + time.minute * MILLIS_IN_MINUTE
                    showDialWithDialog = false

                },
            )
        }
    }
}

private fun getCurrentHourPlusOne(): Int {
    val calendar = Calendar.getInstance()
    return (calendar.get(Calendar.HOUR_OF_DAY) + 1) % 24
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialogExample(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = getCurrentHourPlusOne(),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
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
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() }
    )
}

const val MILLIS_IN_MINUTE = 60_000L
const val MILLIS_IN_HOUR = 3_600_000L