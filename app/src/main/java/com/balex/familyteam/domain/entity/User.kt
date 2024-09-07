package com.balex.familyteam.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerializedName("nickName")
    val nickName: String = DEFAULT_NICK_NAME,
    @SerializedName("isAdmin")
    val isAdmin: Boolean = false,
    @SerializedName("fakeEmail")
    val fakeEmail: String = DEFAULT_FAKE_EMAIL,
    @SerializedName("adminEmailOrPhone")
    val adminEmailOrPhone: String = "",
    @SerializedName("displayName")
    val displayName: String = "",
    @SerializedName("password")
    val password: String = "",
    @SerializedName("language")
    val language: String = Language.DEFAULT_LANGUAGE.symbol,
    @SerializedName("listToDo")
    val listToDo: ToDoList = ToDoList(),
) {
    companion object {
        const val ERROR_LOADING_USER_DATA_FROM_FIREBASE = "ERROR_LOADING_USER_DATA_FROM_FIREBASE"
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
        const val DEFAULT_FAKE_EMAIL = "DEFAULT_FAKE_EMAIL"
    }
}


