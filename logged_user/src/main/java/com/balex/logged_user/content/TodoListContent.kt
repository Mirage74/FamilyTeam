package com.balex.logged_user.content

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.common.extensions.allMyTasks
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.logged_user.content.subcontent.GreetingRow
import com.balex.logged_user.content.subcontent.ShowTasksList
import com.balex.logged_user.content.subcontent.inputtask.convertMillisToDate
import com.balex.logged_user.content.subcontent.recourses.ShowAvailableResources

@Composable
fun TodoListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    deviceToken: String,
    activity: Activity,
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
            GreetingRow(state.user.nickName, state.user.displayName, state.user.hasPremiumAccount)
            if (state.user.hasPremiumAccount) {
                val expiresDate = convertMillisToDate(state.user.premiumAccountExpirationDate)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Premium till: $expiresDate"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShowAvailableResources(state) { component.onExchangeCoinsClicked() }

            Button(
                onClick = {component.onBuyCoinsClicked(activity)},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 64.dp)
                    .weight(1f)
            ) {
                Text("Buy Coins")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { component.onClickAddNewTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 64.dp)
                    .weight(1f)
            ) {
                Text(
                    text = context.getString(R.string.add_button_text),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val tasks = state.user.listToDo.allMyTasks(state.user.nickName).externalTasks
            ShowTasksList(
                tasks,
                state,
                component,
                true,
                deviceToken,
                modifier = Modifier
                    .weight(4f)
                    .padding(paddingValues)
            )
        }
    }
}






