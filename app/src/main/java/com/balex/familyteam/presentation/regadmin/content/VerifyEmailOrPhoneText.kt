package com.balex.familyteam.presentation.regadmin.content

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun VerifyEmailOrPhoneText(state: RegAdminStore.State, component: RegAdminComponent, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isRegisterButtonWasPressed) {
                Spacer(modifier = Modifier.height(24.dp))
                if (state.selectedOption == RegistrationOption.EMAIL) {
                    Text(
                        text = context.getString(R.string.please_verify_email),
                        fontSize = dimensionResource(id = commonR.dimen.verify_text_size).value.sp
                    )
                } else {
                    Text(
                        text = context.getString(R.string.please_verify_phone),
                        fontSize = dimensionResource(id = commonR.dimen.verify_text_size).value.sp
                    )
                }
            }
        }
    }
}