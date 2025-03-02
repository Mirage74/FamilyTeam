package com.balex.common

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

val LocalLocalizedContext = compositionLocalOf<Context> { error("CompositionLocal LocalLocalizedContext not provided") }

fun setLocale(context: Context, languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config)
}

@Composable
fun LocalizedContextProvider(languageCode: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val localizedContext = remember(languageCode) { setLocale(context, languageCode) }
    CompositionLocalProvider(LocalLocalizedContext provides localizedContext) {
        content()
    }
}