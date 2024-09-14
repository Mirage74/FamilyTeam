package com.balex.familyteam.presentation.loginuser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.balex.familyteam.presentation.ui.theme.DarkBlue

@Composable
fun LoginUserContent(component: LoginUserComponent) {
    val state by component.model.collectAsState()

    when (state.loginUserState) {

        LoginUserStore.State.LoginUserState.Content -> {
            //MainLoginUserContent(component)
            ErrorLoginUserScreen()
        }

        LoginUserStore.State.LoginUserState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        }

        LoginUserStore.State.LoginUserState.Error -> {
            ErrorLoginUserScreen()
        }
    }
}

@Composable
fun ErrorLoginUserScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error login user",
            color = Color.Red,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}