package com.balex.familyteam.presentation.regadmin

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.LocalLocalizedContext
import com.balex.familyteam.LocalizedContextProvider
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.presentation.ANIMATION_DURATION
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.TopAppBarOnlyLanguage
import com.balex.familyteam.presentation.regadmin.content.ChooseEmailOrPhoneButton
import com.balex.familyteam.presentation.regadmin.content.CodeSmsTextField
import com.balex.familyteam.presentation.regadmin.content.DisplayNameTextField
import com.balex.familyteam.presentation.regadmin.content.EmailTextField
import com.balex.familyteam.presentation.regadmin.content.NickNameTextField
import com.balex.familyteam.presentation.regadmin.content.PasswordTextField
import com.balex.familyteam.presentation.regadmin.content.PhoneTextField
import com.balex.familyteam.presentation.regadmin.content.RegisterOrTryAgainButton
import com.balex.familyteam.presentation.regadmin.content.ResendSmsButton
import com.balex.familyteam.presentation.regadmin.content.VerifyEmailOrPhoneText
import com.balex.familyteam.presentation.rememberImeState
import com.balex.familyteam.presentation.ui.theme.DarkBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun RegAdminContent(component: RegAdminComponent, activity: MainActivity) {

    val state by component.model.collectAsState(context = CoroutineScope(Dispatchers.Main.immediate).coroutineContext)

    LocalizedContextProvider(languageCode = state.language.lowercase()) {
        when (state.regAdminState) {
            RegAdminStore.State.RegAdminState.Content -> {
                ContentScreen(component, state, activity)
            }

            RegAdminStore.State.RegAdminState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            }

            RegAdminStore.State.RegAdminState.Error -> {
                ErrorRegAdminScreen(state)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContentScreen(
    component: RegAdminComponent,
    state: RegAdminStore.State,
    activity: MainActivity
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)

    ) {
        val imeState = rememberImeState()
        val scrollState = rememberScrollState()


        LaunchedEffect(key1 = imeState.value) {
            if (imeState.value) {
                scrollState.animateScrollTo(scrollState.maxValue, tween(ANIMATION_DURATION))
            }
        }

        Scaffold(
            topBar = {
                val onLanguageChanged: (String) -> Unit = { newLanguage ->
                    component.onLanguageChanged(newLanguage)
                }
                TopAppBarOnlyLanguage(state.language, onLanguageChanged)
            },
            containerColor = Color.Cyan
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val context = LocalLocalizedContext.current

                ChooseEmailOrPhoneButton(state, component, context)
                Spacer(modifier = Modifier.height(24.dp))
                EmailTextField(state, component, context)
                PhoneTextField(state, component, context)
                NickNameTextField(state, component, context)
                Text(
                    text = context.getString(R.string.optional)
                )
                DisplayNameTextField(state, component, context)
                PasswordTextField(state, component, context)

                if (state.isRegisterButtonWasPressed && state.selectedOption == RegistrationOption.PHONE) {
                    CodeSmsTextField(state, component, context)
                }

                VerifyEmailOrPhoneText(state, component, context)

                if (state.isRegisterButtonWasPressed && state.selectedOption == RegistrationOption.PHONE) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ResendSmsButton(state, component.phoneFirebaseRepository, context, activity)
                }
                Spacer(modifier = Modifier.height(24.dp))
                RegisterOrTryAgainButton(state, component, context, activity)
            }
        }
    }
}



@Composable
fun ErrorRegAdminScreen(state: RegAdminStore.State) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.errorMessage,
            color = Color.Red,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}