package com.balex.familyteam.domain.entity

import kotlinx.serialization.Serializable

@Serializable
class ToDoList (
    val thingsToDoShared: ExternalTasks = ExternalTasks(listOf()),
    val thingsToDoPrivate: PrivateTasks = PrivateTasks(listOf()),
    val listToShop: List<String> = listOf()
)