package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ToDoList (
    @SerializedName("thingsToDoShared")
    val thingsToDoShared: ExternalTasks = ExternalTasks(listOf()),
    @SerializedName("thingsToDoPrivate")
    val thingsToDoPrivate: PrivateTasks = PrivateTasks(listOf()),
    @SerializedName("thingsToDoForOtherUsers")
    val thingsToDoForOtherUsers: ExternalTasks = ExternalTasks(listOf()),
    @SerializedName("listToShop")
    val listToShop: List<String> = listOf()
)