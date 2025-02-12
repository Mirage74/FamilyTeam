package com.balex.common.domain.entity

@Suppress("unused")
data class DeletedSubUser(
    val adminEmailOrPhone: String,
    val nickName: String,
    val fakeEmail: String
)
