package com.balex.familyteam.data.repository

import android.content.Context
import com.balex.familyteam.data.datastore.Storage
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.RegLogRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegLogRepositoryImpl @Inject constructor(
    private val context: Context
) : RegLogRepository {

    private var _user = User()
    private val user: User
        get() = _user.copy()

    private val isCurrentUserNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun observeUser(): StateFlow<User> = flow {
        val adminFromStorage = Storage.getUser(context)
        if (adminFromStorage != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {


        }
        isCurrentUserNeedRefreshFlow.emit(Unit)

        isCurrentUserNeedRefreshFlow.collect {
            emit(user)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = user
        )

    override fun observeAdmin(): StateFlow<Admin> {
        TODO("Not yet implemented")
    }

    override fun registerAdmin(email: String, phone: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun loginAdmin(email: String, phone: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun loginUser(email: String, password: String) {
        TODO("Not yet implemented")
    }


}