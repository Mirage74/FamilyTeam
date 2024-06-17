package com.balex.familyteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    var currentLanguage by remember { mutableStateOf("ru") }

    LocalizedContextProvider(languageCode = currentLanguage) {
        MyScreen(onLanguageChange = { newLanguage ->
            currentLanguage = newLanguage
        })
    }
}

@Composable
fun MyScreen(onLanguageChange: (String) -> Unit) {
    val context = LocalLocalizedContext.current
    val exampleString = context.getString(R.string.app_name)

    Column {
        Text(text = exampleString)
        Button(onClick = { onLanguageChange("de") }) {
            Text(text = "Change to DE")
        }
        Button(onClick = { onLanguageChange("en") }) {
            Text(text = "Change to English")
        }
    }
}