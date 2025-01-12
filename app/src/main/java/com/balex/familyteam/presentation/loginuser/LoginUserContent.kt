package com.balex.familyteam.presentation.loginuser

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
import com.balex.common.ANIMATION_DURATION
import com.balex.common.TopAppBarOnlyLanguage
import com.balex.common.rememberImeState
import com.balex.common.theme.DarkBlue
import com.balex.familyteam.presentation.loginuser.content.EmailTextField
import com.balex.familyteam.presentation.loginuser.content.LoginButton
import com.balex.familyteam.presentation.loginuser.content.NickNameTextField
import com.balex.familyteam.presentation.loginuser.content.PasswordTextField
import kotlinx.coroutines.Dispatchers

@Composable
fun LoginUserContent(component: LoginUserComponent) {

    val state by component.model.collectAsState(Dispatchers.Main.immediate)

    com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {

        when (state.loginUserState) {

            LoginUserStore.State.LoginUserState.Content -> {
                MainLoginUserContent(component, state)
            }

            LoginUserStore.State.LoginUserState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            }

            LoginUserStore.State.LoginUserState.Error -> {
                ErrorLoginUserScreen(state)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainLoginUserContent(
    component: LoginUserComponent,
    state: LoginUserStore.State
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
                val context = com.balex.common.LocalLocalizedContext.current

                EmailTextField(state, component, context)
                Spacer(modifier = Modifier.height(24.dp))
                NickNameTextField(state, component, context)
                Spacer(modifier = Modifier.height(24.dp))
                PasswordTextField(state, component, context)
                Spacer(modifier = Modifier.height(48.dp))
                LoginButton(state, component, context)
            }
        }
    }
}


@Composable
fun ErrorLoginUserScreen(state: LoginUserStore.State) {
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

