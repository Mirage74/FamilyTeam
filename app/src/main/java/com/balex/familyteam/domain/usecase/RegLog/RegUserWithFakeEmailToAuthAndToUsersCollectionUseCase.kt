package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class RegUserWithFakeEmailToAuthAndToUsersCollectionUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(emailOrPhone: String, nickName: String, displayName: String, password: String) =
        repository.regUserWithFakeEmailToAuthAndToUsersCollection(emailOrPhone, nickName, displayName, password)
}