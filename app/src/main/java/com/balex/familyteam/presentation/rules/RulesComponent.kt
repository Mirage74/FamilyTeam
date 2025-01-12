package com.balex.familyteam.presentation.rules

import kotlinx.coroutines.flow.StateFlow

interface RulesComponent {

    val model: StateFlow<RulesStore.State>

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)

    fun onClickAbout()

}