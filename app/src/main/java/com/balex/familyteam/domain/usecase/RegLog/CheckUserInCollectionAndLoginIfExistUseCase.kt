package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class CheckUserInCollectionAndLoginIfExistUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(
        adminEmailOrPhone: String,
        nickName: String,
        password: String
    ) = repository.checkUserInCollectionAndLoginIfExist(adminEmailOrPhone, nickName, password)
}