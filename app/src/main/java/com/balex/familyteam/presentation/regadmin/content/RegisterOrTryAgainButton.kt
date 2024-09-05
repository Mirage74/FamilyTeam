package com.balex.familyteam.presentation.regadmin.content

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
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.regadmin.RegAdminComponent
import com.balex.familyteam.presentation.regadmin.RegAdminStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterOrTryAgainButton(
    state: RegAdminStore.State,
    component: RegAdminComponent,
    context: Context,
    activity: MainActivity
) {
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    if (!state.isRegisterButtonWasPressed) {
        Button(
            enabled = state.isPasswordEnabled && state.isRegisterButtonEnabled,
            onClick = {
                if (state.selectedOption == RegistrationOption.EMAIL) {
                    component.onClickRegister()
                } else {
                    coroutineScope.launch {
                        component.phoneFirebaseRepository.sendSmsVerifyCode(
                            "+" + state.emailOrPhone,
                            state.nickName,
                            state.displayName,
                            state.password,
                            activity
                        )
                    }
                    component.onClickRegister()
                }

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
    } else {
        Button(
            onClick = {
                component.onClickTryAgain()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
        ) {
            Text(
                text = context.getString(R.string.try_again_button),
                fontSize = dimensionResource(id = R.dimen.reg_admin_button_text_size).value.sp
            )
        }
    }
}