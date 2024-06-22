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

class RegLogRepositoryImpl(
    private val context: Context
) : RegLogRepository {

    private var _admin = Admin()
    private val admin: Admin
        get() = _admin.copy()

    private val isCurrentAdminNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun getAdmin(): StateFlow<Admin> = flow {
        val adminFromStorage = Storage.getAdmin(context)
        if (adminFromStorage != Storage.NO_ADMIN_SAVED_IN_SHARED_PREFERENCES) {


        }
        isCurrentAdminNeedRefreshFlow.emit(Unit)

        isCurrentAdminNeedRefreshFlow.collect {
            emit(admin)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = admin
        )

    override fun getUser(): StateFlow<User> {
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