package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ToDoList (
    val thingsToDoShared: List<String> = listOf(),
    val thingsToDoPrivate: List<String> = listOf(),
    val listToShop: List<String> = listOf()
): Parcelable