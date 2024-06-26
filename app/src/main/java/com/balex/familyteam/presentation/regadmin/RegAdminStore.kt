package com.balex.familyteam.presentation.regadmin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Intent
import com.balex.familyteam.presentation.regadmin.RegAdminStore.Label
import com.balex.familyteam.presentation.regadmin.RegAdminStore.State
import javax.inject.Inject

interface RegAdminStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    data class State(
        val regAdminState: RegAdminState
    ) {
        sealed interface RegAdminState {

            data object Initial : RegAdminState


        }
    }

    sealed interface Label {
    }
}

class RegAdminStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory
) {

    fun create(): RegAdminStore =
        object : RegAdminStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RegAdminStore",
            initialState = State(State.RegAdminState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
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
        override fun State.reduce(msg: Msg): State = State(State.RegAdminState.Initial)
    }
}
