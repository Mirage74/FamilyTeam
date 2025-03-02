package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
data class Reminder(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("alarmTime")
    val alarmTime: Long = 0,
    @SerializedName("deviceToken")
    val deviceToken: String = ""
)
