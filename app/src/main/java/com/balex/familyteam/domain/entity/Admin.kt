package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Admin(
    val registrationOption: RegistrationOption = RegistrationOption.EMAIL,
    val emailOrPhoneNumber: String = "",
    val isEmailOrPhoneNumberVerified: Boolean = false
): Parcelable
