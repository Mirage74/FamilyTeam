package com.balex.familyteam.data.repository

import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.familyteam.domain.entity.ExternalTasks
import com.balex.familyteam.domain.entity.PrivateTasks
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.repository.UserRepository
import com.balex.familyteam.domain.usecase.regLog.AddUserUseCase
import com.balex.familyteam.domain.usecase.regLog.GetUserUseCase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val addUserUseCase: AddUserUseCase
) : UserRepository {

    private var usersList = mutableListOf(User())

    private val externalTasksList = ExternalTasks()

    private val privateTasksList = PrivateTasks()

    private val shopList = mutableListOf<String>()

    private val myTasksForOtherUsersList = ExternalTasks()

    private val isCurrentUsersListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentExternalTasksListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentPrivateTasksListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentShopListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)
    private val isCurrentMyTasksForOtherUsersListNeedRefreshFlow =
        MutableSharedFlow<Unit>(replay = 1)

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun observeUsersList(): StateFlow<List<User>> = flow {
        getUsersListFromFirebase { documents ->
            if (documents.isNotEmpty()) {
                val list = mutableListOf<User>()
                documents.forEach { document ->
                    document?.toObject(User::class.java)?.let {
                        list.add(it)
                    }
                }
                usersList = list.toMutableList()
            }
        }
        isCurrentUsersListNeedRefreshFlow.emit(Unit)
        isCurrentUsersListNeedRefreshFlow.collect {
            emit(usersList)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = usersList
        )

    override suspend fun removeUser(nickName: String) {
        TODO("Not yet implemented")
    }

    override fun observeExternalTasks(): StateFlow<ExternalTasks> {
        TODO("Not yet implemented")
    }

    override fun observePrivateTasks(): StateFlow<PrivateTasks> {
        TODO("Not yet implemented")
    }

    override fun observeListToShop(): StateFlow<List<String>> {
        TODO("Not yet implemented")
    }

    override fun observeMyTasksForOtherUsers(): StateFlow<ExternalTasks> {
        TODO("Not yet implemented")
    }

    private fun getUsersListFromFirebase(
        callback: (List<DocumentSnapshot?>) -> Unit
    ) {
        usersCollection
            .whereNotEqualTo("nickName", getUserUseCase().nickName)
            .get(Source.SERVER)
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    callback(documents.documents)
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                //throw RuntimeException("getUsersListFromFirebase: $ERROR_GET_USERS_LIST_FROM_FIREBASE")
            }
    }

    override suspend fun createNewUser(user: User) {
        coroutineScope.launch {
            val resultFirebaseAddUser = addUserUseCase(user)
            if (resultFirebaseAddUser.isSuccess) {
                isCurrentUsersListNeedRefreshFlow.emit(Unit)

            } else {
                throw RuntimeException("createNewUser: $ERROR_CREATE_NEW_USER_IN_FIREBASE")
            }
        }
    }

    companion object {
        const val ERROR_GET_USERS_LIST_FROM_FIREBASE = "ERROR_GET_USERS_LIST_FROM_FIREBASE"
        const val ERROR_CREATE_NEW_USER_IN_FIREBASE = "ERROR_CREATE_NEW_USER_IN_FIREBASE"
    }
}