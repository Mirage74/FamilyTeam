package com.balex.familyteam.presentation.loggeduser.content

import android.content.Context
import androidx.compose.foundation.layout.Spacer
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
import com.balex.familyteam.presentation.loggeduser.LoggedUserComponent
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore

@Composable
fun RegisterNewUserButton(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    context: Context
) {

    Button(
        enabled = state.isPasswordEnabled && state.isRegisterInFirebaseButtonEnabled && state.isCreateNewUserClicked,
        onClick = {
            component.onAdminPageRegisterNewUserInFirebaseClicked()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
    ) {
        Text(
            text = context.getString(R.string.reg_button),
            fontSize = dimensionResource(id = R.dimen.reg_admin_button_text_size).value.sp
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        enabled = state.isPasswordEnabled && state.isRegisterInFirebaseButtonEnabled && state.isCreateNewUserClicked,
        onClick = {
            component.onAdminPageCancelCreateNewUserClicked()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
    ) {
        Text(
            text = context.getString(R.string.cancel_button),
            fontSize = dimensionResource(id = R.dimen.reg_admin_button_text_size).value.sp
        )
    }

}