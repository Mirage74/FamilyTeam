package com.balex.common.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class Task(
    val description: String,
    @Contextual
    val cutoffTime: Calendar
)
