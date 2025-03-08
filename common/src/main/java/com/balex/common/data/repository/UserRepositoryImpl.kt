package com.balex.common.data.repository

import android.content.Context
import com.balex.common.R
import com.balex.common.data.datastore.Storage
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.NO_NEW_TOKEN
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.NO_NOTIFICATION_PERMISSION_GRANTED
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.Reminder
import com.balex.common.domain.entity.Task
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.UserRepository
import com.balex.common.domain.usecases.regLog.GetRepoAdminUseCase
import com.balex.common.domain.usecases.regLog.GetTokenUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.regLog.SetNewTokenUseCase
import com.balex.common.extensions.isNotEmptyNickName
import com.balex.common.extensions.logExceptionToFirebase
import com.balex.common.extensions.logTextToFirebase
import com.balex.common.extensions.numberOfReminders
import com.balex.common.extensions.toReminder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getTokenUseCase: GetTokenUseCase,
    private val setNewTokenUseCase: SetNewTokenUseCase,
    private val getRepoAdminUseCase: GetRepoAdminUseCase,
    private val context: Context
) : UserRepository {

    @Suppress("unused")
    private var usersNicknamesList: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            coroutineScope.launch {
                isCurrentUsersListNeedRefreshFlow.emit(Unit)
            }
        }
    private val isCurrentUsersListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    private var isAdminUsersNickNamesListListenerRegistered = false

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)
    private val scheduleCollection = db.collection(FIREBASE_SCHEDULERS_COLLECTION)
    private val scheduleDeleteCollection = db.collection(FIREBASE_SCHEDULERS_DELETE_COLLECTION)

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    override fun observeUsersList(): StateFlow<List<String>> {
        return flow {
            usersNicknamesList = getUsersListFromFirebase()
            emit(usersNicknamesList)
            if (!isAdminUsersNickNamesListListenerRegistered) {
                addUsersListListenerInFirebase()
                isAdminUsersNickNamesListListenerRegistered = true
            }

            isCurrentUsersListNeedRefreshFlow.collect {
                if (usersNicknamesList.isNotEmpty()) {
                    emit(usersNicknamesList)
                }
            }
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
    }

    private fun addUsersListListenerInFirebase() {
        val currentUser = getUserUseCase()
        if (currentUser.adminEmailOrPhone != User.DEFAULT_FAKE_EMAIL && currentUser.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {


            val adminCollection = adminsCollection.document(currentUser.adminEmailOrPhone)

            adminCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val admin = it.toObject(Admin::class.java) ?: Admin()
                    if (admin.nickName != Admin.DEFAULT_NICK_NAME) {
                        usersNicknamesList = admin.usersNickNamesList.toMutableList()
                    }
                }
            }
        }
    }


    override suspend fun emitUsersNicknamesListNeedRefresh() {
        coroutineScope.launch {
            usersNicknamesList = getUsersListFromFirebase()
        }
    }

    private suspend fun addRemindersToSchedule(task: Task, token: String) {
        if (token != NO_NEW_TOKEN && token.isNotBlank()) {
            try {
                withContext(Dispatchers.IO) {
                    if (task.alarmTime1 != Task.NO_ALARM) {
                        scheduleCollection.add(task.toReminder(1, token)).await()
                    }
                    if (task.alarmTime2 != Task.NO_ALARM) {
                        scheduleCollection.add(task.toReminder(2, token)).await()
                    }
                    if (task.alarmTime3 != Task.NO_ALARM) {
                        scheduleCollection.add(task.toReminder(3, token)).await()
                    }
                }
            } catch (e: Exception) {
                logExceptionToFirebase("addRemindersToSchedule", e.message.toString())
            }
        }
    }

    private suspend fun addRemindersToDeleteSchedule(task: Task) {
        try {
            if (task.alarmTime1 != Task.NO_ALARM) {
                scheduleDeleteCollection.add(
                    Reminder(id = task.id + 1)
                ).await()

                val document = Firebase.firestore
                    .collection(FIREBASE_SCHEDULERS_COLLECTION)
                    .whereEqualTo("id", task.id + 1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                document?.reference?.delete()?.await()
            }
            if (task.alarmTime2 != Task.NO_ALARM) {
                scheduleDeleteCollection.add(
                    Reminder(id = task.id + 2)
                ).await()

                val document = Firebase.firestore
                    .collection(FIREBASE_SCHEDULERS_COLLECTION)
                    .whereEqualTo("id", task.id + 2)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                document?.reference?.delete()?.await()
            }
            if (task.alarmTime3 != Task.NO_ALARM) {
                scheduleDeleteCollection.add(
                    Reminder(id = task.id + 3)
                ).await()

                val document = Firebase.firestore
                    .collection(FIREBASE_SCHEDULERS_COLLECTION)
                    .whereEqualTo("id", task.id + 3)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                document?.reference?.delete()?.await()
            }
        } catch (e: Exception) {
            logExceptionToFirebase("addRemindersToDeleteSchedule", e.message.toString())
        }
    }

    override suspend fun saveDeviceToken(token: String) {
        val user = getUserUseCase()
        if (token.isNotBlank()) {
            setNewTokenUseCase(token)
        }
        if (user.nickName != User.DEFAULT_NICK_NAME && user.nickName != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            withContext(Dispatchers.IO) {
                val userCollection =
                    usersCollection.document(user.adminEmailOrPhone)
                        .collection(user.nickName.lowercase())
                        .document(user.nickName.lowercase())
                val userSnapshot = userCollection.get().await()
                if (userSnapshot.exists()) {
                    val oldToken = userSnapshot.get("token").toString()
                    if (oldToken.isBlank()) {
                        try {
                            userCollection.update("token", token).await()
                        } catch (e: Exception) {
                            logExceptionToFirebase("saveDeviceToken", e.message.toString())
                        }

                    } else {
                        if (oldToken != token) {
                            userCollection.update("token", token).await()
                            cancelOldRemindersAndCreateNew(oldToken, token)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserToken(userName: String): String {
        var userToken = NO_TOKEN
        val user = getUserUseCase()
        if (user.isNotEmptyNickName()) {
            withContext(Dispatchers.IO) {
                val userCollection =
                    usersCollection.document(user.adminEmailOrPhone)
                        .collection(userName.lowercase())
                        .document(userName.lowercase())
                val userSnapshot = userCollection.get().await()
                if (userSnapshot.exists()) {
                    val tokenFromFirebase = userSnapshot.get("token").toString()
                    if (tokenFromFirebase.isNotBlank()) {
                        userToken = tokenFromFirebase
                    }
                }
            }
        }
        return userToken
    }


    private suspend fun cancelOldRemindersAndCreateNew(oldToken: String, newToken: String) {
        if (newToken != NO_NEW_TOKEN && newToken.isNotBlank()) {
            val listRemindersToCancel = mutableListOf<Reminder>()
            val listRemindersToCreate = mutableListOf<Reminder>()

            try {

                val scheduleSnapshot = scheduleCollection.get().await()
                val scheduleReminders = scheduleSnapshot.documents
                val scheduleRemindersObjects = scheduleSnapshot.documents.mapNotNull { document ->
                    document.toObject(Reminder::class.java)
                }

                listRemindersToCancel.addAll(scheduleRemindersObjects.filter { it.deviceToken == oldToken })

                listRemindersToCreate.addAll(
                    listRemindersToCancel.map { reminder ->
                        reminder.copy(deviceToken = newToken)
                    }
                )


                for (reminderToCancel in listRemindersToCancel) {
                    scheduleDeleteCollection.add(reminderToCancel).await()

                    val documentToDelete = scheduleReminders.firstOrNull { doc ->
                        val reminder = doc.toObject(Reminder::class.java)
                        reminder?.id == reminderToCancel.id
                    }

                    if (documentToDelete != null) {
                        documentToDelete.reference.delete().await()
                        break
                    }
                }

                listRemindersToCreate.forEach {
                    scheduleCollection.add(it).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun cancelOldCloudTaskAndCreateNew(oldTask: Task, task: Task, token: String) {
        if (token != NO_NEW_TOKEN && token.isNotBlank()) {
            val listRemindersToCancel = mutableListOf<Reminder>()
            val listRemindersToCreate = mutableListOf<Reminder>()

            if (oldTask.alarmTime1 != task.alarmTime1) {
                if (oldTask.alarmTime1 == Task.NO_ALARM) {
                    listRemindersToCreate.add(task.toReminder(1, token))
                } else if (task.alarmTime1 == Task.NO_ALARM) {
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 1))
                } else {
                    listRemindersToCreate.add(task.toReminder(1, token))
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 1))
                }
            }

            if (oldTask.alarmTime2 != task.alarmTime2) {
                if (oldTask.alarmTime2 == Task.NO_ALARM) {
                    listRemindersToCreate.add(task.toReminder(2, token))
                } else if (task.alarmTime2 == Task.NO_ALARM) {
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 2))
                } else {
                    listRemindersToCreate.add(task.toReminder(2, token))
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 2))
                }
            }

            if (oldTask.alarmTime3 != task.alarmTime3) {
                if (oldTask.alarmTime3 == Task.NO_ALARM) {
                    listRemindersToCreate.add(task.toReminder(3, token))
                } else if (task.alarmTime3 == Task.NO_ALARM) {
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 3))
                } else {
                    listRemindersToCreate.add(task.toReminder(3, token))
                    listRemindersToCancel.add(Reminder(id = oldTask.id + 3))
                }
            }


            try {

                val scheduleSnapshot = scheduleCollection.get().await()
                val scheduleReminders = scheduleSnapshot.documents

                for (reminderToCancel in listRemindersToCancel) {
                    scheduleDeleteCollection.add(reminderToCancel).await()

                    val documentToDelete = scheduleReminders.firstOrNull { doc ->
                        val reminder = doc.toObject(Reminder::class.java)
                        reminder?.id == reminderToCancel.id
                    }

                    if (documentToDelete != null) {
                        documentToDelete.reference.delete().await()
                        break
                    }
                }
            } catch (e: Exception) {
                println("Error deleting from Scheduler, cancelOldCloudTaskANdCreateNew: ${e.message}")
            }

            listRemindersToCreate.forEach {
                scheduleCollection.add(it).await()
            }
        }
    }


    override suspend fun addOrModifyPrivateTaskToFirebase(
        task: Task,
        taskMode: TaskMode,
        token: String
    ) {

        val userForModify = getUserUseCase()
        if (userForModify.isNotEmptyNickName() && token != NO_NEW_TOKEN && token.isNotBlank()) {

            if (userForModify.availableTasksToAdd > 0 || taskMode == TaskMode.EDIT) {
                val toDoOld = userForModify.listToDo
                var oldTask = toDoOld.thingsToDoPrivate.privateTasks.find { it.id == task.id }
                if (oldTask == null) {
                    oldTask = Task()
                }
                val updatedTodoList =
                    if (taskMode == TaskMode.ADD) {
                        toDoOld.copy(
                            thingsToDoPrivate = toDoOld.thingsToDoPrivate.copy(
                                privateTasks = toDoOld.thingsToDoPrivate.privateTasks + task
                            )
                        )
                    } else {
                        if (token != NO_NOTIFICATION_PERMISSION_GRANTED) {
                            cancelOldCloudTaskAndCreateNew(oldTask, task, token)
                        }
                        toDoOld.copy(
                            thingsToDoPrivate = toDoOld.thingsToDoPrivate.copy(
                                privateTasks = toDoOld.thingsToDoPrivate.privateTasks.map { taskItem ->
                                    if (taskItem.id == task.id) task else taskItem
                                }
                            )
                        )
                    }

                val newAvailableFCM = if (taskMode == TaskMode.ADD) {
                    userForModify.availableFCM - task.numberOfReminders()
                } else {
                    val diffReminders = task.numberOfReminders() - oldTask.numberOfReminders()
                    if (diffReminders > 0) {
                        userForModify.availableFCM - diffReminders
                    } else {
                        userForModify.availableFCM
                    }
                }

                val userForUpdate = userForModify.copy(
                    listToDo = updatedTodoList,
                    availableTasksToAdd = userForModify.availableTasksToAdd - 1,
                    availableFCM = newAvailableFCM
                )
                val userCollection =
                    usersCollection.document(userForModify.adminEmailOrPhone)
                        .collection(userForModify.nickName.lowercase())
                        .document(userForModify.nickName.lowercase())

                if (taskMode == TaskMode.ADD) {
                    if (token != NO_NOTIFICATION_PERMISSION_GRANTED) {
                        addRemindersToSchedule(task, token)
                    }
                }

                try {
                    withContext(Dispatchers.IO) {
                        userCollection.set(userForUpdate).await()
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase("addPrivateTaskToFirebase", e.message.toString())
                }
            } else {
                logTextToFirebase("addPrivateTaskToFirebase, no available tasks to add")
            }
        }
    }

    override suspend fun addOrModifyExternalTaskToFirebase(
        externalTask: ExternalTask,
        taskMode: TaskMode
    ) {
        val token = getUserToken(externalTask.taskOwner)
        val currentUser = getUserUseCase()
        if (token != NO_TOKEN && currentUser.isNotEmptyNickName()) {
            if (currentUser.availableTasksToAdd > 0 || taskMode == TaskMode.EDIT) {
                val toDoOld = currentUser.listToDo
                val updatedTodoList =
                    if (taskMode == TaskMode.ADD) {
                        toDoOld.copy(
                            thingsToDoForOtherUsers = toDoOld.thingsToDoForOtherUsers.copy(
                                externalTasks = toDoOld.thingsToDoForOtherUsers.externalTasks + externalTask
                            )
                        )
                    } else {
                        toDoOld.copy(
                            thingsToDoForOtherUsers = toDoOld.thingsToDoForOtherUsers.copy(
                                externalTasks = toDoOld.thingsToDoForOtherUsers.externalTasks.map { taskItem ->
                                    if (taskItem.task.id == externalTask.task.id) externalTask else taskItem
                                }
                            )
                        )
                    }

                val newAvailableFCM = if (taskMode == TaskMode.ADD) {
                    currentUser.availableFCM - externalTask.task.numberOfReminders()
                } else currentUser.availableFCM

                val userForUpdate = currentUser.copy(
                    listToDo = updatedTodoList,
                    availableTasksToAdd = currentUser.availableTasksToAdd - 1,
                    availableFCM = newAvailableFCM
                )
                val userCollection =
                    usersCollection.document(currentUser.adminEmailOrPhone)
                        .collection(currentUser.nickName.lowercase())
                        .document(currentUser.nickName.lowercase())

                try {
                    withContext(Dispatchers.IO) {
                        userCollection.set(userForUpdate).await()
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase("addExternalTaskToFirebase", e.message.toString())
                }


                val externalUserCollection =
                    usersCollection.document(currentUser.adminEmailOrPhone)
                        .collection(externalTask.taskOwner.lowercase())
                        .document(externalTask.taskOwner.lowercase())

                if (taskMode == TaskMode.ADD) {
                    addRemindersToSchedule(externalTask.task, token)
                }

                try {
                    withContext(Dispatchers.IO) {
                        val externalUserSnapshot = externalUserCollection.get().await()
                        val externalUser = externalUserSnapshot?.toObject(User::class.java)
                        if (externalUser != null) {
                            val toDoOldExternal = externalUser.listToDo

                            val updatedTodoListExternal =
                                if (taskMode == TaskMode.ADD) {
                                    toDoOldExternal.copy(
                                        thingsToDoShared = toDoOldExternal.thingsToDoShared.copy(
                                            externalTasks = toDoOldExternal.thingsToDoShared.externalTasks + externalTask.copy(
                                                taskOwner = currentUser.nickName
                                            )
                                        )
                                    )
                                } else {
                                    toDoOldExternal.copy(
                                        thingsToDoShared = toDoOldExternal.thingsToDoShared.copy(
                                            externalTasks = toDoOldExternal.thingsToDoShared.externalTasks.map { taskItem ->
                                                if (taskItem.task.id == externalTask.task.id) {
                                                    externalTask.copy(taskOwner = taskItem.taskOwner)
                                                } else {
                                                    taskItem
                                                }
                                            }
                                        )
                                    )
                                }
                            val userForUpdateExternal = externalUser.copy(
                                listToDo = updatedTodoListExternal
                            )

                            externalUserCollection.set(userForUpdateExternal).await()

                        }
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase("addExternalTaskToFirebase", e.message.toString())
                }

            } else {
                logTextToFirebase("addExternalTaskToFirebase, no available tasks to add")
            }
        }
    }

    override suspend fun deleteTaskFromFirebase(
        externalTask: ExternalTask,
        taskType: TaskType
    ) {
        withContext(Dispatchers.IO) {
            addRemindersToDeleteSchedule(externalTask.task)
        }

        val currentUser = getUserUseCase()

        if (currentUser.isNotEmptyNickName()) {

            val userCollectionCurrentUser =
                usersCollection.document(currentUser.adminEmailOrPhone)
                    .collection(currentUser.nickName.lowercase())
                    .document(currentUser.nickName.lowercase())

            val externalUserCollection =
                usersCollection.document(currentUser.adminEmailOrPhone)
                    .collection(externalTask.taskOwner.lowercase())
                    .document(externalTask.taskOwner.lowercase())

            val currentUserData: User?
            val externalUserData: User?
            withContext(Dispatchers.IO) {
                val documentSnapshotCurrentUser = userCollectionCurrentUser.get().await()
                currentUserData = documentSnapshotCurrentUser?.toObject(User::class.java)
                val externalUserSnapshot = externalUserCollection.get().await()
                externalUserData = externalUserSnapshot?.toObject(User::class.java)
            }

            if (currentUserData != null) {
                if (taskType == TaskType.PRIVATE) {
                    val privateTasksToUpdate =
                        currentUserData.listToDo.thingsToDoPrivate.privateTasks.filterNot { task ->
                            task.id == externalTask.task.id
                        }
                    val listToDoForUpdate = currentUserData.listToDo.copy(
                        thingsToDoPrivate = PrivateTasks(privateTasks = privateTasksToUpdate)
                    )
                    try {
                        withContext(Dispatchers.IO) {
                            userCollectionCurrentUser.update("listToDo", listToDoForUpdate).await()
                        }
                    } catch (e: Exception) {
                        logExceptionToFirebase("deleteTaskFromFirebase", e.message.toString())
                    }
                } else if (taskType == TaskType.MY_TO_OTHER_USER) {
                    val externalTasksToUpdate =
                        currentUserData.listToDo.thingsToDoForOtherUsers.externalTasks.filterNot { externalTasksItem ->
                            externalTasksItem.task.id == externalTask.task.id
                        }
                    val listToDoForUpdate = currentUserData.listToDo.copy(
                        thingsToDoForOtherUsers = ExternalTasks(externalTasks = externalTasksToUpdate)
                    )
                    try {
                        withContext(Dispatchers.IO) {
                            userCollectionCurrentUser.update("listToDo", listToDoForUpdate).await()
                        }
                    } catch (e: Exception) {
                        logExceptionToFirebase("deleteTaskFromFirebase", e.message.toString())
                    }

                    if (externalUserData != null) {
                        val otherUserSharedTasks =
                            externalUserData.listToDo.thingsToDoShared.externalTasks.filterNot { externalTasksItem ->
                                externalTasksItem.task.id == externalTask.task.id
                            }
                        val listToDoOtherUserForUpdate = externalUserData.listToDo.copy(
                            thingsToDoShared = ExternalTasks(externalTasks = otherUserSharedTasks)
                        )
                        try {
                            withContext(Dispatchers.IO) {
                                externalUserCollection.update(
                                    "listToDo",
                                    listToDoOtherUserForUpdate
                                )
                                    .await()
                            }
                        } catch (e: Exception) {
                            logExceptionToFirebase(
                                "deleteTaskFromFirebase, TaskType.MY_TO_OTHER_USER, other user list error",
                                e.message.toString()
                            )
                        }
                    }
                } else if (taskType == TaskType.FROM_OTHER_USER_FOR_ME) {
                    val externalTasksToUpdate =
                        currentUserData.listToDo.thingsToDoShared.externalTasks.filterNot { externalTasksItem ->
                            externalTasksItem.task.id == externalTask.task.id
                        }
                    val listToDoForUpdate = currentUserData.listToDo.copy(
                        thingsToDoShared = ExternalTasks(externalTasks = externalTasksToUpdate)
                    )
                    try {
                        withContext(Dispatchers.IO) {
                            userCollectionCurrentUser.update("listToDo", listToDoForUpdate).await()
                        }
                    } catch (e: Exception) {
                        logExceptionToFirebase(
                            "deleteTaskFromFirebase, TaskType.FROM_OTHER_USER_FOR_ME, my list error",
                            e.message.toString()
                        )
                    }

                    if (externalUserData != null) {
                        val otherUserSharedTasks =
                            externalUserData.listToDo.thingsToDoForOtherUsers.externalTasks.filterNot { externalTasksItem ->
                                externalTasksItem.task.id == externalTask.task.id
                            }
                        val listToDoOtherUserForUpdate = externalUserData.listToDo.copy(
                            thingsToDoForOtherUsers = ExternalTasks(externalTasks = otherUserSharedTasks)
                        )
                        try {
                            withContext(Dispatchers.IO) {
                                externalUserCollection.update(
                                    "listToDo",
                                    listToDoOtherUserForUpdate
                                )
                                    .await()
                            }
                        } catch (e: Exception) {
                            logExceptionToFirebase(
                                "deleteTaskFromFirebase, TaskType.FROM_OTHER_USER_FOR_ME, other user list error",
                                e.message.toString()
                            )
                        }
                    }
                }
            }
        }
    }


    private suspend fun getUsersListFromFirebase(): MutableList<String> {
        val newToken = getTokenUseCase()
        if (newToken != NO_NEW_TOKEN) {
            saveDeviceToken(newToken)
        }
        val admin = getRepoAdminUseCase()
        val usersList = mutableListOf<String>()
        var currentTry = 0

        while (usersList.isEmpty() && currentTry < MAX_TRY_GET_USERS_LIST) {
            delay(DELAY_TRY_GET_USERS_LIST)
            try {
                val adminData: Admin?
                withContext(Dispatchers.IO) {
                    val adminDocumentSnapshot = adminsCollection
                        .document(admin.emailOrPhoneNumber)
                        .get()
                        .await()

                    adminData = adminDocumentSnapshot?.toObject(Admin::class.java)
                }


                if (adminData != null) {
                    usersList.addAll(adminData.usersNickNamesList)
                }


            } catch (exception: Exception) {
                logExceptionToFirebase("getUsersListFromFirebase", exception.message.toString())
            }
            currentTry++
        }
        if (usersList.isEmpty()) {
            throw RuntimeException("getUsersListFromFirebase: $ERROR_GET_USERS_LIST_FROM_FIREBASE")
        }
        return usersList

    }


    override suspend fun exchangeCoins(coins: Int, tasks: Int, reminders: Int) {
        val userForModify = getUserUseCase()
        if (userForModify.isNotEmptyNickName()) {
            val userForUpdate = userForModify.copy(
                teamCoins = coins,
                availableTasksToAdd = tasks,
                availableFCM = reminders
            )

            val userCollection =
                usersCollection.document(userForModify.adminEmailOrPhone)
                    .collection(userForModify.nickName.lowercase())
                    .document(userForModify.nickName.lowercase())

            try {
                withContext(Dispatchers.IO) {
                    userCollection.set(userForUpdate).await()
                }
            } catch (e: Exception) {
                logExceptionToFirebase(
                    "exchangeCoins, error update coins in firebase",
                    e.message.toString()
                )
            }
        }
    }

    override suspend fun setPremiumStatus(premiumStatus: BillingRepositoryImpl.Companion.PremiumStatus) {
        if (premiumStatus != BillingRepositoryImpl.Companion.PremiumStatus.NO_PREMIUM) {

            val userForModify = getUserUseCase()
            if (userForModify.isNotEmptyNickName()) {
                val premiumCostInCoins = when (premiumStatus) {
                    BillingRepositoryImpl.Companion.PremiumStatus.ONE_MONTH -> {
                        context.resources.getInteger(R.integer.premium_account_one_month_cost)
                    }

                    BillingRepositoryImpl.Companion.PremiumStatus.ONE_YEAR -> {
                        context.resources.getInteger(R.integer.premium_account_one_year_cost)
                    }

                    BillingRepositoryImpl.Companion.PremiumStatus.UNLIMITED -> {
                        context.resources.getInteger(R.integer.premium_account_unlimited_cost)
                    }

                    else -> {
                        0
                    }
                }
                val premiumStatusExpiration = when (premiumStatus) {

                    BillingRepositoryImpl.Companion.PremiumStatus.ONE_MONTH -> {
                        if (userForModify.hasPremiumAccount) {
                            userForModify.premiumAccountExpirationDate + MILLIS_IN_MONTH
                        } else {
                            System.currentTimeMillis() + MILLIS_IN_MONTH
                        }
                    }

                    BillingRepositoryImpl.Companion.PremiumStatus.ONE_YEAR -> {
                        if (userForModify.hasPremiumAccount) {
                            userForModify.premiumAccountExpirationDate + MILLIS_IN_YEAR
                        } else {
                            System.currentTimeMillis() + MILLIS_IN_YEAR
                        }
                    }

                    BillingRepositoryImpl.Companion.PremiumStatus.UNLIMITED -> {
                        System.currentTimeMillis() + MILLIS_IN_100_YEAR
                    }

                    else -> {
                        userForModify.premiumAccountExpirationDate
                    }
                }
                val userForUpdate = userForModify.copy(
                    hasPremiumAccount = true,
                    premiumAccountExpirationDate = premiumStatusExpiration,
                    teamCoins = userForModify.teamCoins - premiumCostInCoins
                )

                val userCollection =
                    usersCollection.document(userForModify.adminEmailOrPhone)
                        .collection(userForModify.nickName.lowercase())
                        .document(userForModify.nickName.lowercase())

                try {
                    withContext(Dispatchers.IO) {
                        userCollection.set(userForUpdate).await()
                    }
                } catch (e: Exception) {
                    logExceptionToFirebase(
                        "setPremiumStatus, error setPremiumStatus in firebase",
                        e.message.toString()
                    )
                }
            }
        }
    }

    companion object {

        const val FIREBASE_SCHEDULERS_COLLECTION = "schedule"
        const val FIREBASE_SCHEDULERS_DELETE_COLLECTION = "schedule-delete"
        const val DELAY_TRY_GET_USERS_LIST = 1000L
        const val MAX_TRY_GET_USERS_LIST = 10
        const val ERROR_GET_USERS_LIST_FROM_FIREBASE = "Error getting users list from firebase"

        const val NO_TOKEN = "no token"


        const val MILLIS_IN_MONTH = 31 * 24 * 60 * 60 * 1000L
        const val MILLIS_IN_YEAR = 366 * 24 * 60 * 60 * 1000L
        const val MILLIS_IN_100_YEAR = MILLIS_IN_YEAR * 100

        enum class TaskType {
            PRIVATE,
            FROM_OTHER_USER_FOR_ME,
            MY_TO_OTHER_USER
        }
    }
}

enum class TaskMode {
    ADD, EDIT
}