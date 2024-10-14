package com.balex.familyteam.presentation.loginuser.content

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
import com.balex.common.R
import com.balex.familyteam.presentation.loginuser.LoginUserComponent
import com.balex.familyteam.presentation.loginuser.LoginUserStore

@Composable
fun EmailTextField(state: LoginUserStore.State, component: LoginUserComponent, context: Context) {
    val emailText = state.adminEmailOrPhone
    val emailColor = if (state.isNickNameEnabled) Color.Unspecified else Color.Red


    TextField(
        value = emailText,
        onValueChange = { component.onLoginFieldChanged(it) },
        label = { Text(context.getString(R.string.email_or_phone)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textStyle = LocalTextStyle.current.copy(
            color = emailColor
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}