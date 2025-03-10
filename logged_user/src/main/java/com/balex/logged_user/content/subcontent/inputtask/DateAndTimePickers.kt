package com.balex.logged_user.content.subcontent.inputtask

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.balex.logged_user.R

@Composable
fun DateAndTimePickers(
    taskDateInMillis: Long,
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )
    {

        var initialDate = taskDateInMillis + shiftTimeInMillis

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
            defaultDateInMillis = initialDate / MILLIS_IN_DAY * MILLIS_IN_DAY,
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