package com.balex.familyteam.presentation.about

import kotlinx.coroutines.flow.StateFlow

interface AboutComponent {

    val model: StateFlow<AboutStore.State>

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)
}