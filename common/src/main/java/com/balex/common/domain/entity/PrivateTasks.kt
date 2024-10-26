package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PrivateTasks(
    @SerializedName("privateTasks")
    val privateTasks: List<Task> = listOf()
)
