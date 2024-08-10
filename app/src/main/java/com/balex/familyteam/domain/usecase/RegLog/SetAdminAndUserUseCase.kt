package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SetAdminAndUserUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(emailOrPhone: String, nickName: String, displayName: String, password: String) =
        repository.setAdminAndUser(emailOrPhone, nickName, displayName, password)
}