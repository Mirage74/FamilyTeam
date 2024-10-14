package com.balex.familyteam.presentation.regadmin

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.R
import com.balex.common.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.common.domain.entity.RegistrationOption
import com.balex.common.domain.entity.User
import com.balex.common.extensions.*
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.IsWrongPasswordUseCase
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.ObserveUserUseCase
import com.balex.common.domain.usecases.regLog.RegisterAndVerifyByEmailUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Intent
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Label
import com.balex.familyteam.presentation.regadmin.RegAdminStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface RegAdminStore : Store<Intent, State, Label> {

    fun stopBootstrapperCollectFlow()

    sealed interface Intent {

        data object ClickedBack : Intent

        data object ClickedRegister : Intent

        data object ClickedEmailOrPhoneButton : Intent

        data object ClickedChangePasswordVisibility : Intent

        data object ClickedTryAgain : Intent

        data class LoginFieldChanged(val currentLoginText: String) : Intent

        data class NickNameFieldChanged(val currentNickNameText: String) : Intent

        data class DisplayNameFieldChanged(val currentDisplayNameText: String) : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data class SmsNumberFieldChanged(val currentSmsNumberText: String) : Intent

        data object ClickedAbout : Intent

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

    }

    data class State(
        val language: String,
        val selectedOption: RegistrationOption,
        val emailOrPhone: String,
        val nickName: String,
        val displayName: String,
        val password: String,
        val smsCode: String,
        val isNickNameEnabled: Boolean,
        val isPasswordEnabled: Boolean,
        val passwordVisible: Boolean,
        val isRegisterButtonEnabled: Boolean,
        val isRegisterButtonWasPressed: Boolean,
        val isSmsVerifyButtonWasPressed: Boolean,
        val isSmsOkButtonEnabled: Boolean,
        val isEmailOrPhoneNumberVerified: Boolean,
        val errorMessage: String,
        val regAdminState: RegAdminState
    ) {
        sealed interface RegAdminState {

            data object Content : RegAdminState

            data object Loading : RegAdminState

            data object Error : RegAdminState

        }
    }

    sealed interface Label {

        data object ClickedAbout : Label

        data object AdminIsRegisteredAndVerified : Label

        data object ClickedBack : Label

        data class LoginPageWrongPassword(val user: User) : Label

    }
}

class RegAdminStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val observeIsWrongPasswordUseCase: IsWrongPasswordUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeUserUseCase: ObserveUserUseCase,
    private val registerAndVerifyByEmailUseCase: RegisterAndVerifyByEmailUseCase,
    context: Context
) {
    val appContext: Context = context.applicationContext

    fun create(language: String): RegAdminStore =
        object : RegAdminStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RegAdminStore",
            initialState = State(
                language,
                RegistrationOption.EMAIL,
                "",
                "",
                "",
                "",
                "",
                isNickNameEnabled = false,
                isPasswordEnabled = false,
                passwordVisible = false,
                isRegisterButtonWasPressed = false,
                isSmsVerifyButtonWasPressed = false,
                isSmsOkButtonEnabled = false,
                isRegisterButtonEnabled = false,
                isEmailOrPhoneNumberVerified = false,
                errorMessage = User.NO_ERROR_MESSAGE,
                regAdminState = State.RegAdminState.Loading
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

        data class LanguageIsChanged(val language: String) : Action

        data class UserIsChanged(val user: User) : Action

        data class AdminExistWrongPassword(val user: User) : Action

    }

    private sealed interface Msg {

        data class UserInfoIsChanged(val user: User) : Msg

        data object ClickedRegister : Msg

        data object ClickedSmsCodeConfirmation : Msg

        data object ChangeEmailOrPhoneButton : Msg

        data object ChangePasswordVisibility : Msg

        data object ProcessTryAgain : Msg

        data object EmailOrPhoneMatched : Msg

        data object EmailOrPhoneNotMatched : Msg

        data object NickNameMatched : Msg

        data object NickNameNotMatched : Msg

        data object SmsMatched : Msg

        data object SmsMatchedNotMatched : Msg

        data object PasswordMatched : Msg

        data object PasswordNotMatched : Msg

        data class UpdateLoginFieldText(val currentLoginText: String) : Msg

        data class UpdateNickNameFieldText(val currentNickNameText: String) : Msg

        data class UpdateDisplayNameFieldText(val currentDisplayNameText: String) : Msg

        data class UpdatePasswordFieldText(val currentPasswordText: String) : Msg

        data class UpdateSmsNumberFieldText(val currentSmsNumberText: String) : Msg

        data class LanguageIsChanged(val language: String) : Msg

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
                    if (it.adminEmailOrPhone.isNotEmpty()) {
                        dispatch(Action.AdminExistWrongPassword(it))
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
                Intent.ClickedBack -> {
                    publish(Label.ClickedBack)
                }

                Intent.ClickedChangePasswordVisibility -> {
                    dispatch(Msg.ChangePasswordVisibility)
                }

                Intent.ClickedEmailOrPhoneButton -> {
                    dispatch(Msg.ChangeEmailOrPhoneButton)
                }

                Intent.ClickedRegister -> {
                    scope.launch {
                        if (getState().selectedOption == RegistrationOption.EMAIL) {
                            registerAndVerifyByEmailUseCase(
                                getState().emailOrPhone,
                                getState().nickName,
                                getState().displayName,
                                getState().password
                            )
                        }
                    }
                    dispatch(Msg.ClickedRegister)
                }

                Intent.ClickedTryAgain -> {
                    dispatch(Msg.ProcessTryAgain)
                }

                is Intent.LoginFieldChanged -> {

                    if (getState().selectedOption == RegistrationOption.EMAIL) {
                        dispatch(Msg.UpdateLoginFieldText(intent.currentLoginText))
                        if (Regex(REGEX_PATTERN_EMAIL)
                                .matches(intent.currentLoginText)
                        ) {
                            dispatch(Msg.EmailOrPhoneMatched)
                        } else {
                            dispatch(Msg.EmailOrPhoneNotMatched)
                        }
                    } else {
                        val text =
                            intent.currentLoginText.replace(Regex(REGEX_PATTERN_NOT_NUMBERS), "")
                        dispatch(Msg.UpdateLoginFieldText(text))
                        if (text.length >= appContext.resources.getInteger(R.integer.min_numbers_in_phone)) {
                            dispatch(Msg.EmailOrPhoneMatched)
                        } else {
                            dispatch(Msg.EmailOrPhoneNotMatched)
                        }

                    }
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

                is Intent.SmsNumberFieldChanged -> {
                    val text =
                        intent.currentSmsNumberText.replace(Regex(REGEX_PATTERN_NOT_NUMBERS), "")
                    dispatch(Msg.UpdateSmsNumberFieldText(text))
                    if (text.length == appContext.resources.getInteger(R.integer.sms_code_length)) {
                        dispatch(Msg.SmsMatched)
                    } else {
                        dispatch(Msg.SmsMatchedNotMatched)
                    }
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
                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
                }

                is Action.UserIsChanged -> {
                    if (action.user.nickName.length >= appContext.resources.getInteger(R.integer.min_nickName_length)
                        && (action.user.nickName != NO_USER_SAVED_IN_SHARED_PREFERENCES)
                        && (action.user.nickName != User.DEFAULT_NICK_NAME)
                    ) {
                        publish(Label.AdminIsRegisteredAndVerified)
                    }
                    dispatch(Msg.UserInfoIsChanged(action.user))
                }

                is Action.AdminExistWrongPassword -> {
                    if (getState().isRegisterButtonWasPressed) {
                        publish(Label.LoginPageWrongPassword(action.user))
                    }

                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {

            Msg.ChangeEmailOrPhoneButton -> {
                copy(
                    emailOrPhone = "",
                    nickName = "",
                    displayName = "",
                    password = "",
                    smsCode = "",
                    selectedOption = if (selectedOption == RegistrationOption.EMAIL) RegistrationOption.PHONE else RegistrationOption.EMAIL,
                    isNickNameEnabled = false,
                    isPasswordEnabled = false,
                    isRegisterButtonEnabled = false
                )
            }

            Msg.ChangePasswordVisibility -> {
                copy(passwordVisible = !passwordVisible)
            }

            Msg.ProcessTryAgain -> {
                copy(
                    selectedOption = RegistrationOption.EMAIL,
                    emailOrPhone = "",
                    nickName = "",
                    displayName = "",
                    password = "",
                    smsCode = "",
                    isNickNameEnabled = false,
                    isPasswordEnabled = false,
                    passwordVisible = false,
                    isRegisterButtonWasPressed = false,
                    isRegisterButtonEnabled = false,
                    isEmailOrPhoneNumberVerified = false,
                    errorMessage = User.NO_ERROR_MESSAGE,
                    regAdminState = State.RegAdminState.Content
                )

            }

            is Msg.UpdateLoginFieldText -> {
                copy(emailOrPhone = msg.currentLoginText)
            }

            is Msg.UpdateNickNameFieldText -> {
                copy(nickName = msg.currentNickNameText)
            }

            is Msg.UpdateDisplayNameFieldText -> {
                copy(displayName = msg.currentDisplayNameText)
            }

            is Msg.UpdatePasswordFieldText -> {
                copy(password = msg.currentPasswordText)
            }

            is Msg.UpdateSmsNumberFieldText -> {
                copy(smsCode = msg.currentSmsNumberText)
            }

            is Msg.LanguageIsChanged -> {
                copy(language = msg.language)
            }

            Msg.ClickedRegister -> {
                copy(
                    isRegisterButtonWasPressed = true
                )
            }

            Msg.EmailOrPhoneMatched -> {
                copy(isNickNameEnabled = true)
            }

            Msg.EmailOrPhoneNotMatched -> {
                copy(isNickNameEnabled = false)
            }

            Msg.NickNameMatched -> {
                copy(isPasswordEnabled = true)
            }

            Msg.NickNameNotMatched -> {
                copy(isPasswordEnabled = false)
            }

            Msg.PasswordMatched -> {
                copy(isRegisterButtonEnabled = true)
            }

            Msg.PasswordNotMatched -> {
                copy(isRegisterButtonEnabled = false)
            }

            Msg.ClickedSmsCodeConfirmation -> {
                copy(isSmsVerifyButtonWasPressed = true)
            }

            Msg.SmsMatched -> {
                copy(isSmsOkButtonEnabled = true)
            }

            Msg.SmsMatchedNotMatched -> {
                copy(isSmsOkButtonEnabled = false)
            }

            is Msg.UserInfoIsChanged -> {
                if (msg.user.existErrorInData) {
                    copy(
                        errorMessage = msg.user.errorMessage,
                        regAdminState = State.RegAdminState.Error)
                } else {
                    copy(regAdminState = State.RegAdminState.Content)
                }

            }
        }
    }

}
