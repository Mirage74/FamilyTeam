package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.common.data.repository.TaskMode
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

        if (!state.isAddTaskClicked && !state.isEditTaskClicked) {
            GreetingRow(state)
            Button(
                onClick = { component.onClickAddNewTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 64.dp)
                    .weight(1f)
            ) {
                Text(
                    text = context.getString(R.string.add_button_text),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val tasks = state.user.listToDo.allMyTasks(state.user.nickName).externalTasks
            ShowTasksList(tasks, state, component, true,  modifier = Modifier.weight(4f).padding(paddingValues))
        } else {
            if (state.isAddTaskClicked) {
                InputOrEditTaskForm(component, state, true, TaskMode.ADD, context)
            } else {
                InputOrEditTaskForm(component, state, true, TaskMode.EDIT, context)
            }
        }
    }
}






