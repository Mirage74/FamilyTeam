package com.balex.familyteam.presentation.regadmin

import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import kotlinx.coroutines.flow.StateFlow

interface RegAdminComponent {

    val phoneFirebaseRepository: PhoneFirebaseRepository

    val model: StateFlow<RegAdminStore.State>

    fun onClickRegister()

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