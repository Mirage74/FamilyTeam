package com.balex.familyteam.presentation.regadmin.content

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balex.familyteam.R
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.regadmin.RegAdminComponent
import com.balex.familyteam.presentation.regadmin.RegAdminStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CodeSmsTextField(state: RegAdminStore.State, component: RegAdminComponent, context: Context) {
    val smsText =
        if (state.isRegisterButtonWasPressed) state.smsCode else ""

    val smsColor =
        if (state.isSmsOkButtonEnabled) Color.Unspecified else Color.Red
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val coroutineScope = CoroutineScope(Dispatchers.Default)
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = smsText,
                enabled = !state.isSmsVerifyButtonWasPressed,
                onValueChange = { component.onSmsNumberFieldChanged(it) },
                label = { Text(context.getString(R.string.enter_code_from_sms)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = smsColor
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        component.phoneFirebaseRepository.verifySmsCode(
                            state.smsCode,
                            "+" + state.emailOrPhone,
                            state.nickName,
                            state.displayName,
                            state.password
                        )
                    }
                },
                enabled = state.isSmsOkButtonEnabled && !state.isSmsVerifyButtonWasPressed,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(context.getString(R.string.button_ok))
            }
        }
    }
}