package com.balex.familyteam.presentation.loggeduser.content

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balex.familyteam.R
import com.balex.familyteam.presentation.loggeduser.LoggedUserComponent
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore

@Composable
fun NickNameTextField(state: LoggedUserStore.State, component: LoggedUserComponent, context: Context) {
    val nickNamedColor =
        if (state.isPasswordEnabled) Color.Unspecified else Color.Red

    TextField(
        value = state.nickName,
        onValueChange = { component.onNickNameFieldChanged(it) },
        label = { Text(context.getString(R.string.nick_name)) },
        enabled = state.isCreateNewUserClicked,
        textStyle = LocalTextStyle.current.copy(
            color = nickNamedColor
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}