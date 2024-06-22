package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Admin(
    val isEmailRegistration: Boolean = true,
    val emailOrPhoneNumber: String = "",
    val isEmailOrPhoneNumberConfirmed: Boolean = false
): Parcelable
