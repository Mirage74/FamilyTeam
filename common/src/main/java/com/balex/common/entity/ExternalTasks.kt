package com.balex.common.entity

import kotlinx.serialization.Serializable

@Serializable
data class ExternalTasks(
    val externalTasks: List<ExternalTask> = listOf()
)
