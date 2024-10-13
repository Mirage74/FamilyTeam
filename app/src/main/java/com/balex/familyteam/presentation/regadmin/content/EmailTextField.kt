package com.balex.familyteam.presentation.regadmin.content

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balex.familyteam.R
import com.balex.common.domain.entity.RegistrationOption
import com.balex.familyteam.presentation.regadmin.RegAdminComponent
import com.balex.familyteam.presentation.regadmin.RegAdminStore

@Composable
fun EmailTextField(state: RegAdminStore.State, component: RegAdminComponent, context: Context) {
    val emailText =
        if (state.selectedOption == RegistrationOption.EMAIL) state.emailOrPhone else ""
    val emailColor = if (state.isNickNameEnabled) Color.Unspecified else Color.Red

    var hasFocus by remember { mutableStateOf(false) }

    TextField(
        value = emailText,
        onValueChange = { component.onLoginFieldChanged(it) },
        label = { Text(context.getString(R.string.e_mail)) },
        enabled = (state.selectedOption == RegistrationOption.EMAIL) && (!state.isRegisterButtonWasPressed),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textStyle = LocalTextStyle.current.copy(
            color = emailColor
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}