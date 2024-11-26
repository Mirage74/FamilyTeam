package com.balex.common.domain.entity

data class DeletedSubUser(
    val adminEmailOrPhone: String,
    val nickName: String,
    val fakeEmail: String
)
