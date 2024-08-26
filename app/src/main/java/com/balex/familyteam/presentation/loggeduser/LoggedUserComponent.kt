package com.balex.familyteam.presentation.loggeduser

import kotlinx.coroutines.flow.StateFlow

interface LoggedUserComponent {

    val model: StateFlow<LoggedUserStore.State>

    fun onNavigateToBottomItem(page: PagesNames)

    fun onLanguageChanged(language: String)

}