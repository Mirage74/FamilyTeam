package com.balex.familyteam.presentation.notlogged

import kotlinx.coroutines.flow.StateFlow

interface NotLoggedComponent {

    val model: StateFlow<NotLoggedStore.State>

    fun onClickRegAdmin()

    fun onClickLoginUser()

    fun onClickRules()

    fun onClickAbout()

    @Suppress("unused")
    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)
}