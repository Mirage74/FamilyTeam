package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalTask(
    @SerializedName("task")
    val task: Task = Task(),
    @SerializedName("taskOwner")
    val taskOwner: String = ""
)
