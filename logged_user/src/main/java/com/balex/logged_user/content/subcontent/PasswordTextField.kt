package com.balex.logged_user.content.subcontent

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.balex.common.R
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun PasswordTextField(state: LoggedUserStore.State, component: LoggedUserComponent, context: Context) {
    val passwordColor =
        if (state.isRegisterInFirebaseButtonEnabled) Color.Unspecified else Color.Red

    TextField(
        value = state.password,
        onValueChange = { component.onPasswordFieldChanged(it) },
        label = { Text(context.getString(R.string.password)) },
        enabled = state.isPasswordEnabled && state.isCreateNewUserClicked,
        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (state.passwordVisible)
                Icons.Default.Visibility
            else Icons.Default.VisibilityOff

            IconButton(onClick = {
                component.onClickChangePasswordVisibility()
            }) {
                Icon(imageVector = image, contentDescription = null)
            }
        },
        textStyle = LocalTextStyle.current.copy(
            color = passwordColor
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}