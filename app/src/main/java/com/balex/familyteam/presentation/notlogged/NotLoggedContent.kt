package com.balex.familyteam.presentation.notlogged

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.balex.familyteam.presentation.ui.theme.FamilyTeamTheme


@Composable
fun NotLoggedContent() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        ThreeButtonsScreen()
    }
}

@Composable
fun ThreeButtonsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Text(text = "Button 1")
        }
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Text(text = "Button 2")
        }
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Text(text = "Button 3")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThreeButtonsScreenPreview() {
    FamilyTeamTheme {
        ThreeButtonsScreen()
    }
}