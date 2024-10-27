package com.balex.logged_user

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.common.domain.entity.ExternalTasks
import com.balex.common.domain.entity.PrivateTasks
import com.balex.common.domain.entity.ToDoList
import com.balex.common.domain.entity.User
import com.balex.common.extensions.*
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.ObserveUserUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.common.domain.usecases.regLog.StorageSavePreferencesUseCase
import com.balex.common.domain.usecases.user.ObserveUsersListUseCase
import com.balex.common.domain.usecases.user.RemoveUserUseCase
import com.balex.common.R
import com.balex.common.data.repository.TaskMode
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import com.balex.common.domain.usecases.admin.CreateNewUserUseCase
import com.balex.common.domain.usecases.user.AddExternalTaskToFirebaseUseCase
import com.balex.common.domain.usecases.user.AddPrivateTaskToFirebaseUseCase
import com.balex.common.domain.usecases.user.DeleteTaskFromFirebaseUseCase
import com.balex.logged_user.LoggedUserStore.Intent
import com.balex.logged_user.LoggedUserStore.Label
import com.balex.logged_user.LoggedUserStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface LoggedUserStore : Store<Intent, State, Label> {

    fun stopBootstrapperCollectFlow()

    sealed interface Intent {

        data object BackFromNewTaskFormClicked : Intent

        data object ClickedAddNewTask : Intent

        data class ClickedEditTask(
            val externalTask: ExternalTask,
            val taskType: UserRepositoryImpl.Companion.TaskType
        ) : Intent

        data class ClickedAddPrivateTaskOrEditToFirebase(val task: Task, val taskMode: TaskMode) :
            Intent

        data class ClickedAddExternalTaskOrEditToFirebase(
            val externalTask: ExternalTask,
            val taskMode: TaskMode
        ) : Intent

        data class ClickedDeleteTask(
            val externalTask: ExternalTask,
            val taskType: UserRepositoryImpl.Companion.TaskType
        ) : Intent

        data object ClickedCreateNewUser : Intent

        data object ClickedEditUsersList : Intent

        data object ClickedRegisterNewUserInFirebase : Intent

        data object ClickedCancelRegisterNewUserInFirebase : Intent

        data class ClickedRemoveUserFromFirebase(val nickName: String) : Intent

        data class ChangePage(val page: PagesNames) : Intent

        data class NickNameFieldChanged(val currentNickNameText: String) : Intent

        data class DisplayNameFieldChanged(val currentDisplayNameText: String) : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data object ClickedChangePasswordVisibility : Intent

        data object ClickedAbout : Intent

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

    }

    data class State(
        val user: User,
        val usersNicknamesList: List<String>,
        val isAddTaskClicked: Boolean,
        val isEditTaskClicked: Boolean,
        val taskForEdit: ExternalTask,
        val taskType: UserRepositoryImpl.Companion.TaskType,
        //val isAddOrEditTaskToFirebaseClicked: Boolean,
        val isWrongTaskData: Boolean,
        val isCreateNewUserClicked: Boolean,
        val isEditUsersListClicked: Boolean,
        val passwordVisible: Boolean,
        val nickName: String,
        val displayName: String,
        val password: String,
        val isPasswordEnabled: Boolean,
        val isRegisterInFirebaseButtonEnabled: Boolean,
        val isCancelBtnEnabled: Boolean,
        val language: String,
        val activeBottomItem: PagesNames,
        val todoList: ToDoList,
        val myTasksForOtherUsersList: ExternalTasks,
        val loggedUserState: LoggedUserState
    ) {
        sealed interface LoggedUserState {

            data object Loading : LoggedUserState

            data object Content : LoggedUserState
        }
    }

    sealed interface Label {

        data object ClickedAbout : Label

    }
}

class LoggedUserStoreFactory @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val createNewUserUseCase: CreateNewUserUseCase,
    private val storageSavePreferencesUseCase: StorageSavePreferencesUseCase,
    private val removeUserUseCase: RemoveUserUseCase,
    private val deleteTaskFromFirebaseUseCase: DeleteTaskFromFirebaseUseCase,
    private val observeUsersListUseCase: ObserveUsersListUseCase,
    private val addPrivateTaskToFirebaseUseCase: AddPrivateTaskToFirebaseUseCase,
    private val addExternalTaskToFirebaseUseCase: AddExternalTaskToFirebaseUseCase,
    private val storeFactory: StoreFactory,
    context: Context
) {

    val appContext: Context = context.applicationContext

    fun create(language: String): LoggedUserStore =
        object : LoggedUserStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoggedUserStore",
            initialState = State(
                getUserUseCase(),
                listOf(),
                isAddTaskClicked = false,
                isEditTaskClicked = false,
                taskForEdit = ExternalTask(),
                taskType = UserRepositoryImpl.Companion.TaskType.PRIVATE,
                //isAddOrEditTaskToFirebaseClicked = false,
                isWrongTaskData = false,
                isCreateNewUserClicked = false,
                isEditUsersListClicked = false,
                passwordVisible = false,
                nickName = "",
                displayName = "",
                password = "",
                isPasswordEnabled = false,
                isCancelBtnEnabled = false,
                isRegisterInFirebaseButtonEnabled = false,
                language = language,
                activeBottomItem = PagesNames.TodoList,
                todoList = ToDoList(),
                myTasksForOtherUsersList = ExternalTasks(listOf()),
                loggedUserState = State.LoggedUserState.Loading
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
            private val bootstrapper: BootstrapperImpl = BootstrapperImpl()

            override fun stopBootstrapperCollectFlow() {
                bootstrapper.stop()
            }
        }

    private sealed interface Action {

        data class UserIsChanged(val user: User) : Action

        data class UsersListIsChanged(val usersList: List<String>) : Action

        data class ExternalTasksListIsChanged(val externalTasksList: ExternalTasks) : Action

        data class PrivateTasksListIsChanged(val privateTasksList: PrivateTasks) : Action

        data class ShopListIsChanged(val shopList: List<String>) : Action

        data class MyTasksForOtherUsersListIsChanged(val myTasksForOtherUsersList: ExternalTasks) :
            Action

        data class PageIsChanged(val page: PagesNames) : Action

        data class LanguageIsChanged(val language: String) : Action

    }

    private sealed interface Msg {

        data object BackFromNewTaskFormClicked : Msg

        data object ButtonAddTaskClicked : Msg

        data class ClickedEditTask(
            val externalTask: ExternalTask,
            val taskType: UserRepositoryImpl.Companion.TaskType
        ) : Msg

        data object ButtonAddTaskToFirebaseOrEditClickedAndTaskDataIsCorrect : Msg

        data object ButtonAddTaskToFirebaseOrEditClickedButTaskDataIsIncorrect : Msg

        data class UserIsChanged(val user: User) : Msg

        data class UsersListIsChanged(val usersList: List<String>) : Msg

        data class ExternalTasksListIsChanged(val externalTasksList: ExternalTasks) : Msg

        data class PrivateTasksListIsChanged(val privateTasksList: PrivateTasks) : Msg

        data class ShopListIsChanged(val shopList: List<String>) : Msg

        data class MyTasksForOtherUsersListIsChanged(val myTasksForOtherUsersList: ExternalTasks) :
            Msg

        data class PageIsChanged(val page: PagesNames) : Msg

        data class UpdateNickNameFieldText(val currentNickNameText: String) : Msg

        data object ButtonCreateNewUserClicked : Msg

        data object ClickedCancelRegisterNewUserInFirebase : Msg

        data object ButtonRegisterNewUserInFirebaseClicked : Msg

        data object ButtonEditUsersListClicked : Msg

        data object NickNameMatched : Msg

        data object NickNameNotMatched : Msg

        data class UpdateDisplayNameFieldText(val currentDisplayNameText: String) : Msg

        data class UpdatePasswordFieldText(val currentPasswordText: String) : Msg

        data object ChangePasswordVisibility : Msg

        data object PasswordMatched : Msg

        data object PasswordNotMatched : Msg

        data class LanguageIsChanged(val language: String) : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        private var userJob: Job? = null
        private var languageJob: Job? = null
        private var usersListJob: Job? = null

        override fun invoke() {
            start()
        }

        fun stop() {
            userJob?.cancel()
            languageJob?.cancel()
            usersListJob?.cancel()
        }

        fun start() {
            userJob = scope.launch {
                observeUserUseCase().collect {
                    dispatch(Action.UserIsChanged(it))
                }
            }

            languageJob = scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }

            usersListJob = scope.launch {
                observeUsersListUseCase().collect {
                    dispatch(Action.UsersListIsChanged(it))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {

                Intent.BackFromNewTaskFormClicked -> {
                    dispatch(Msg.BackFromNewTaskFormClicked)
                }

                Intent.ClickedAddNewTask -> {
                    dispatch(Msg.ButtonAddTaskClicked)
                }

                is Intent.ClickedEditTask -> {
                    dispatch(Msg.ClickedEditTask(intent.externalTask, intent.taskType))
                }

                is Intent.ClickedDeleteTask -> {
                    scope.launch {
                        deleteTaskFromFirebaseUseCase(intent.externalTask, intent.taskType)
                    }
                }

                is Intent.ClickedAddPrivateTaskOrEditToFirebase -> {
                    if (intent.task.checkData()) {
                        scope.launch {
                            addPrivateTaskToFirebaseUseCase(intent.task, intent.taskMode)
                        }
                        dispatch(Msg.ButtonAddTaskToFirebaseOrEditClickedAndTaskDataIsCorrect)
                    } else {
                        dispatch(Msg.ButtonAddTaskToFirebaseOrEditClickedButTaskDataIsIncorrect)
                    }
                }

                is Intent.ClickedAddExternalTaskOrEditToFirebase -> {
                    scope.launch {
                        if (intent.externalTask.task.checkData()) {
                            scope.launch {
                                addExternalTaskToFirebaseUseCase(
                                    intent.externalTask,
                                    intent.taskMode
                                )
                            }
                            dispatch(Msg.ButtonAddTaskToFirebaseOrEditClickedAndTaskDataIsCorrect)
                        } else {
                            dispatch(Msg.ButtonAddTaskToFirebaseOrEditClickedButTaskDataIsIncorrect)
                        }
                    }
                }

                Intent.ClickedCreateNewUser -> {
                    dispatch(Msg.ButtonCreateNewUserClicked)
                }

                is Intent.ClickedRegisterNewUserInFirebase -> {
                    scope.launch {
                        createNewUserUseCase(
                            User(
                                nickName = getState().nickName,
                                displayName = getState().displayName,
                                password = getState().password
                            )
                        )
                    }
                    dispatch(Msg.ButtonRegisterNewUserInFirebaseClicked)
                }

                Intent.ClickedCancelRegisterNewUserInFirebase -> {
                    dispatch(Msg.ClickedCancelRegisterNewUserInFirebase)
                }

                Intent.ClickedEditUsersList -> TODO()
                is Intent.ClickedRemoveUserFromFirebase -> TODO()

                is Intent.ChangePage -> {
                    dispatch(Msg.PageIsChanged(intent.page))
                }

                Intent.ClickedChangePasswordVisibility -> {
                    dispatch(Msg.ChangePasswordVisibility)
                }

                is Intent.NickNameFieldChanged -> {
                    val text = if (intent.currentNickNameText.length == 1) {
                        intent.currentNickNameText.replace(Regex(REGEX_PATTERN_NOT_LETTERS), "")
                    } else {
                        intent.currentNickNameText.replace(
                            Regex
                                (REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE), ""
                        )
                    }

                    dispatch(Msg.UpdateNickNameFieldText(text))
                    if (text.length >= appContext.resources.getInteger(R.integer.min_nickName_length)) {
                        dispatch(Msg.NickNameMatched)
                    } else {
                        dispatch(Msg.NickNameNotMatched)
                    }
                }

                is Intent.DisplayNameFieldChanged -> {
                    var text = intent.currentDisplayNameText.replace(
                        Regex(
                            REGEX_PATTERN_NOT_ANY_LETTERS_NUMBERS_UNDERSCORE
                        ), ""
                    )
                    if (text.length > appContext.resources.getInteger(R.integer.max_displayName_length)) {
                        text = text.substring(
                            0,
                            appContext.resources.getInteger(R.integer.max_displayName_length)
                        )
                    }
                    dispatch(Msg.UpdateDisplayNameFieldText(text))
                }

                is Intent.PasswordFieldChanged -> {
                    dispatch(Msg.UpdatePasswordFieldText(intent.currentPasswordText))
                    if (intent.currentPasswordText.length >= appContext.resources.getInteger(R.integer.min_password_length)) {
                        dispatch(Msg.PasswordMatched)
                    } else {
                        dispatch(Msg.PasswordNotMatched)
                    }
                }

                is Intent.ClickedAbout -> {
                    publish(Label.ClickedAbout)
                }

                Intent.RefreshLanguage -> {
                    dispatch(Msg.LanguageIsChanged(getState().language))
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {

                is Action.UserIsChanged -> {
                    if (action.user.nickName.length >= appContext.resources.getInteger(R.integer.min_nickName_length)
                        && (action.user.nickName != NO_USER_SAVED_IN_SHARED_PREFERENCES
                                && action.user.nickName != User.DEFAULT_NICK_NAME)
                    ) {
                        storageSavePreferencesUseCase(
                            action.user.adminEmailOrPhone,
                            action.user.nickName,
                            action.user.password,
                            action.user.language
                        )
                        dispatch(Msg.UserIsChanged(action.user))
                    }
                }

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
                }

                is Action.PageIsChanged -> {
                    dispatch(Msg.PageIsChanged(action.page))
                }

                is Action.UsersListIsChanged -> {
                    dispatch(Msg.UsersListIsChanged(action.usersList))
                }

                is Action.ExternalTasksListIsChanged -> {
                    dispatch(Msg.ExternalTasksListIsChanged(action.externalTasksList))
                }

                is Action.PrivateTasksListIsChanged -> {
                    dispatch(Msg.PrivateTasksListIsChanged(action.privateTasksList))
                }

                is Action.ShopListIsChanged -> {
                    dispatch(Msg.ShopListIsChanged(action.shopList))
                }

                is Action.MyTasksForOtherUsersListIsChanged -> {
                    dispatch(Msg.MyTasksForOtherUsersListIsChanged(action.myTasksForOtherUsersList))

                }


            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {

                Msg.BackFromNewTaskFormClicked -> {
                    copy(isAddTaskClicked = false)
                }

                Msg.ButtonAddTaskClicked -> {
                    copy(
                        isAddTaskClicked = true
                    )
                }

                is Msg.ClickedEditTask -> {
                    copy(
                        isEditTaskClicked = true,
                        taskForEdit = msg.externalTask,
                        taskType = msg.taskType
                    )
                }

                Msg.ButtonAddTaskToFirebaseOrEditClickedAndTaskDataIsCorrect -> {
                    copy(
                        isAddTaskClicked = false,
                        isEditTaskClicked = false,
                        isWrongTaskData = false,
                        taskForEdit = ExternalTask()
                    )
                }

                Msg.ButtonAddTaskToFirebaseOrEditClickedButTaskDataIsIncorrect -> {
                    copy(
                        //isAddOrEditTaskToFirebaseClicked = false,
                        isWrongTaskData = true
                    )
                }

                is Msg.UserIsChanged -> {
                    if (msg.user.nickName != User.DEFAULT_NICK_NAME) {
                        copy(
                            user = msg.user,
                            loggedUserState = State.LoggedUserState.Content
                        )
                    } else {
                        copy(
                            user = msg.user
                        )
                    }
                }

                is Msg.UsersListIsChanged -> {
                    copy(
                        usersNicknamesList = msg.usersList
                    )
                }

                is Msg.ExternalTasksListIsChanged -> {
                    val list = ToDoList(
                        thingsToDoShared = msg.externalTasksList,
                        thingsToDoPrivate = todoList.thingsToDoPrivate,
                        listToShop = todoList.listToShop
                    )
                    copy(todoList = list)
                }

                is Msg.PrivateTasksListIsChanged -> {
                    val list = ToDoList(
                        thingsToDoShared = todoList.thingsToDoShared,
                        thingsToDoPrivate = msg.privateTasksList,
                        listToShop = todoList.listToShop
                    )
                    copy(todoList = list)
                }

                is Msg.ShopListIsChanged -> {
                    val list = ToDoList(
                        thingsToDoShared = todoList.thingsToDoShared,
                        thingsToDoPrivate = todoList.thingsToDoPrivate,
                        listToShop = msg.shopList
                    )
                    copy(todoList = list)
                }

                is Msg.MyTasksForOtherUsersListIsChanged -> {
                    copy(myTasksForOtherUsersList = msg.myTasksForOtherUsersList)
                }


                is Msg.LanguageIsChanged -> {
                    copy(language = msg.language)
                }

                is Msg.PageIsChanged -> {
                    copy(activeBottomItem = msg.page)
                }

                Msg.ButtonCreateNewUserClicked -> {
                    copy(isCreateNewUserClicked = true)
                }

                Msg.ButtonEditUsersListClicked -> {
                    copy(isEditUsersListClicked = true)
                }

                is Msg.UpdateNickNameFieldText -> {
                    copy(nickName = msg.currentNickNameText)
                }

                is Msg.UpdatePasswordFieldText -> {
                    copy(password = msg.currentPasswordText)
                }

                Msg.ChangePasswordVisibility -> {
                    copy(passwordVisible = !passwordVisible)
                }

                Msg.NickNameMatched -> {
                    copy(isPasswordEnabled = true)
                }

                Msg.NickNameNotMatched -> {
                    copy(isPasswordEnabled = false)
                }

                is Msg.UpdateDisplayNameFieldText -> {
                    copy(displayName = msg.currentDisplayNameText)
                }

                Msg.PasswordMatched -> {
                    copy(isRegisterInFirebaseButtonEnabled = true)
                }

                Msg.PasswordNotMatched -> {
                    copy(isRegisterInFirebaseButtonEnabled = false)
                }

                Msg.ButtonRegisterNewUserInFirebaseClicked -> {
                    copy(isCreateNewUserClicked = false)
                }

                Msg.ClickedCancelRegisterNewUserInFirebase -> {
                    copy(isCreateNewUserClicked = false)
                }
            }
    }
}
