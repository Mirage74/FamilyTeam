package com.balex.familyteam.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val registrationOption: RegistrationOption = RegistrationOption.EMAIL,
    val emailOrPhoneNumber: String = "",
    val isEmailOrPhoneNumberVerified: Boolean = false
)
