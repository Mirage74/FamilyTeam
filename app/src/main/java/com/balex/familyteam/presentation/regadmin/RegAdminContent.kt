package com.balex.familyteam.presentation.regadmin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

@Composable
fun RegAdminContent(component: RegAdminComponent) {
    val state by component.model.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Switch between email and phone registration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
        ) {
        Text(
            text = "Register by Email",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { }
                .padding(8.dp)
        )
        Text(
            text = "Register by Phone",
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { }
                .padding(8.dp)
        )
    }

        // Input field for email or phone
        TextField(
            value = state.emailOrPhone,
            onValueChange = {
                //state.emailOrPhone = it
                            },
            label = { Text(if (true) "Email" else "Phone") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (true) KeyboardType.Email else KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input field for password
        TextField(
            value = state.password,
            onValueChange = {
                //password = it
                            },
            label = { Text("Password") },
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (state.passwordVisible)
                    Icons.Default.Visibility
                else Icons.Default.VisibilityOff

                IconButton(onClick = {
                //    passwordVisible = !passwordVisible
                }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register button
        Button(
            onClick = {
                // Handle registration logic
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Register")
        }
    }
}

