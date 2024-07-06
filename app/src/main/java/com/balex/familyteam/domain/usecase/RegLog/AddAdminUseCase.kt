package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class AddAdminUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(admin: Admin) = repository.addAdmin(admin)
}