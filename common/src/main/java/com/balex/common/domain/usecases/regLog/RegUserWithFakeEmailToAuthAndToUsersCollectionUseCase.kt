package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class RegUserWithFakeEmailToAuthAndToUsersCollectionUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(emailOrPhone: String, nickName: String, displayName: String, password: String) =
        repository.regUserWithFakeEmailToAuthAndToUsersCollection(emailOrPhone, nickName, displayName, password)
}