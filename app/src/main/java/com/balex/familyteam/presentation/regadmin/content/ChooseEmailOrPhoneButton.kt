package com.balex.familyteam.presentation.regadmin.content

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.R
import com.balex.common.R as commonR
import com.balex.common.entity.RegistrationOption
import com.balex.familyteam.presentation.regadmin.RegAdminComponent
import com.balex.familyteam.presentation.regadmin.RegAdminStore

@Composable
fun ChooseEmailOrPhoneButton(state: RegAdminStore.State, component: RegAdminComponent,  context: Context) {
    Row {
        Button(
            enabled = (state.selectedOption != RegistrationOption.EMAIL) && (!state.isRegisterButtonWasPressed),
            onClick = {
                component.onClickEmailOrPhoneButton()
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .height(dimensionResource(id = commonR.dimen.reg_buttons_height).value.dp),
        ) {
            Text(
                context.getString(R.string.by_email),
                fontSize = dimensionResource(id = commonR.dimen.reg_admin_button_text_size).value.sp
            )
        }
        Button(
            enabled = (state.selectedOption != RegistrationOption.PHONE) && (!state.isRegisterButtonWasPressed),
            onClick = {
                component.onClickEmailOrPhoneButton()
            },
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .height(dimensionResource(id = commonR.dimen.reg_buttons_height).value.dp),
        ) {
            Text(
                context.getString(R.string.by_phone),
                fontSize = dimensionResource(id = commonR.dimen.reg_admin_button_text_size).value.sp
            )
        }
    }
}