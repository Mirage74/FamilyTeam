package com.balex.familyteam.presentation.about

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.balex.common.R
import kotlinx.coroutines.Dispatchers

@Composable
fun AboutContent(component: AboutComponent) {
    val state by component.model.collectAsState(Dispatchers.Main.immediate)

    com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {
        AboutScreen()
    }
}

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val aboutText = context.getString(R.string.about_text)
    Text(text = aboutText)
}