package com.balex.common.domain.usecases.admin

import com.balex.common.domain.repository.AdminRepository
import javax.inject.Inject

@Suppress("unused")
class DeleteSelfAccountUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(userName: String, onNavigateToNonloggedScreen: () -> Unit) =
        repository.deleteSelfAccount(userName, onNavigateToNonloggedScreen)
}