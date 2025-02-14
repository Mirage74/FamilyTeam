package com.balex.common.domain.entity

@Suppress("unused")
data class DeletedAdmin(
    val emailOrPhoneNumber: String,
    val nickName: String,
    val fakeEmail: String,
    val usersNickNamesList: List<String> = emptyList()
)