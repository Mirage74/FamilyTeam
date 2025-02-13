package com.balex.familyteam.presentation.loginuser

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.R
import com.balex.common.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.CheckUserInCollectionAndLoginIfExistErrorMessages
import com.balex.common.domain.entity.User
import com.balex.common.domain.entity.User.Companion.NO_ERROR_MESSAGE
import com.balex.common.domain.usecases.regLog.CheckUserInCollectionAndLoginIfExistUseCase
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.IsWrongPasswordUseCase
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.ObserveUserUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.common.domain.usecases.regLog.StorageSavePreferencesUseCase
import com.balex.common.extensions.REGEX_PATTERN_EMAIL
import com.balex.common.extensions.REGEX_PATTERN_NOT_LATIN_LETTERS_NUMBERS_UNDERSCORE
import com.balex.common.extensions.REGEX_PATTERN_NOT_LETTERS
import com.balex.common.extensions.REGEX_PATTERN_ONLY_NUMBERS_FIRST_NOT_ZERO
import com.balex.common.extensions.formatStringFirstLetterUppercase
import com.balex.familyteam.presentation.loginuser.LoginUserStore.Intent
import com.balex.familyteam.presentation.loginuser.LoginUserStore.Label
import com.balex.familyteam.presentation.loginuser.LoginUserStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface LoginUserStore : Store<Intent, State, Label> {

    fun stopBootstrapperCollectFlow()

    sealed interface Intent {

        data object ClickedLoginButton : Intent

        data class LoginAdminFieldChanged(val currentAdminLoginText: String) : Intent

        data class NickNameFieldChanged(val currentNickNameFieldChanged: String) : Intent

        data object ClickedChangePasswordVisibility : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data class ClickedChangeLanguage(val language: String) : Intent
    }

    @Suppress("unused")
    data class State(

        val adminEmailOrPhone: String,

        val nickName: String,
        val isNickNameEnabled: Boolean,

        val password: String,
        val isPasswordEnabled: Boolean,
        val isPasswordVisible: Boolean,

        val isLoginButtonEnabled: Boolean,

        val language: String,

        val errorMessage: String,

        val loginUserState: LoginUserState



    ) {
        sealed interface LoginUserState {

            data object Loading : LoginUserState

            data object Error : LoginUserState

            data object Content : LoginUserState

        }
    }

    sealed interface Label {

        data object UserIsLogged : Label

    }
}

class LoginUserStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val observeIsWrongPasswordUseCase: IsWrongPasswordUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val checkUserInCollectionAndLoginIfExistUseCase: CheckUserInCollectionAndLoginIfExistUseCase,
    private val storageSavePreferencesUseCase: StorageSavePreferencesUseCase,
    context: Context
) {

    val appContext: Context = context.applicationContext

    @Suppress("unused")
    fun create(
        adminEmailOrPhoneDefault: String,
        language: String
    ): LoginUserStore =
        object : LoginUserStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoginUserStore",
            initialState = State(
                adminEmailOrPhone = adminEmailOrPhoneDefault,

                nickName = "",
                isNickNameEnabled = false,

                password = "",
                isPasswordEnabled = false,
                isPasswordVisible = false,

                isLoginButtonEnabled = false,

                language = language,
                errorMessage = NO_ERROR_MESSAGE,
                loginUserState = State.LoginUserState.Loading
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

        data class LanguageIsChanged(val language: String) : Action

        data class AdminAndUserExistButWrongPassword(val user: User) : Action

    }

    @Suppress("unused")
    private sealed interface Msg {

        data class ClickedLoginButton(val adminEmailOrPhone: String, val nickName: String) : Msg

        data object EmptyUser : Msg


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

        data class SetLoginWithoutWrongPassword(val user: User) : Msg

        data class ErrorOccurred(val errorMessage: String) : Msg

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
                    dispatch(Action.UserIsChanged(it))
                }
            }
            passwordJob = scope.launch {
                observeIsWrongPasswordUseCase().collect {
                    if (it.adminEmailOrPhone.isNotBlank()) {
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
        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.LoginAdminFieldChanged -> {
                    if (Regex(REGEX_PATTERN_EMAIL).matches(intent.currentAdminLoginText)) {
                        dispatch(Msg.LoginAdminMatched)
                    } else {
                        if ((Regex(REGEX_PATTERN_ONLY_NUMBERS_FIRST_NOT_ZERO)
                                .matches(intent.currentAdminLoginText.trim())) &&
                            (intent.currentAdminLoginText.length >= appContext.resources.getInteger(
                                R.integer.min_numbers_in_phone
                            ))
                        ) {
                            dispatch(Msg.LoginAdminMatched)
                        } else {
                            dispatch(Msg.LoginAdminNotMatched)
                        }
                    }
                    dispatch(Msg.UpdateLoginAdminField(intent.currentAdminLoginText))
                }

                is Intent.NickNameFieldChanged -> {

                    val text = if (intent.currentNickNameFieldChanged.length == 1) {
                        intent.currentNickNameFieldChanged.replace(
                            Regex(REGEX_PATTERN_NOT_LETTERS),
                            ""
                        )
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
                    scope.launch {
                        val adminEmailOrPhone = state().adminEmailOrPhone.trim()
                        val nickName = state().nickName.trim()
                        val password = state().password
                        val language = state().language
                        dispatch(Msg.ClickedLoginButton(adminEmailOrPhone, nickName))

                        val loggedUser = checkUserInCollectionAndLoginIfExistUseCase(
                            adminEmailOrPhone,
                            nickName.formatStringFirstLetterUppercase(),
                            password
                        )
                        if (!loggedUser.existErrorInData) {
                            storageSavePreferencesUseCase(adminEmailOrPhone, nickName, password, language)
                            publish(Label.UserIsLogged)
                        } else {
                            when (loggedUser.errorMessage) {
                                CheckUserInCollectionAndLoginIfExistErrorMessages.ADMIN_NOT_FOUND.name -> {
                                    dispatch(Msg.LoginAdminNotMatched)
                                }

                                CheckUserInCollectionAndLoginIfExistErrorMessages.NICK_NAME_NOT_FOUND.name -> {
                                    dispatch(Msg.NickNameNotMatched)
                                }

                                CheckUserInCollectionAndLoginIfExistErrorMessages.WRONG_PASSWORD.name -> {
                                    dispatch(Msg.PasswordNotMatched)
                                }

                                else -> {
                                    dispatch(Msg.ErrorOccurred(loggedUser.errorMessage))
                                }
                            }
                        }
                    }
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

            }
        }

        override fun executeAction(action: Action) {
            when (action) {

                is Action.UserIsChanged -> {
                    if (action.user.nickName.length >= appContext.resources.getInteger(R.integer.min_nickName_length)
                        && (action.user.nickName != NO_USER_SAVED_IN_SHARED_PREFERENCES)
                        && (action.user.nickName != User.DEFAULT_NICK_NAME)
                    ) {
                        if (action.user.password != User.WRONG_PASSWORD) {
                            storageSavePreferencesUseCase(action.user.adminEmailOrPhone, action.user.nickName, action.user.password, action.user.language)
                            publish(Label.UserIsLogged)
                        }
                    } else {
                        dispatch(Msg.EmptyUser)
                    }
                }

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
                }

                is Action.AdminAndUserExistButWrongPassword -> {
                    saveLanguageUseCase(action.user.language)
                    dispatch(Msg.SetLoginWithoutWrongPassword(action.user))
                }

            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.ClickedLoginButton -> {
                    copy(
                        adminEmailOrPhone = msg.adminEmailOrPhone,
                        nickName = msg.nickName,
                        loginUserState = State.LoginUserState.Loading
                    )
                }

                is Msg.UpdateLoginAdminField -> {
                    copy(adminEmailOrPhone = msg.currentAdminLoginText)
                }

                Msg.LoginAdminMatched -> {
                    copy(isNickNameEnabled = true)
                }

                Msg.LoginAdminNotMatched -> {
                    copy(
                        isNickNameEnabled = false,
                        loginUserState = State.LoginUserState.Content
                    )
                }

                is Msg.UpdateNickNameField -> {
                    copy(nickName = msg.currentNickNameFieldText)

                }

                Msg.NickNameMatched -> {
                    copy(isPasswordEnabled = true)
                }

                Msg.NickNameNotMatched -> {
                    copy(
                        isPasswordEnabled = false,
                        loginUserState = State.LoginUserState.Content
                    )
                }

                is Msg.UpdatePasswordField -> {
                    copy(password = msg.currentPasswordText)
                }

                Msg.PasswordMatched -> {
                    copy(isLoginButtonEnabled = true)

                }

                Msg.PasswordNotMatched -> {
                    copy(
                        isLoginButtonEnabled = false,
                        loginUserState = State.LoginUserState.Content
                    )
                }

                Msg.ClickedChangePasswordVisibility -> {
                    copy(isPasswordVisible = !isPasswordVisible)
                }

                is Msg.LanguageIsChanged -> {
                    copy(language = msg.language)
                }

                is Msg.SetLoginWithoutWrongPassword -> {
                    copy(
                        adminEmailOrPhone = msg.user.adminEmailOrPhone,
                        nickName = msg.user.nickName,
                        password = "",
                        isNickNameEnabled = true,
                        isPasswordEnabled = true,
                        isLoginButtonEnabled = false,
                        loginUserState = State.LoginUserState.Content
                    )
                }

                is Msg.ErrorOccurred -> {
                    copy(
                        errorMessage = msg.errorMessage,
                        loginUserState = State.LoginUserState.Error
                    )
                }

                Msg.EmptyUser -> {
                    copy(loginUserState = State.LoginUserState.Content)
                }
            }
    }
}

