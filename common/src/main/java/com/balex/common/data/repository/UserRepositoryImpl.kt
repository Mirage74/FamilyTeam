package com.balex.common.data.repository

import android.util.Log
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.Task
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.UserRepository
import com.balex.common.domain.usecases.regLog.GetRepoAdminUseCase
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getRepoAdminUseCase: GetRepoAdminUseCase,
) : UserRepository {

    private var usersNicknamesList: MutableList<String> = mutableListOf()

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

    override fun observeUsersList(): StateFlow<List<String>> = flow {
        usersNicknamesList = getUsersListFromFirebase()
        isCurrentUsersListNeedRefreshFlow.emit(Unit)
        isCurrentUsersListNeedRefreshFlow.collect {
            emit(usersNicknamesList)
        }
    }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = usersNicknamesList
        )


    override suspend fun emitUsersNicknamesListNeedRefresh() {
        coroutineScope.launch {
            usersNicknamesList = getUsersListFromFirebase()
            isCurrentUsersListNeedRefreshFlow.emit(Unit)
        }
    }

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

        val userForModify = getUserUseCase()

        if (userForModify.availableTasksToAdd > 0) {
            try {
                val toDoOld = userForModify.listToDo
                val updatedTodoList = toDoOld.copy(
                    thingsToDoPrivate = toDoOld.thingsToDoPrivate.copy(
                        privateTasks = toDoOld.thingsToDoPrivate.privateTasks + task
                    )
                )
                val userForUpdate = userForModify.copy(
                    listToDo = updatedTodoList,
                    availableTasksToAdd = userForModify.availableTasksToAdd - 1
                )
                val userCollection =
                    usersCollection.document(userForModify.adminEmailOrPhone)
                        .collection(userForModify.nickName.lowercase())
                        .document(userForModify.nickName.lowercase())
                userCollection.set(userForUpdate).await()

            } catch (e: Exception) {
                Log.d("addPrivateTaskToFirebase error", e.toString())
            }
        } else {
            Log.d("addPrivateTaskToFirebase error", "No available tasks to add")
        }
    }

    override suspend fun deleteTaskFromFirebase(externalTask: ExternalTask) {
        try {
            val userForModify = getUserUseCase()

            val userCollection =
                usersCollection.document(userForModify.adminEmailOrPhone)
                    .collection(userForModify.nickName.lowercase())
                    .document(userForModify.nickName.lowercase())

            val documentSnapshot = userCollection.get().await()
            val userData = documentSnapshot?.toObject(User::class.java)

            if (userData != null) {
                val privateTasksToUpdate =
                    userData.listToDo.thingsToDoPrivate.privateTasks.filterNot { task ->
                        task.cutoffTime == externalTask.task.cutoffTime
                    }

                val listToDoForUpdate = userData.listToDo.copy(
                    thingsToDoPrivate = PrivateTasks(privateTasks = privateTasksToUpdate)
                )
                userCollection.update("listToDo", listToDoForUpdate).await()
            }
        } catch (e: Exception) {
            Log.d("deleteTaskFromFirebase error", e.toString())
        }
    }

    private suspend fun getUsersListFromFirebase(): MutableList<String> {
        val admin = getRepoAdminUseCase()
        return try {
            val adminDocumentSnapshot = adminsCollection
                .document(admin.emailOrPhoneNumber)
                .get()
                .await()

            val adminData = adminDocumentSnapshot?.toObject(Admin::class.java)
            val usersList = mutableListOf<String>()

            if (adminData != null) {
                usersList.addAll(adminData.usersNickNamesList)
            } else {
                usersList.add(admin.nickName)
            }

            usersList

        } catch (exception: Exception) {
            exception.printStackTrace()
            throw RuntimeException("getUsersListFromFirebase: $ERROR_GET_USERS_LIST_FROM_FIREBASE")
        }
    }


    companion object {
        const val ERROR_GET_USERS_LIST_FROM_FIREBASE = "ERROR_GET_USERS_LIST_FROM_FIREBASE"
    }
}