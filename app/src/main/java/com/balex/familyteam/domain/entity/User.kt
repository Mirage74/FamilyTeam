package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val login: String = "",
    val isAdmin: Boolean = false,
    val adminName: String = "",
    val name: String = "",
    val listToDo: ToDoList = ToDoList(),
): Parcelable
