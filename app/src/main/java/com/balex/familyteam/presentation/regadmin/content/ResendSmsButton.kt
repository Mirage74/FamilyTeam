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
import com.balex.common.R as commonR
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.regadmin.RegAdminStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ResendSmsButton(
    state: RegAdminStore.State, phoneFirebaseRepository: PhoneFirebaseRepository,
    context: Context, activity: MainActivity
) {
    Button(
        onClick = {
            //component.onClickSendSmsAgain()
            CoroutineScope(Dispatchers.Default).launch {
                phoneFirebaseRepository.resendVerificationCode(
                    "+" + state.emailOrPhone,
                    state.nickName,
                    state.displayName,
                    state.password,
                    activity
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = commonR.dimen.reg_buttons_height).value.dp)
    ) {
        Text(
            text = context.getString(R.string.button_resend_sms),
            fontSize = dimensionResource(id = commonR.dimen.reg_admin_button_text_size).value.sp
        )
    }

}