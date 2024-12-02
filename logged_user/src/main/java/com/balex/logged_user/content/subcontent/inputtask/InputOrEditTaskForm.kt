package com.balex.logged_user.content.subcontent.inputtask

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.balex.common.data.repository.TaskMode
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.logged_user.content.subcontent.GreetingRow
import java.util.Calendar
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


        var isCheckBoxSelected1 by remember { mutableStateOf(state.taskForEdit.task.alarmTime1 != Task.NO_ALARM) }
        var isCheckBoxSelected2 by remember { mutableStateOf(state.taskForEdit.task.alarmTime2 != Task.NO_ALARM) }
        var isCheckBoxSelected3 by remember { mutableStateOf(state.taskForEdit.task.alarmTime3 != Task.NO_ALARM) }

        var expanded by remember { mutableStateOf(false) }
        var selectedUser by remember { mutableStateOf<String?>(null) }


        var reminderInMillis1 =
            context.resources.getInteger(R.integer.alarm_default_in_days_3) * MILLIS_IN_DAY
        var reminderInMillis2 =
            context.resources.getInteger(R.integer.alarm_default_in_hour_2) * MILLIS_IN_HOUR
        var reminderInMillis3 =
            context.resources.getInteger(R.integer.alarm_default_in_min_1) * MILLIS_IN_MINUTE

        val errorInputText = context.getString(
            R.string.wrong_input_data_for_one_task,
            Task.MIN_CUTOFF_TIME_FROM_NOW_IN_MINUTES,
            Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIME_AND_ALARMS_IN_MINUTES,
            Task.MIN_DIFFERENCE_BETWEEN_CUTOFF_TIMES_IN_MINUTES
        )

        val initialDate by remember {
            mutableLongStateOf(Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis)
        }

        var currentCutoff =
            context.resources.getInteger(R.integer.alarm_default_in_days_3) * MILLIS_IN_DAY + MILLIS_IN_DAY + context.resources.getInteger(
                R.integer.task_default_in_min
            ) * MILLIS_IN_MINUTE

        var selectedDateInMillis by remember { mutableLongStateOf((initialDate + currentCutoff) / MILLIS_IN_DAY * MILLIS_IN_DAY) }
        var selectedTimeInMillis by remember { mutableLongStateOf((initialDate + currentCutoff) % MILLIS_IN_DAY) }

        var selectedAlarmInMillisDate1 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis1) / MILLIS_IN_DAY * MILLIS_IN_DAY) }
        var selectedAlarmInMillisTime1 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis1) % MILLIS_IN_DAY) }
        var selectedAlarmInMillisDate2 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis2) / MILLIS_IN_DAY * MILLIS_IN_DAY) }
        var selectedAlarmInMillisTime2 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis2) % MILLIS_IN_DAY) }
        var selectedAlarmInMillisDate3 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis3) / MILLIS_IN_DAY * MILLIS_IN_DAY) }
        var selectedAlarmInMillisTime3 by remember { mutableLongStateOf((selectedDateInMillis + reminderInMillis3) % MILLIS_IN_DAY) }

        var description by remember {
            mutableStateOf(
                if (taskMode == TaskMode.EDIT) {
                    TextFieldValue(state.taskForEdit.task.description)
                } else {
                    TextFieldValue("")
                }
            )
        }


        if (taskMode == TaskMode.EDIT) {

            currentCutoff = state.taskForEdit.task.cutoffTime
            selectedUser = state.taskForEdit.taskOwner

            if (state.taskForEdit.task.alarmTime1 != Task.NO_ALARM) {
                reminderInMillis1 = state.taskForEdit.task.alarmTime1
            }
            if (state.taskForEdit.task.alarmTime2 != Task.NO_ALARM) {
                reminderInMillis2 = state.taskForEdit.task.alarmTime2
            }
            if (state.taskForEdit.task.alarmTime3 != Task.NO_ALARM) {
                reminderInMillis3 = state.taskForEdit.task.alarmTime3
            }
        }

        Column(

            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {




            GreetingRow(state.user.nickName, state.user.displayName)

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
                initialDate,
                isEditMode = taskMode == TaskMode.EDIT,
                shiftTimeInMillis = currentCutoff,
                onDateSelected = { selectedDate ->
                    selectedDateInMillis = selectedDate
                },
                onTimeSelected = { selectedTime ->
                    selectedTimeInMillis = selectedTime
                },
                textTitle = context.getString(R.string.select_date_and_time_for_task),
                showCheckBox = NO_CHECK_BOX,
                isCheckBoxSelected = true,
                context = context
            )


            if (state.user.availableFCM > 0) {
                DateAndTimePickers(
                    initialDate,
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
                    initialDate,
                    isEditMode = taskMode == TaskMode.EDIT,
                    shiftTimeInMillis = reminderInMillis2,
                    onDateSelected = { selectedAlarmInMillisDate2 = it },
                    onTimeSelected = { selectedAlarmInMillisTime2 = it },
                    onCheck = {
                        isCheckBoxSelected2 = it
                    },
                    textTitle = context.getString(R.string.select_date_and_time_for_alarm2),
                    showCheckBox = YES_CHECK_BOX,
                    isCheckBoxSelected = isCheckBoxSelected2,
                    context = context
                )
            }


            if (state.user.availableFCM > 2) {
                DateAndTimePickers(
                    initialDate,
                    isEditMode = taskMode == TaskMode.EDIT,
                    shiftTimeInMillis = reminderInMillis3,
                    onDateSelected = {
                        selectedAlarmInMillisDate3 = it
                    },
                    onTimeSelected = {
                        selectedAlarmInMillisTime3 = it
                    },
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
                    Text(text = state.taskForEdit.taskOwner)
                }
            }


            Button(
                enabled = description.text.isNotBlank(),
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
                        component.onClickAddNewTaskOrEditForMeToFirebase(
                            task.copy(),
                            taskMode,
                            deviceToken
                        )
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

const val MILLIS_IN_MINUTE = 60 * 1_000L
const val MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60
const val MILLIS_IN_DAY = MILLIS_IN_HOUR * 24
const val NO_CHECK_BOX = false
const val YES_CHECK_BOX = true

