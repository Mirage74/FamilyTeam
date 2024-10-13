package com.balex.common.domain.usecases.regLog

import com.balex.common.domain.repository.RegLogRepository
import javax.inject.Inject

class RemoveRecordFromCollectionUseCase @Inject constructor(
    private val repository: RegLogRepository
) {
    suspend operator fun invoke(collectionName: String, emailOrPhoneNumber: String) = repository.removeRecordFromCollection(collectionName, emailOrPhoneNumber)
}