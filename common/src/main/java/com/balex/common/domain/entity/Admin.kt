package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val nickName: String = DEFAULT_NICK_NAME,
    val registrationOption: RegistrationOption = RegistrationOption.EMAIL,
    val emailOrPhoneNumber: String = "",
    val isEmailOrPhoneNumberVerified: Boolean = false,
    val usersNickNamesList: List<String> = emptyList()
) {
    companion object {
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
    }
}

