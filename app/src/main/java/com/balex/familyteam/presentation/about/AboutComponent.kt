package com.balex.familyteam.presentation.about

import kotlinx.coroutines.flow.StateFlow

interface AboutComponent {

    val model: StateFlow<AboutStore.State>

    fun onLanguageChanged(language: String)

    fun onClickRules()

    suspend fun onClickDeleteAccount(userName: String)

    suspend fun onClickDeleteTeam()
}