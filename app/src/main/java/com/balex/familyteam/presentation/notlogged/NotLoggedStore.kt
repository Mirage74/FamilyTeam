package com.balex.familyteam.presentation.notlogged

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Intent
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.Label
import com.balex.familyteam.presentation.notlogged.NotLoggedStore.State
import javax.inject.Inject

internal interface NotLoggedStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickedLoginAdmin: Intent

        data object ClickedRegisterAdmin: Intent

        data object ClickedLoginUser: Intent

    }

    data class State(
        val logChooseState: LogChooseState
    ) {
        sealed interface LogChooseState {

            data object Initial: LogChooseState

            data object LoginAdmin: LogChooseState

            data object RegisterAdmin: LogChooseState

            data object LoginUser: LogChooseState

        }
    }

    sealed interface Label {

        data object ClickedLoginAdmin: Label

        data object ClickedRegisterAdmin: Label

        data object ClickedLoginUser: Label

    }
}

internal class NotLoggedStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory
) {

    fun create(): NotLoggedStore =
        object : NotLoggedStore, Store<Intent, State, Label> by storeFactory.create(
            name = "NotLoggedStore",
            initialState = State.LogChooseState.Initial,
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoginR

    }

    private sealed interface Msg {
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
        }

        override fun executeAction(action: Action, getState: () -> State) {
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            }
    }
}
