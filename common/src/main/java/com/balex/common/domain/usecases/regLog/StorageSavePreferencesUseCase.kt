package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

@Suppress("unused")
class StorageSavePreferencesUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(email: String, nickName: String, password: String, language: String) = repository.storageSavePreferences(email, nickName, password, language)
}