package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
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