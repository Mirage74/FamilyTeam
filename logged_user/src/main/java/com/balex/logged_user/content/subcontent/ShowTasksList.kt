package com.balex.logged_user.content.subcontent

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.extensions.isExpired
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.content.subcontent.inputtask.convertMillisToDate
import com.balex.logged_user.content.subcontent.inputtask.convertMillisToTime

@Composable
fun ShowTasksList(
    tasks: List<ExternalTask>,
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    isMyTask: Boolean,
    deviceToken: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .padding(bottom = 32.dp)
    ) {

        items(tasks, key = { it.task.id }) { externalTask ->
            val description = externalTask.task.description
            val taskOwner = externalTask.taskOwner
            val taskDate = convertMillisToDate(externalTask.task.cutoffTime)
            val taskTime = convertMillisToTime(externalTask.task.cutoffTime)
            val taskDateAndTime = "$taskDate, $taskTime"
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(bottom = 16.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 100f) {
                                    component.onClickDeleteTask(
                                        externalTask,
                                        getTaskType(externalTask, isMyTask, state.user.nickName),
                                        deviceToken
                                    )
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX = (offsetX + dragAmount).coerceAtLeast(0f)
                            }
                        )
                    }
                    .offset { IntOffset(animatedOffsetX.dp.roundToPx(), 0) }
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()

                        drawRect(
                            color = Color.Black,
                            style = Stroke(width = strokeWidth)
                        )
                    },

                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = description,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(5f),
                    fontSize = 20.sp
                )


                Text(
                    text = taskDateAndTime,
                    modifier = Modifier
                        .weight(5f),
                    fontSize = 12.sp
                )

                Text(
                    text = taskOwner,
                    modifier = Modifier
                        .weight(2f),
                    color = if (externalTask.task.isExpired()) Color.Red else {
                        if (taskOwner != state.user.nickName) Color.Green else Color.Gray
                    },
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                )

                if (!(isMyTask && externalTask.taskOwner != state.user.nickName)) {
                    IconButton(
                        onClick = {
                            component.onClickEditTask(
                                externalTask,
                                getTaskType(externalTask, isMyTask, state.user.nickName)
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit task",
                            tint = Color.Blue,
                        )
                    }
                }

                IconButton(
                    onClick = {
                        component.onClickDeleteTask(
                            externalTask,
                            getTaskType(externalTask, isMyTask, state.user.nickName),
                            deviceToken
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = Color.Gray,
                    )
                }

            }
        }
    }
}

fun getTaskType(
    externalTask: ExternalTask,
    isMyTask: Boolean,
    currentUserNickname: String
): UserRepositoryImpl.Companion.TaskType {
    return when (isMyTask) {
        true -> {
            if (externalTask.taskOwner.trim() == currentUserNickname.trim()) {
                UserRepositoryImpl.Companion.TaskType.PRIVATE
            } else {
                UserRepositoryImpl.Companion.TaskType.FROM_OTHER_USER_FOR_ME
            }
        }

        false -> {
            UserRepositoryImpl.Companion.TaskType.MY_TO_OTHER_USER
        }
    }
}