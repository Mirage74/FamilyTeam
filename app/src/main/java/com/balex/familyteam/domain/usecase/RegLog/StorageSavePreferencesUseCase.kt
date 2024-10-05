package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class StorageSavePreferencesUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(email: String, nickName: String, password: String, language: String) = repository.storageSavePreferences(email, nickName, password, language)
}