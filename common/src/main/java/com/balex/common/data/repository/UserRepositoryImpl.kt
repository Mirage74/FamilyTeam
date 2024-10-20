package com.balex.common.data.repository

import android.util.Log
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.Task
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.UserRepository
import com.balex.common.domain.usecases.regLog.AddUserToCollectionUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,

    private val addUserToCollectionUseCase: AddUserToCollectionUseCase
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
        //val teamList = getUsersListFromFirebase()
        //Log.d("documents:", teamList.toString())
//        if (documents.isNotEmpty()) {
//            val list = mutableListOf<User>()
//            documents.forEach { document ->
//                document?.toObject(User::class.java)?.let {
//                    list.add(it)
//                }
//            }
//            usersList = list.toMutableList()
//        }

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

    override suspend fun addPrivateTaskToFirebase(task: Task) {
        try {
            val userForModify = getUserUseCase()
            val toDoOld = userForModify.listToDo
            val updatedTodoList = toDoOld.copy(
                thingsToDoPrivate = toDoOld.thingsToDoPrivate.copy(
                    privateTasks = toDoOld.thingsToDoPrivate.privateTasks + task
                )
            )
            val userCollection =
                usersCollection.document(userForModify.adminEmailOrPhone)
                    .collection(userForModify.nickName.lowercase())
                    .document(userForModify.nickName.lowercase())
            userCollection.update("listToDo", updatedTodoList).await()

        } catch (e: Exception) {
            Log.d("addPrivateTaskToFirebase error", e.toString())
        }
    }

    //    private suspend fun getUsersListFromFirebase(): List<User> {
//        try {
//
//
//            val teamData = usersCollection.document(getUserUseCase().adminEmailOrPhone)
//                .get()
//                .await()
//
//            val dt = teamData.data
//
//            val userList = mutableListOf<User>()
//            if (teamData.data != null) {
//            for (collection in teamData.data!!) {
//
//                    collection.value?.let { toObject(User::class.java)?.let { userList.add(it) }}
//
//            }
//}
//            return userList
//
//        } catch (exception: Exception) {
//            exception.printStackTrace()
//            throw RuntimeException("getUsersListFromFirebase: $ERROR_GET_USERS_LIST_FROM_FIREBASE")
//        }
//    }



    companion object {
        const val ERROR_GET_USERS_LIST_FROM_FIREBASE = "ERROR_GET_USERS_LIST_FROM_FIREBASE"
    }
}