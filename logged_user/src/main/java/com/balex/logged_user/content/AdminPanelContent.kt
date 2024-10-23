package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
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
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R

@Composable
fun AdminPanelContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        val context = LocalLocalizedContext.current

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = context.getString(R.string.title_admin_panel),
        )
        if (!state.isEditUsersListClicked) {
            Button(
                onClick = { component.onAdminPageCreateNewUserClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Text(
                    text = context.getString(R.string.button_text_create_new_user)
                )
            }
        }

        if (!state.isCreateNewUserClicked) {
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Text(
                    text = context.getString(R.string.button_text_edit_users_list)
                )
            }
        }


        if (state.isCreateNewUserClicked) {
            NickNameTextField(state, component, context)
            Text(
                text = context.getString(com.balex.common.R.string.optional)
            )
            DisplayNameTextField(state, component, context)
            PasswordTextField(state, component, context)
            Spacer(modifier = Modifier.height(24.dp))
            RegisterNewUserButton(state, component, context)
        }
    }
}