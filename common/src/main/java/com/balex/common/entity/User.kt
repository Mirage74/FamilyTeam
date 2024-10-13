package com.balex.common.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerializedName("nickName")
    val nickName: String = DEFAULT_NICK_NAME,
    @SerializedName("admin")
    val admin: Boolean = false,
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
    val isError: Boolean = false,
    val errorMessage: String = NO_ERROR_MESSAGE
) {
    companion object {
        const val NO_ERROR_MESSAGE = "NO_ERROR_MESSAGE"
        const val DEFAULT_NICK_NAME = "DEFAULT_NICK_NAME"
        const val DEFAULT_FAKE_EMAIL = "DEFAULT_FAKE_EMAIL"
        const val WRONG_PASSWORD = "WRONG_PASSWORD"
    }
}


