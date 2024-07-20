package com.balex.familyteam.domain.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("nickName")
    val nickName: String = "",
    @SerializedName("isAdmin")
    val isAdmin: Boolean = false,
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
): Parcelable {
    companion object {
        const val ERROR_LOADING_USER_DATA_FROM_FIREBASE = "ERROR_LOADING_USER_DATA_FROM_FIREBASE"
    }
}


