package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        if (!state.isCreateNewUserClicked) {
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
            ShowUsersList(state, paddingValues)
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

@Composable
fun ShowUsersList(
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        items(state.usersNicknamesList) { userNickname ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()

                        drawRect(
                            color = Color.Black,
                            style = Stroke(width = strokeWidth)
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userNickname,
                    fontSize = 20.sp
                )

            }
        }
    }
}

