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
import com.balex.common.data.local.model.ShopItemDBModel
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.logged_user.content.subcontent.GreetingRow
import com.balex.logged_user.content.subcontent.ShowShopList
import com.balex.logged_user.content.subcontent.recourses.ShowAvailableResources

@Composable
fun ShopListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State
) {
    val context = LocalLocalizedContext.current

    val arrangement = if (!state.isAddShopItemClicked) {
        Arrangement.SpaceEvenly
    } else {
        Arrangement.Top
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = arrangement
    ) {
        if (!state.isAddShopItemClicked) {
            GreetingRow(state.user.nickName, state.user.displayName)
            Spacer(modifier = Modifier.height(16.dp))
            if (state.isExchangeCoinsClicked) {
                //ShowAvailableResources(state, component::onBuyCoinsClicked, component::onConfirmExchangeClicked)
            }
            ShowAvailableResources(state, component::onExchangeCoinsClicked)
            Button(
                onClick = { component.onClickAddShopItem() },
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

            ShowShopList(state, component, modifier = Modifier.weight(4f))
        } else {
            var description by remember { mutableStateOf(TextFieldValue("")) }
            Spacer(modifier = Modifier.height(16.dp))
            GreetingRow(state.user.nickName, state.user.displayName)
            Spacer(modifier = Modifier.height(16.dp))
            Text(context.getString(R.string.shop_item_description))
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    component.onClickedAddShopItemToDatabase(ShopItemDBModel(description = description.text))
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 16.dp)
            ) {
                Text(context.getString(R.string.add_item_button_text))
            }

        }
    }
}

