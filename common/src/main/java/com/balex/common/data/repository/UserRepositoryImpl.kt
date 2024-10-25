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
import com.balex.common.extensions.numberOfReminders
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val isCurrentUsersListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun observeUsersList(): StateFlow<List<String>> {
        val job = Job()
        return flow {
            while (job.isActive) {
                usersNicknamesList = getUsersListFromFirebase()
                if (usersNicknamesList.isNotEmpty()) {
                    emit(usersNicknamesList)
                    job.complete()
                    isCurrentUsersListNeedRefreshFlow.collect {
                        emit(usersNicknamesList)
                    }
                }
            }
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
    }



    override suspend fun emitUsersNicknamesListNeedRefresh() {
        coroutineScope.launch {
            usersNicknamesList = getUsersListFromFirebase()
            isCurrentUsersListNeedRefreshFlow.emit(Unit)
        }
    }

    override suspend fun removeUser(nickName: String) {
        TODO("Not yet implemented")
    }



    override suspend fun addPrivateTaskToFirebase(task: Task) {

        val userForModify = getUserUseCase()

        if (userForModify.availableTasksToAdd > 0) {
            val toDoOld = userForModify.listToDo
            val updatedTodoList = toDoOld.copy(
                thingsToDoPrivate = toDoOld.thingsToDoPrivate.copy(
                    privateTasks = toDoOld.thingsToDoPrivate.privateTasks + task
                )
            )
            val userForUpdate = userForModify.copy(
                listToDo = updatedTodoList,
                availableTasksToAdd = userForModify.availableTasksToAdd - 1,
                availableFCM = userForModify.availableFCM - task.numberOfReminders()
            )
            val userCollection =
                usersCollection.document(userForModify.adminEmailOrPhone)
                    .collection(userForModify.nickName.lowercase())
                    .document(userForModify.nickName.lowercase())

            try {
                userCollection.set(userForUpdate).await()
            } catch (e: Exception) {
                Log.d("addPrivateTaskToFirebase error", e.toString())
            }
        } else {
            Log.d("addPrivateTaskToFirebase error", "No available tasks to add")
        }
    }

    override suspend fun addExternalTaskToFirebase(externalTask: ExternalTask) {
        val currentUser = getUserUseCase()

        if (currentUser.availableTasksToAdd > 0) {
            val toDoOld = currentUser.listToDo
            val updatedTodoList = toDoOld.copy(
                thingsToDoForOtherUsers = toDoOld.thingsToDoForOtherUsers.copy(
                    externalTasks = toDoOld.thingsToDoForOtherUsers.externalTasks + externalTask
                )
            )
            val userForUpdate = currentUser.copy(
                listToDo = updatedTodoList,
                availableTasksToAdd = currentUser.availableTasksToAdd - 1,
                availableFCM = currentUser.availableFCM - externalTask.task.numberOfReminders()
            )
            val userCollection =
                usersCollection.document(currentUser.adminEmailOrPhone)
                    .collection(currentUser.nickName.lowercase())
                    .document(currentUser.nickName.lowercase())

            try {
                userCollection.set(userForUpdate).await()
            } catch (e: Exception) {
                Log.d("addExternalTaskToFirebase error modify user", e.toString())
            }


            val externalUserCollection =
                usersCollection.document(currentUser.adminEmailOrPhone)
                    .collection(externalTask.taskOwner.lowercase())
                    .document(externalTask.taskOwner.lowercase())
            try {
                val externalUserSnapshot = externalUserCollection.get().await()
                val externalUser = externalUserSnapshot?.toObject(User::class.java)
                if (externalUser != null) {
                    val toDoOldExternal = externalUser.listToDo
                    val updatedTodoListExternal = toDoOldExternal.copy(
                        thingsToDoShared = toDoOldExternal.thingsToDoShared.copy(
                            externalTasks = toDoOldExternal.thingsToDoShared.externalTasks + externalTask.copy(taskOwner = currentUser.nickName)
                        )
                    )
                    val userForUpdateExternal = externalUser.copy(
                        listToDo = updatedTodoListExternal
                    )

                    externalUserCollection.set(userForUpdateExternal).await()
                }

            } catch (e: Exception) {
                Log.d("addExternalTaskToFirebase error external user", e.toString())
            }

        } else {
            Log.d("addExternalTaskToFirebase error", "No available tasks to add")
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
        val usersList = mutableListOf<String>()
        return try {
            val adminDocumentSnapshot = adminsCollection
                .document(admin.emailOrPhoneNumber)
                .get()
                .await()

            val adminData = adminDocumentSnapshot?.toObject(Admin::class.java)


            if (adminData != null) {
                usersList.addAll(adminData.usersNickNamesList)
            } else {
                usersList.add(admin.nickName)
            }

            usersList

        } catch (exception: Exception) {
            //exception.printStackTrace()
            //throw RuntimeException("getUsersListFromFirebase: $ERROR_GET_USERS_LIST_FROM_FIREBASE")
            Log.d(ERROR_GET_USERS_LIST_FROM_FIREBASE, exception.toString())
            usersList
        }

    }


    companion object {
        const val ERROR_GET_USERS_LIST_FROM_FIREBASE = "ERROR_GET_USERS_LIST_FROM_FIREBASE"
    }
}