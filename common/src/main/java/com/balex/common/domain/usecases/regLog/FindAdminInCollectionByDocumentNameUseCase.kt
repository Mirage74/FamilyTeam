package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class FindAdminInCollectionByDocumentNameUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(email: String) = repository.findAdminInCollectionByDocumentName(email)
}