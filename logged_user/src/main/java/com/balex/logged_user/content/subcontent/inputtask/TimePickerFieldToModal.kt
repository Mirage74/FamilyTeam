package com.balex.logged_user.content.subcontent.inputtask

import android.content.Context
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.balex.logged_user.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFieldToModal(
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
                Text(context.getString(com.balex.common.R.string.button_dismiss))
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(context.getString(com.balex.common.R.string.button_ok))
            }
        },
        text = { content() }
    )
}

private fun convertMillisToTime(millis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun getCurrentHour(dateInMillis: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("UTC")
        timeInMillis = dateInMillis
    }
    return calendar.get(Calendar.HOUR_OF_DAY)
}

private fun getCurrentMinute(dateInMillis: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("UTC")
        timeInMillis = dateInMillis
    }
    return calendar.get(Calendar.MINUTE)
}