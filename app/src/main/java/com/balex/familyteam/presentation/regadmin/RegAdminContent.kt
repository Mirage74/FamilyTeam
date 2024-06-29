package com.balex.familyteam.presentation.regadmin

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.RegistrationOption


@Composable
fun RegAdminContent(component: RegAdminComponent) {

    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value){
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)

    )
    {
        val state by component.model.collectAsState()



        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center


        ) {

            Row {
                Button(
                    enabled = (state.selectedOption != RegistrationOption.EMAIL) && (!state.isRegisterButtonWasPressed),
                    onClick = {
                        component.onClickEmailOrPhoneButton()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                        .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp),
                ) {
                    Text("by Email")
                }
                Button(
                    enabled = (state.selectedOption != RegistrationOption.PHONE) && (!state.isRegisterButtonWasPressed),
                    onClick = {
                        component.onClickEmailOrPhoneButton()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp),
                ) {
                    Text("By Phone")
                }
            }

            Spacer(modifier = Modifier.height(96.dp))

            val emailText = if (state.selectedOption == RegistrationOption.EMAIL) state.emailOrPhone else ""
            val phoneText = if (state.selectedOption == RegistrationOption.PHONE) state.emailOrPhone else ""



            TextField(
                value = emailText,
                onValueChange = { component.onLoginFieldChanged(it) },
                label = { Text("Email") },
                enabled = (state.selectedOption == RegistrationOption.EMAIL) && (!state.isRegisterButtonWasPressed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            TextField(
                value = phoneText,
                onValueChange = {component.onLoginFieldChanged(it)},
                label = { Text("Phone") },
                enabled = (state.selectedOption == RegistrationOption.PHONE) && (!state.isRegisterButtonWasPressed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            TextField(
                value = state.password,
                onValueChange = {component.onPasswordFieldChanged(it)},
                label = { Text("Password") },
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (state.passwordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    IconButton(onClick = {
                        component.onClickChangePasswordVisibility()
                    }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )


            if (state.isRegError) {
                Text("Registration failed")
            } else {
                if (state.isRegisterButtonWasPressed) {
                    if (state.selectedOption == RegistrationOption.EMAIL) {
                        Text("Please verify your email")
                    } else {
                        Text("Please verify your phone")
                    }

                }
            }

            Spacer(modifier = Modifier.height(96.dp))

            Button(
                enabled = !state.isRegisterButtonWasPressed,
                onClick = {
                    // Handle registration logic
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.reg_buttons_height).value.dp)
            ) {
                if (!state.isRegError) {
                    Text(text = "Register")
                } else {
                    Text(text = "Try again")
                }

            }
            //Spacer(modifier = Modifier.weight(1f))

        }
    }

}

