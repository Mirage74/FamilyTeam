package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class ExternalTask(
    val task: Task,
    val taskOwner: String
)
