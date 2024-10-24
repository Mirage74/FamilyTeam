package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.balex.common.LocalLocalizedContext
import com.balex.common.extensions.allMyTasks
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R

@Composable
fun TodoListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {

    val context = LocalLocalizedContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        GreetingRow(component, state)

        if (!state.isAddTodoItemClicked) {

            Button(
                onClick = { component.onClickAddNewTaskForMe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 64.dp)
            ) {
                Text(
                    text = context.getString(R.string.add_button_text),
                )
            }

            ShowTasksList(state, component, paddingValues)
        } else {
            InputMyNewTaskForm(state, component, context)
        }


    }
}

@Composable
fun GreetingRow(
    component: LoggedUserComponent,
    state: LoggedUserStore.State
) {
    val context = LocalLocalizedContext.current
    val displayName = state.user.displayName.ifEmpty { state.user.nickName }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.hello_text, displayName),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(24.dp))

        Icon(
            imageVector = Icons.Default.AddTask,
            contentDescription = "Tasks available"
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableTasksToAdd.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "FCM available"
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableFCM.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ShowTasksList(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        val tasks = state.user.listToDo.allMyTasks(state.user.nickName).externalTasks
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
                    color = if (taskOwner != state.user.nickName) Color.Green else Color.Gray,
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
                    onClick = { component.onClickDeleteTask(externalTask) },
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