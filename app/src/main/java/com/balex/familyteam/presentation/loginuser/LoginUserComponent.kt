package com.balex.familyteam.presentation.loginuser

import kotlinx.coroutines.flow.StateFlow

interface LoginUserComponent {

    val model: StateFlow<LoginUserStore.State>

    fun onClickLogin()

    fun onLoginFieldChanged(currentLoginText: String)

    fun onNickNameFieldChanged(currentNickNameText: String)

    fun onPasswordFieldChanged(currentPasswordText: String)

    fun onClickChangePasswordVisibility()

    fun onClickAbout()

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)
}