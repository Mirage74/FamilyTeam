package com.balex.logged_user.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.balex.common.LocalLocalizedContext
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R

@Composable
fun MyTasksForOtherUsersContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    val context = LocalLocalizedContext.current
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        items(state.myTasksForOtherUsersList.externalTasks) { myTask ->
            val description = myTask.task.description
            val taskOwner = myTask.taskOwner
            Text(text = context.getString(R.string.text_my_tasks_for_other, description, taskOwner))
        }
    }
}