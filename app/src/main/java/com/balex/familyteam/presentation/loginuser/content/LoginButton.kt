package com.balex.familyteam.presentation.loginuser.content

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.R
import com.balex.familyteam.presentation.loginuser.LoginUserComponent
import com.balex.familyteam.presentation.loginuser.LoginUserStore

@Composable
fun LoginButton(
    state: LoginUserStore.State,
    component: LoginUserComponent,
    context: Context
) {
        Button(
            enabled = state.isNickNameEnabled && state.isPasswordEnabled && state.isLoginButtonEnabled,
            onClick = {
                component.onClickLogin()

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
        ) {
            Text(
                text = context.getString(R.string.login_button),
                fontSize = dimensionResource(id = R.dimen.reg_admin_button_text_size).value.sp
            )
        }

}