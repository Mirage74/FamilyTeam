package com.balex.familyteam.presentation.notlogged

import kotlinx.coroutines.flow.StateFlow

interface NotLoggedComponent {

    val model: StateFlow<NotLoggedStore.State>

    fun onClickRegAdmin()

    fun onClickLoginAdmin()

    fun onClickLoginUser()

    fun onLanguageChanged(language: String)

    fun onClickAbout()

    fun onRefreshLanguage()
}