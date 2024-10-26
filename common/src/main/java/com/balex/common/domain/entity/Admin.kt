package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    @SerializedName("nickName")
    val nickName: String = DEFAULT_NICK_NAME,
    @SerializedName("registrationOption")
    val registrationOption: RegistrationOption = RegistrationOption.EMAIL,
    @SerializedName("emailOrPhoneNumber")
    val emailOrPhoneNumber: String = "",
    @SerializedName("emailOrPhoneNumberVerified")
    val emailOrPhoneNumberVerified: Boolean = false,
    @SerializedName("usersNickNamesList")
    val usersNickNamesList: List<String> = emptyList()
) {
    companion object {
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
    }
}

