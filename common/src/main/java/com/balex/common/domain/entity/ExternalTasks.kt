package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalTasks(
    @SerializedName("externalTasks")
    val externalTasks: List<ExternalTask> = listOf()
)
