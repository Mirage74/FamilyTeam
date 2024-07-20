package com.balex.familyteam.presentation.regadmin

import kotlinx.coroutines.flow.StateFlow

interface RegAdminComponent {

    val model: StateFlow<RegAdminStore.State>

    fun onClickBack()

    fun onClickRegister()

    fun onClickSmsVerify()

    fun onClickSendSmsAgain()

    fun onClickEmailOrPhoneButton()

    fun onClickChangePasswordVisibility()

    fun onClickTryAgain()

    fun onLoginFieldChanged(currentLoginText: String)

    fun onNickNameFieldChanged(currentNickNameText: String)

    fun onDisplayNameFieldChanged(currentDisplayNameText: String)

    fun onPasswordFieldChanged(currentPasswordText: String)

    fun onSmsNumberFieldChanged(currentSmsText: String)

    fun onLanguageChanged(language: String)

}