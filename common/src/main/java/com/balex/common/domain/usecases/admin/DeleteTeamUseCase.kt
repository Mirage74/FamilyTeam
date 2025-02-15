package com.balex.common.domain.usecases.admin

import com.balex.common.domain.repository.AdminRepository
import javax.inject.Inject

@Suppress("unused")
class DeleteTeamUseCase @Inject constructor(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(onNavigateToNonloggedScreen: () -> Unit) = repository.deleteTeam(onNavigateToNonloggedScreen)
}
