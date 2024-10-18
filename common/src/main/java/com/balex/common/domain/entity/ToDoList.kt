package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
class ToDoList (
    val thingsToDoShared: ExternalTasks = ExternalTasks(listOf()),
    val thingsToDoPrivate: PrivateTasks = PrivateTasks(listOf()),
    val thingsToDoForOtherUsers: ExternalTasks = ExternalTasks(listOf()),
    val listToShop: List<String> = listOf()
)