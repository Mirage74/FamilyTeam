package com.balex.familyteam.presentation.notlogged

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.common.data.repository.RegLogRepositoryImpl
import com.balex.common.domain.entity.User
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.regLog.GetWrongPasswordUserUseCase
import com.balex.common.domain.usecases.regLog.IsWrongPasswordUseCase
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.ObserveUserUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.common.domain.usecases.regLog.SignToFirebaseWithFakeEmailUseCase
import com.balex.common.domain.usecases.regLog.StorageClearPreferencesUseCase
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Intent
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Label
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface NotLoggedStore : Store<Intent, State, Label> {

    fun stopBootstrapperCollectFlow()

    sealed interface Intent {

        data object ClickedRegisterAdmin : Intent

        data object ClickedLoginUser : Intent

        data object ClickedAbout : Intent

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

    }

    data class State(
        val language: String,
        val errorMessage: String,
        val logChooseState: LogChooseState
    ) {
        sealed interface LogChooseState {

            data object Initial : LogChooseState

            data object NoSavedUserFound : LogChooseState

            data object ErrorLoadingUserData : LogChooseState

        }
    }

    sealed interface Label {

        data object UserIsLogged : Label

        data object ClickedRegisterAdmin : Label

        data object ClickedLoginUser : Label

        data object ClickedAbout : Label

        data class LoginPageWrongPassword(val user: User) : Label

    }
}

class NotLoggedStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val observeUserUseCase: ObserveUserUseCase,
    private val observeIsWrongPasswordUseCase: IsWrongPasswordUseCase,
    private val storageClearPreferencesUseCase: StorageClearPreferencesUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getWrongPasswordUserUseCase: GetWrongPasswordUserUseCase,
    private val signToFirebaseWithFakeEmailUseCase: SignToFirebaseWithFakeEmailUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val getLanguageUseCase: GetLanguageUseCase
) {

    fun create(language: String): NotLoggedStore =
        object : NotLoggedStore, Store<Intent, State, Label> by storeFactory.create(
            name = "NotLoggedStore",
            initialState = State(language, User.NO_ERROR_MESSAGE, State.LogChooseState.Initial),
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

        data object UserNotExistInPreference : Action

        data object UserExistInPreferenceAndLoadedUserData : Action

        data object UserExistInPreferenceButErrorLoadingUserData : Action

        data class AdminAndUserExistButWrongPassword(val user: User) : Action

        data class OtherUserError(val errorMessage: String) : Action

        data class LanguageIsChanged(val language: String) : Action

    }

    private sealed interface Msg {

        data class RefreshLanguage(val language: String) : Msg

        data object UserIsNotExistInPreference : Msg

        data object UserExistInPreferenceButErrorLoadingUserData : Msg

        data class OtherUserError(val errorMessage: String) : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        private var userJob: Job? = null
        private var passwordJob: Job? = null
        private var languageJob: Job? = null

        override fun invoke() {
            start()
        }

        fun stop() {
            userJob?.cancel()
            passwordJob?.cancel()
            languageJob?.cancel()
        }

        fun start() {
            userJob = scope.launch {
                observeUserUseCase().collect {
                    if (it.existErrorInData) {
                        if (it.errorMessage == RegLogRepositoryImpl.ERROR_LOADING_USER_DATA_FROM_FIREBASE) {
                            dispatch(Action.UserExistInPreferenceButErrorLoadingUserData)
                        } else {
                            dispatch(Action.OtherUserError(it.errorMessage))
                        }
                    } else {
                        val login = it.nickName
                        if (login.isNotEmpty()) {
                            when (login) {
                                NO_USER_SAVED_IN_SHARED_PREFERENCES -> {
                                    dispatch(Action.UserNotExistInPreference)
                                }

//                                User.DEFAULT_NICK_NAME -> {
//                                    dispatch(Action.UserNotExistInPreference)
//                                }

                                else -> {
                                    if (getWrongPasswordUserUseCase().nickName == User.DEFAULT_NICK_NAME
                                        && login != User.DEFAULT_NICK_NAME
                                    ) {
                                        signToFirebaseWithFakeEmailUseCase(getUserUseCase())
                                        dispatch(Action.UserExistInPreferenceAndLoadedUserData)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            passwordJob = scope.launch {
                observeIsWrongPasswordUseCase().collect {
                    if (it.nickName != User.DEFAULT_NICK_NAME) {
                        dispatch(Action.AdminAndUserExistButWrongPassword(it))
                    }
                }
            }

            languageJob = scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }

        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {

                Intent.ClickedRegisterAdmin -> {
                    publish(Label.ClickedRegisterAdmin)
                }

                Intent.ClickedLoginUser -> {
                    publish(Label.ClickedLoginUser)
                }

                Intent.ClickedAbout -> {
                    publish(Label.ClickedAbout)
                }

                Intent.RefreshLanguage -> {
                    dispatch(Msg.RefreshLanguage(getLanguageUseCase()))
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.RefreshLanguage(intent.language))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {

                Action.UserExistInPreferenceAndLoadedUserData -> {
                    publish(Label.UserIsLogged)
                }

                Action.UserExistInPreferenceButErrorLoadingUserData -> {
                    storageClearPreferencesUseCase()
                    dispatch(Msg.UserExistInPreferenceButErrorLoadingUserData)
                }

                Action.UserNotExistInPreference -> {
                    dispatch(Msg.UserIsNotExistInPreference)
                }

                is Action.AdminAndUserExistButWrongPassword -> {
                    publish(Label.LoginPageWrongPassword(action.user))
                }

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.RefreshLanguage(action.language))
                }

                is Action.OtherUserError -> {
                    dispatch(Msg.OtherUserError(action.errorMessage))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {

            Msg.UserIsNotExistInPreference -> {
                copy(logChooseState = State.LogChooseState.NoSavedUserFound)
            }

            Msg.UserExistInPreferenceButErrorLoadingUserData -> {
                copy(
                    errorMessage = msg.toString(),
                    logChooseState = State.LogChooseState.ErrorLoadingUserData
                )
            }

            is Msg.RefreshLanguage -> {
                copy(language = msg.language)
            }

            is Msg.OtherUserError -> {

                copy(
                    errorMessage = msg.toString(),
                    logChooseState = State.LogChooseState.ErrorLoadingUserData
                )
            }
        }
    }
}
