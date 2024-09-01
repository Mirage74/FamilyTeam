package com.balex.familyteam.presentation.loginuser

import kotlinx.coroutines.flow.StateFlow

interface LoginUserComponent {

    val model: StateFlow<LoginUserStore.State>

    fun onClickAbout()

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)
}