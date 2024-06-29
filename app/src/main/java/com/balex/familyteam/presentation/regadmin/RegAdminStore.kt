package com.balex.familyteam.presentation.regadmin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.domain.entity.RegistrationOption
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Intent
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Label
import com.balex.familyteam.presentation.regadmin.RegAdminStore.State
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

    }

    data class State(
        val selectedOption: RegistrationOption,
        val emailOrPhone: String,
        val password: String,
        val passwordVisible: Boolean,
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
    private val storeFactory: StoreFactory
) {

    fun create(): RegAdminStore =
        object : RegAdminStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RegAdminStore",
            initialState = State(
                RegistrationOption.EMAIL,
                "",
                "",
                passwordVisible = false,
                isRegisterButtonWasPressed = false,
                isRegError = false,
                regAdminState = State.RegAdminState.Content
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

    }

    private sealed interface Msg {

        data object ProcessRegister : Msg

        data object ChangeEmailOrPhoneButton : Msg

        data object ChangePasswordVisibility : Msg

        data object ProcessTryAgain : Msg

        data class UpdateLoginFieldText(val currentLoginText: String) : Msg

        data class UpdatePasswordFieldText(val currentPasswordText: String) : Msg
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
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
                    dispatch(Msg.ProcessRegister)
                }
                Intent.ClickedTryAgain -> {
                    dispatch(Msg.ProcessTryAgain)
                }
                is Intent.LoginFieldChanged -> {
                    dispatch(Msg.UpdateLoginFieldText(intent.currentLoginText))
                }
                is Intent.PasswordFieldChanged -> {
                    dispatch(Msg.UpdatePasswordFieldText(intent.currentPasswordText))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =when(msg) {

            Msg.ChangeEmailOrPhoneButton -> {
                copy(
                    emailOrPhone = "",
                    selectedOption = if (selectedOption == RegistrationOption.EMAIL) RegistrationOption.PHONE else RegistrationOption.EMAIL
                )
            }

            Msg.ChangePasswordVisibility -> {
                copy(passwordVisible = !passwordVisible)
            }

            Msg.ProcessRegister -> {
                TODO()
            }

            Msg.ProcessTryAgain -> {
                copy(
                    selectedOption = RegistrationOption.EMAIL,
                    emailOrPhone = "",
                    password = "",
                    passwordVisible = false,
                    isRegisterButtonWasPressed = false,
                    isRegError = false
                )
            }

            is Msg.UpdateLoginFieldText -> {
                copy(emailOrPhone = msg.currentLoginText)
            }

            is Msg.UpdatePasswordFieldText -> {
                copy(password = msg.currentPasswordText)
            }

        }
    }
}
