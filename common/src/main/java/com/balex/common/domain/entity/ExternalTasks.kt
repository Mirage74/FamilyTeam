package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class ExternalTasks(
    val externalTasks: List<ExternalTask> = listOf()
)
