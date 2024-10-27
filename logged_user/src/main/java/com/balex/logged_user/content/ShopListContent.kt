package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.logged_user.content.subcontent.GreetingRow
import com.balex.logged_user.content.subcontent.ShowShopList

@Composable
fun ShopListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State
) {
    val context = LocalLocalizedContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!state.isAddShopListClicked) {
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
                    text = context.getString(R.string.add_item_button_text),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val tasks = state.user.listToDo.thingsToDoForOtherUsers.externalTasks
            ShowShopList(state, component, modifier = Modifier.weight(4f))
        } else {
            var description by remember { mutableStateOf(TextFieldValue("")) }
            GreetingRow(state)
            Text(context.getString(R.string.shop_item_description))
            OutlinedTextField(
                value = description,
                onValueChange = {
                    if (it.text.length <= context.resources.getInteger(R.integer.max_task_description_length)) {
                        description = it
                    }
                },
                label = { Text(context.getString(R.string.shop_item_description)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                        //component.onClickAddShopItemToFirebase(ShopItem(description = description.text))
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(context.getString(R.string.add_item_button_text))
            }

        }
    }
}

