package com.balex.common.entity

import kotlinx.serialization.Serializable

@Serializable
data class PrivateTasks(
    val privateTasks: List<Task> = listOf()
)
