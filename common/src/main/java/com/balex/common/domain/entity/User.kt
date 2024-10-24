package com.balex.common.domain.entity

import com.google.firebase.database.Exclude
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerializedName("nickName")
    val nickName: String = DEFAULT_NICK_NAME,
    @SerializedName("hasAdminRights")
    val hasAdminRights: Boolean = false,
    @SerializedName("fakeEmail")
    val fakeEmail: String = DEFAULT_FAKE_EMAIL,
    @SerializedName("adminEmailOrPhone")
    val adminEmailOrPhone: String = "",
    @SerializedName("displayName")
    val displayName: String = "",
    @SerializedName("password")
    val password: String = "",
    @SerializedName("hasPremiumAccount")
    val hasPremiumAccount: Boolean = false,
    @SerializedName("premiumAccountExpirationDate")
    val premiumAccountExpirationDate: Long = 0,
    @SerializedName("language")
    val language: String = Language.DEFAULT_LANGUAGE.symbol,
    @SerializedName("availableFCM")
    val availableFCM: Int = 0,
    @SerializedName("lastTimeAvailableFCMWasUpdated")
    val lastTimeAvailableFCMWasUpdated: Long = System.currentTimeMillis(),
    @SerializedName("availableTasksToAdd")
    val availableTasksToAdd: Int = 0,
    @SerializedName("listToDo")
    val listToDo: ToDoList = ToDoList(),
    @Exclude
    val existErrorInData: Boolean = false,
    @Exclude
    val errorMessage: String = NO_ERROR_MESSAGE
) {


    companion object {
        const val NO_ERROR_MESSAGE = "NO_ERROR_MESSAGE"
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
        const val DEFAULT_FAKE_EMAIL = "DEFAULT_FAKE_EMAIL"
        const val WRONG_PASSWORD = "WRONG_PASSWORD"
    }
}


