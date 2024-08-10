package com.balex.familyteam.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class PrivateTasks(
    val privateTasks: List<Task>
)
