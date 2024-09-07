package com.balex.familyteam.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val nickName: String = DEFAULT_NICK_NAME,
    val registrationOption: RegistrationOption = RegistrationOption.EMAIL,
    val emailOrPhoneNumber: String = "",
    val isEmailOrPhoneNumberVerified: Boolean = false
) {
    companion object {
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
    }
}

