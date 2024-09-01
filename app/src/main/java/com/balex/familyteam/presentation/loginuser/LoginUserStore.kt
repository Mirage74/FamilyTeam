package com.balex.familyteam.presentation.loginuser

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveUserUseCase
import com.balex.familyteam.domain.usecase.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.loginuser.LoginUserStore.Intent
import com.balex.familyteam.presentation.loginuser.LoginUserStore.Label
import com.balex.familyteam.presentation.loginuser.LoginUserStore.State
import com.balex.familyteam.presentation.regadmin.RegAdminStoreFactory.Companion.REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE
import com.balex.familyteam.presentation.regadmin.RegAdminStoreFactory.Companion.REGEX_PATTERN_NOT_LETTERS
import kotlinx.coroutines.launch
import javax.inject.Inject

interface LoginUserStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickedLoginButton : Intent

        data class LoginAdminFieldChanged(val currentAdminLoginText: String) : Intent

        data class NickNameFieldChanged(val currentNickNameFieldChanged: String) : Intent

        data object ClickedChangePasswordVisibility : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data object ClickedAbout : Intent

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent
    }

    data class State(

        val adminEmailOrPhone: String,

        val nickName: String,
        val isNickNameEnabled: Boolean,

        val password: String,
        val isPasswordEnabled: Boolean,
        val isPasswordVisible: Boolean,

        val isLoginButtonEnabled: Boolean,

        val language: String,
        val loginUserState: LoginUserState,

    ) {
        sealed interface LoginUserState {

            data object Initial : LoginUserState

            data object Loading : LoginUserState

            data object Error : LoginUserState

            data object Content : LoginUserState

        }
    }

    sealed interface Label {

        data object ClickedAbout : Label

        data object UserIsLogged : Label

    }
}

class LoginUserStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    context: Context
) {

    val appContext: Context = context.applicationContext

    fun create(language: String): LoginUserStore =
        object : LoginUserStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoginUserStore",
            initialState = State(
                adminEmailOrPhone = "",

                nickName = "",
                isNickNameEnabled = true,

                password = "",
                isPasswordEnabled = false,
                isPasswordVisible = false,

                isLoginButtonEnabled = false,

                language = language,
                loginUserState = State.LoginUserState.Content
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class UserIsChanged(val user: User) : Action

        data class LanguageIsChanged(val language: String) : Action

    }

    private sealed interface Msg {

        data object ClickedLoginButton : Msg


        data class UpdateLoginAdminField(val currentAdminLoginText: String) : Msg

        data object LoginAdminMatched : Msg

        data object LoginAdminNotMatched : Msg


        data class UpdateNickNameField(val currentNickNameFieldText: String) : Msg

        data object NickNameMatched : Msg

        data object NickNameNotMatched : Msg




        data class UpdatePasswordField(val currentPasswordText: String) : Msg

        data object PasswordMatched : Msg

        data object PasswordNotMatched : Msg

        data object ClickedChangePasswordVisibility : Msg


        data class LanguageIsChanged(val language: String) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }
            scope.launch {
                observeUserUseCase().collect {
                    dispatch(Action.UserIsChanged(it))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {

                is Intent.LoginAdminFieldChanged -> {
                    dispatch(Msg.UpdateLoginAdminField(intent.currentAdminLoginText))
                }

                is Intent.NickNameFieldChanged -> {

                    val text = if (intent.currentNickNameFieldChanged.length == 1) {
                        intent.currentNickNameFieldChanged.replace(Regex(REGEX_PATTERN_NOT_LETTERS), "")
                    } else {
                        intent.currentNickNameFieldChanged.replace(
                            Regex
                                (REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE), ""
                        )
                    }

                    dispatch(Msg.UpdateNickNameField(text))

                    if (text.length >= appContext.resources.getInteger(R.integer.min_nickName_length)) {
                        dispatch(Msg.NickNameMatched)
                    } else {
                        dispatch(Msg.NickNameNotMatched)
                    }



                    dispatch(Msg.UpdateNickNameField(intent.currentNickNameFieldChanged))
                }

                is Intent.PasswordFieldChanged -> {
                    dispatch(Msg.UpdatePasswordField(intent.currentPasswordText))
                    if (intent.currentPasswordText.length >= appContext.resources.getInteger(R.integer.min_password_length)) {
                        dispatch(Msg.PasswordMatched)
                    } else {
                        dispatch(Msg.PasswordNotMatched)
                    }
                }

                Intent.ClickedChangePasswordVisibility -> {
                    dispatch(Msg.ClickedChangePasswordVisibility)
                }

                Intent.ClickedLoginButton -> {
                    dispatch(Msg.ClickedLoginButton)
                    //publish(Label.UserIsLogged)
                }

                Intent.ClickedAbout -> {
                    publish(Label.ClickedAbout)
                }

                Intent.RefreshLanguage -> {
                    dispatch(Msg.LanguageIsChanged(getLanguageUseCase()))
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
                    if (action.user.nickName.length >= appContext.resources.getInteger(R.integer.min_nickName_length)) {
                        publish(Label.UserIsLogged)
                    }
                }

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                Msg.ClickedLoginButton -> {
                    copy(loginUserState = State.LoginUserState.Loading)
                }

                is Msg.UpdateLoginAdminField -> {
                    copy(adminEmailOrPhone = msg.currentAdminLoginText)
                }

                Msg.LoginAdminMatched -> {
                    copy(isNickNameEnabled = true)
                }

                Msg.LoginAdminNotMatched -> {
                    copy(isNickNameEnabled = false)
                }

                is Msg.UpdateNickNameField -> {
                    copy(nickName = msg.currentNickNameFieldText)

                }
                Msg.NickNameMatched -> {
                    copy(isPasswordEnabled = true)
                }

                Msg.NickNameNotMatched -> {
                    copy(isPasswordEnabled = false)
                }

                is Msg.UpdatePasswordField -> {
                    copy(password = msg.currentPasswordText)
                }

                Msg.PasswordMatched -> {
                    copy(isLoginButtonEnabled = true)

                }
                Msg.PasswordNotMatched -> {
                    copy(isLoginButtonEnabled = false)
                }

                Msg.ClickedChangePasswordVisibility -> {
                    copy(isPasswordVisible = !isPasswordVisible)
                }

                is Msg.LanguageIsChanged -> {
                    copy(language = msg.language)
                }

            }
    }
}
