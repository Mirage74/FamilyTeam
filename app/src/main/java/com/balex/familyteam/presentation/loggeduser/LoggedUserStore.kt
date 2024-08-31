package com.balex.familyteam.presentation.loggeduser

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.R
import com.balex.familyteam.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.familyteam.domain.entity.ExternalTasks
import com.balex.familyteam.domain.entity.PrivateTasks
import com.balex.familyteam.domain.entity.ToDoList
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.usecase.regLog.GetUserUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveUserUseCase
import com.balex.familyteam.domain.usecase.regLog.SaveLanguageUseCase
import com.balex.familyteam.domain.usecase.user.CreateNewUserUseCase
import com.balex.familyteam.domain.usecase.user.ObserveExternalTasksUseCase
import com.balex.familyteam.domain.usecase.user.ObserveListToShopUseCase
import com.balex.familyteam.domain.usecase.user.ObserveMyTasksForOtherUsersUseCase
import com.balex.familyteam.domain.usecase.user.ObservePrivateTasksUseCase
import com.balex.familyteam.domain.usecase.user.ObserveUsersListUseCase
import com.balex.familyteam.domain.usecase.user.RemoveUserUseCase
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.Intent
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.Label
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.State
import com.balex.familyteam.presentation.regadmin.RegAdminStoreFactory.Companion.REGEX_PATTERN_NOT_ANY_LETTERS_NUMBERS_UNDERSCORE
import com.balex.familyteam.presentation.regadmin.RegAdminStoreFactory.Companion.REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE
import com.balex.familyteam.presentation.regadmin.RegAdminStoreFactory.Companion.REGEX_PATTERN_NOT_LETTERS

import kotlinx.coroutines.launch
import javax.inject.Inject

interface LoggedUserStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickedCreateNewUser : Intent

        data object ClickedEditUsersList : Intent

        data object ClickedRegisterNewUserInFirebase : Intent

        data object ClickedCancelRegisterNewUserInFirebase : Intent

        data class ClickedRemoveUserFromFirebase(val nickName: String) : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

        data class ChangePage(val page: PagesNames) : Intent

        data class NickNameFieldChanged(val currentNickNameText: String) : Intent

        data class DisplayNameFieldChanged(val currentDisplayNameText: String) : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data object ClickedChangePasswordVisibility : Intent

    }

    data class State(
        val user: User,
        val usersList: List<User>,
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

            data object Initial : LoggedUserState

            data object Loading : LoggedUserState

            data object Content : LoggedUserState
        }
    }

    sealed interface Label {
    }
}

class LoggedUserStoreFactory @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val createNewUserUseCase: CreateNewUserUseCase,
    private val removeUserUseCase: RemoveUserUseCase,
    private val observeUsersListUseCase: ObserveUsersListUseCase,
    private val observeExternalTasksUseCase: ObserveExternalTasksUseCase,
    private val observePrivateTasksUseCase: ObservePrivateTasksUseCase,
    private val observeListToShopUseCase: ObserveListToShopUseCase,
    private val observeMyTasksForOtherUsersUseCase: ObserveMyTasksForOtherUsersUseCase,
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
                loggedUserState = State.LoggedUserState.Initial
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class UserIsChanged(val user: User) : Action

        data class UsersListIsChanged(val usersList: List<User>) : Action

        data class ExternalTasksListIsChanged(val externalTasksList: ExternalTasks) : Action

        data class PrivateTasksListIsChanged(val privateTasksList: PrivateTasks) : Action

        data class ShopListIsChanged(val shopList: List<String>) : Action

        data class MyTasksForOtherUsersListIsChanged(val myTasksForOtherUsersList: ExternalTasks) :
            Action

        data class LanguageIsChanged(val language: String) : Action

        data class PageIsChanged(val page: PagesNames) : Action

    }

    private sealed interface Msg {

        data class UserIsChanged(val user: User) : Msg

        data class UsersListIsChanged(val usersList: List<User>) : Msg

        data class ExternalTasksListIsChanged(val externalTasksList: ExternalTasks) : Msg

        data class PrivateTasksListIsChanged(val privateTasksList: PrivateTasks) : Msg

        data class ShopListIsChanged(val shopList: List<String>) : Msg

        data class MyTasksForOtherUsersListIsChanged(val myTasksForOtherUsersList: ExternalTasks) :
            Msg

        data class LanguageIsChanged(val language: String) : Msg

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

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeUserUseCase().collect {
                    dispatch(Action.UserIsChanged(it))
                }
            }

            scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }

            scope.launch {
                observeUsersListUseCase().collect {
                    dispatch(Action.UsersListIsChanged(it))
                }
            }

//            scope.launch {
//                observeExternalTasksUseCase().collect {
//                    dispatch(Action.ExternalTasksListIsChanged(it))
//                }
//            }
//
//            scope.launch {
//                observePrivateTasksUseCase().collect {
//                    dispatch(Action.PrivateTasksListIsChanged(it))
//                }
//            }
//
//            scope.launch {
//                observeListToShopUseCase().collect {
//                    dispatch(Action.ShopListIsChanged(it))
//                }
//            }
//
//            scope.launch {
//                observeMyTasksForOtherUsersUseCase().collect {
//                    dispatch(Action.MyTasksForOtherUsersListIsChanged(it))
//                }
//            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {

                Intent.ClickedCreateNewUser -> {
                    dispatch(Msg.ButtonCreateNewUserClicked)
                }

                is Intent.ClickedRegisterNewUserInFirebase -> {
                    scope.launch {
                        createNewUserUseCase(
                            User(
                                nickName = getState().nickName,
                                displayName = getState().displayName,
                                password = getState().password,
                                adminEmailOrPhone = getState().user.adminEmailOrPhone,

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


                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

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
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {

                is Action.UserIsChanged -> {
                    if (action.user.nickName.length >= appContext.resources.getInteger(R.integer.min_nickName_length)
                        && (action.user.nickName != NO_USER_SAVED_IN_SHARED_PREFERENCES)
                    ) {
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

                is Msg.UserIsChanged -> {
                    copy(user = msg.user)
                }

                is Msg.UsersListIsChanged -> {
                    copy(usersList = msg.usersList)
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
