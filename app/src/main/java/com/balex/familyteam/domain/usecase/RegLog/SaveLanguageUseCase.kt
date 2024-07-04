package com.balex.familyteam.domain.usecase.regLog

import com.balex.familyteam.domain.repository.RegLogRepository
import javax.inject.Inject

class SaveLanguageUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    operator fun invoke(language: String) = repository.saveLanguage(language)
}