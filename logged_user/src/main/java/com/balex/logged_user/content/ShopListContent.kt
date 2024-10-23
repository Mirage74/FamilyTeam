package com.balex.logged_user.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun ShopListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        items(state.todoList.listToShop) { item ->
            Text(text = item)
        }
    }
}