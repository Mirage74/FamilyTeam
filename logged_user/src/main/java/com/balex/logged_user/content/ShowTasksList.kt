package com.balex.logged_user.content

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.extensions.isExpired
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun ShowTasksList(
    tasks: List<ExternalTask>,
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    isMyTask: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {

        items(tasks) { externalTask ->
            val description = externalTask.task.description
            val taskOwner = externalTask.taskOwner
            val taskDate = convertMillisToDate(externalTask.task.cutoffTime)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
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
                    text = taskDate,
                    modifier = Modifier
                        .weight(3f),
                    fontSize = 12.sp
                )

                Text(
                    text = taskOwner,
                    modifier = Modifier
                        .weight(2f),
                    color = if (externalTask.task.isExpired()) Color.Red else {
                        if (taskOwner != state.user.nickName) Color.Green else Color.Gray},
                    fontSize = 16.sp,
                    textAlign = TextAlign.End
                )

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = Color.Blue,
                    )
                }

                IconButton(
                    onClick = {
                        component.onClickDeleteTask(
                            externalTask,
                            getTaskType(externalTask, isMyTask, state.user.nickName)
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