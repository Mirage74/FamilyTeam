package com.balex.common.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val description: String,
    val cutoffTime: Long
)
