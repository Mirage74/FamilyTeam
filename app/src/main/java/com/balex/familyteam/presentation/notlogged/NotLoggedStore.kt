package com.balex.familyteam.presentation.notlogged

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.domain.usecase.RegLog.ObserveUserUseCase
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

    }

    data class State(
        val logChooseState: LogChooseState
    ) {
        sealed interface LogChooseState {

            data object Initial : LogChooseState

            data object SavedUserLoading : LogChooseState

        }
    }

    sealed interface Label {

        data object UserIsLogged : Label

        data object ClickedLoginAdmin : Label

        data object ClickedRegisterAdmin : Label

        data object ClickedLoginUser : Label

    }
}

class NotLoggedStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val observeUserUseCase: ObserveUserUseCase
) {

    fun create(): NotLoggedStore =
        object : NotLoggedStore, Store<Intent, State, Label> by storeFactory.create(
            name = "NotLoggedStore",
            initialState = State(State.LogChooseState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data object UserIsLogged : Action

    }

    private sealed interface Msg {

        data object UserIsLogged : Msg

        data object RegAsAdmin : Msg

        data object LogAsAdmin : Msg

        data object LogAsUser : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeUserUseCase().collect {
                    if (it.login.isNotEmpty()) {
                        dispatch(Action.UserIsLogged)
                    }
                }
            }

        }
    }

    private class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
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

            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                Action.UserIsLogged -> {
                    publish(Label.UserIsLogged)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            Msg.LogAsAdmin -> TODO()
            Msg.LogAsUser -> TODO()
            Msg.RegAsAdmin -> TODO()
            Msg.UserIsLogged -> TODO()
        }
    }
}
