package com.balex.logged_user.content.subcontent

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balex.common.R
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun DisplayNameTextField(state: LoggedUserStore.State, component: LoggedUserComponent, context: Context) {
    TextField(
        value = state. displayName,
        onValueChange = { component.onDisplayNameFieldChanged(it) },
        label = { Text(context.getString(R.string.display_name)) },
        enabled = state.isCreateNewUserClicked,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}