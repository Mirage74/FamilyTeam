package com.balex.familyteam.presentation.notlogged

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveUserUseCase
import com.balex.familyteam.domain.usecase.regLog.SaveLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.SignInWithEmailAndPasswordUseCase
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Intent
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Label
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface NotLoggedStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickedLoginAdmin : Intent

        data object ClickedRegisterAdmin : Intent

        data object ClickedLoginUser : Intent

        data object ClickedAbout: Intent

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

    }

    data class State(
        val language: String,
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

        data object ClickedLoginAdmin : Label

        data object ClickedRegisterAdmin : Label

        data object ClickedLoginUser : Label

        data object ClickedAbout : Label

    }
}

class NotLoggedStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val observeUserUseCase: ObserveUserUseCase,
    private val signInWithEmailAndPasswordUseCase: SignInWithEmailAndPasswordUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val getLanguageUseCase: GetLanguageUseCase
) {

    fun create(language: String): NotLoggedStore =
        object : NotLoggedStore, Store<Intent, State, Label> by storeFactory.create(
            name = "NotLoggedStore",
            initialState = State(language, State.LogChooseState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class LanguageIsChanged(val language: String) : Action

        data class LanguageIsCheckedInPreference (val language: String): Action

        data object UserNotExistInPreference : Action

        data object UserExistInPreferenceAndLoadedUserData: Action

        data object UserExistInPreferenceButErrorLoadingUserData : Action


    }

    private sealed interface Msg {

        data class RefreshLanguage(val language: String) : Msg

        data object UserIsNotExistInPreference : Msg

        data object UserExistInPreferenceButErrorLoadingUserData : Msg

        data class UserLanguageChanged(val language: String) : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeUserUseCase().collect {
                    val login = it.nickName
                    if (login.isNotEmpty()) {
                        when (login) {
                            NO_USER_SAVED_IN_SHARED_PREFERENCES -> {
                                dispatch(Action.UserNotExistInPreference)
                            }
                            User.ERROR_LOADING_USER_DATA_FROM_FIREBASE -> {
                                dispatch(Action.UserExistInPreferenceButErrorLoadingUserData)
                            }
                            else -> {
                                signInWithEmailAndPasswordUseCase()
                                dispatch(Action.UserExistInPreferenceAndLoadedUserData)
                            }
                        }

                    }
                }
            }

            scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }

        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {

                Intent.ClickedLoginAdmin -> {
                    publish(Label.ClickedLoginAdmin)
                }

                Intent.ClickedRegisterAdmin -> {
                    publish(Label.ClickedRegisterAdmin)
                }

                Intent.ClickedLoginUser -> {
                    publish(Label.ClickedLoginUser)
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.UserLanguageChanged(intent.language))
                }

                Intent.ClickedAbout -> {
                    publish(Label.ClickedAbout)
                }

                Intent.RefreshLanguage -> {
                    dispatch(Msg.RefreshLanguage(getLanguageUseCase()))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {

                Action.UserExistInPreferenceAndLoadedUserData -> {
                    publish(Label.UserIsLogged)
                }

                Action.UserExistInPreferenceButErrorLoadingUserData -> {
                    dispatch(Msg.UserExistInPreferenceButErrorLoadingUserData)
                }

                Action.UserNotExistInPreference -> {
                    dispatch(Msg.UserIsNotExistInPreference)
                }

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.UserLanguageChanged(action.language))
                }

                is Action.LanguageIsCheckedInPreference -> {
                    dispatch(Msg.UserLanguageChanged(action.language))
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
                copy(logChooseState = State.LogChooseState.ErrorLoadingUserData)
            }

            is Msg.UserLanguageChanged -> {
                copy(language = msg.language)
            }

            is Msg.RefreshLanguage -> {
                copy(language = msg.language)
            }
        }
    }
}
