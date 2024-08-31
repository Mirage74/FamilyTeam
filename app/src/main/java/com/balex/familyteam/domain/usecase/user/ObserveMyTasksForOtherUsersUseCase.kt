package com.balex.familyteam.domain.usecase.user

import com.balex.familyteam.domain.repository.UserRepository
import javax.inject.Inject

class ObserveMyTasksForOtherUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke() = repository.observeMyTasksForOtherUsers()
}