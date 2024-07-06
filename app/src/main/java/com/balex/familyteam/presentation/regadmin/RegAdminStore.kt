package com.balex.familyteam.presentation.regadmin

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.Admin
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.domain.usecase.regLog.AddAdminUseCase
import com.balex.familyteam.domain.usecase.regLog.ObserveLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.RegisterAndVerifyByEmailUseCase
import com.balex.familyteam.domain.usecase.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Intent
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Label
import com.balex.familyteam.presentation.regadmin.RegAdminStore.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

interface RegAdminStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickedBack : Intent

        data object ClickedRegister : Intent

        data object ClickedEmailOrPhoneButton : Intent

        data object ClickedChangePasswordVisibility : Intent

        data object ClickedTryAgain : Intent

        data class LoginFieldChanged(val currentLoginText: String) : Intent

        data class PasswordFieldChanged(val currentPasswordText: String) : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

    }

    data class State(
        val language: String,
        val selectedOption: RegistrationOption,
        val emailOrPhone: String,
        val password: String,
        val isPasswordEnabled: Boolean,
        val passwordVisible: Boolean,
        val isRegisterButtonEnabled: Boolean,
        val isRegisterButtonWasPressed: Boolean,
        val isRegError: Boolean,
        val regAdminState: RegAdminState
    ) {
        sealed interface RegAdminState {

            data object Content : RegAdminState

        }
    }

    sealed interface Label {

        data object AdminIsRegisteredAndVerified : RegAdminStore.Label

        data object ClickedBack : RegAdminStore.Label

    }
}

class RegAdminStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val addAdminUseCase: AddAdminUseCase,
    private val registerAndVerifyByEmailUseCase: RegisterAndVerifyByEmailUseCase,
    private val context: Context
) {

    fun create(language: String): RegAdminStore =
        object : RegAdminStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RegAdminStore",
            initialState = State(
                language,
                RegistrationOption.EMAIL,
                "",
                "",
                isPasswordEnabled = false,
                passwordVisible = false,
                isRegisterButtonWasPressed = false,
                isRegisterButtonEnabled = false,
                isRegError = false,
                regAdminState = State.RegAdminState.Content
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class LanguageIsChanged(val language: String) : Action

        data class LanguageIsCheckedInPreference(val language: String) : Action

    }

    private sealed interface Msg {

        data object SuccessRegister : Msg

        data object ErrorRegister : Msg

        data object ChangeEmailOrPhoneButton : Msg

        data object ChangePasswordVisibility : Msg

        data object ProcessTryAgain : Msg

        data object EmailOrPhoneMatched : Msg

        data object EmailOrPhoneNotMatched : Msg

        data object PasswordMatched : Msg

        data object PasswordNotMatched : Msg

        data class UpdateLoginFieldText(val currentLoginText: String) : Msg

        data class UpdatePasswordFieldText(val currentPasswordText: String) : Msg

        data class UserLanguageChanged(val language: String) : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
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
                    val admin = Admin(
                        isEmailRegistration = getState().selectedOption == RegistrationOption.EMAIL,
                        emailOrPhoneNumber = getState().emailOrPhone,
                        isEmailOrPhoneNumberConfirmed = false
                    )
                    CoroutineScope(Dispatchers.Main).launch {
//                        val resultFirebase = addAdminUseCase(admin)
//                        if (resultFirebase.isSuccess) {
//                            dispatch(Msg.SuccessRegister)
//                        } else {
//                            dispatch(Msg.ErrorRegister)
//                        }
                        registerAndVerifyByEmailUseCase(getState().emailOrPhone, getState().password)
                    }

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
                        val text = intent.currentLoginText.replace(Regex(REGEX_PATTERN_NUMBERS), "")
                        dispatch(Msg.UpdateLoginFieldText(text))
                        if (text.length >= context.resources.getInteger(R.integer.min_numbers_in_phone)) {
                            dispatch(Msg.EmailOrPhoneMatched)
                        } else {
                            dispatch(Msg.EmailOrPhoneNotMatched)
                        }

                    }
                }

                is Intent.PasswordFieldChanged -> {
                    dispatch(Msg.UpdatePasswordFieldText(intent.currentPasswordText))
                    if (intent.currentPasswordText.length >= context.resources.getInteger(R.integer.min_password_length)) {
                        dispatch(Msg.PasswordMatched)
                    } else {
                        dispatch(Msg.PasswordNotMatched)
                    }
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.UserLanguageChanged(intent.language))
                }

            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
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

            Msg.ChangeEmailOrPhoneButton -> {
                copy(
                    emailOrPhone = "",
                    selectedOption = if (selectedOption == RegistrationOption.EMAIL) RegistrationOption.PHONE else RegistrationOption.EMAIL,
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
                    password = "",
                    isPasswordEnabled = false,
                    passwordVisible = false,
                    isRegisterButtonWasPressed = false,
                    isRegisterButtonEnabled = false,
                    isRegError = false
                )

            }

            is Msg.UpdateLoginFieldText -> {
                copy(emailOrPhone = msg.currentLoginText)
            }

            is Msg.UpdatePasswordFieldText -> {
                copy(password = msg.currentPasswordText)
            }

            is Msg.UserLanguageChanged -> {
                copy(language = msg.language)
            }

            Msg.SuccessRegister -> {
                copy(
                    isRegisterButtonWasPressed = true,
                    isRegError = false
                )
            }

            Msg.ErrorRegister -> {
                copy(
                    isRegisterButtonWasPressed = true,
                    isRegError = true
                )
            }

            Msg.EmailOrPhoneMatched -> {
                copy(isPasswordEnabled = true)
            }

            Msg.EmailOrPhoneNotMatched -> {
                copy(isPasswordEnabled = false)
            }

            Msg.PasswordMatched -> {
                copy(isRegisterButtonEnabled = true)
            }

            Msg.PasswordNotMatched -> {
                copy(isRegisterButtonEnabled = false)
            }

        }
    }

    companion object {
        const val REGEX_PATTERN_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$"
        const val REGEX_PATTERN_NUMBERS = "[^\\d]"
    }
}
