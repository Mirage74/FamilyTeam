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
import com.balex.familyteam.presentation.regadmin.RegAdminComponent

@Composable
fun ResendSmsButton(component: RegAdminComponent, context: Context) {
    Button(
        onClick = {
            component.onClickSendSmsAgain()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
    ) {
        Text(
            text = context.getString(R.string.button_resend_sms),
            fontSize = dimensionResource(id = R.dimen.reg_admin_button_text_size).value.sp
        )
    }

}