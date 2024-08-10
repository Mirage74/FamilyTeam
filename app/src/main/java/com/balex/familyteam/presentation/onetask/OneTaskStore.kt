package com.balex.familyteam.presentation.onetask

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.presentation.onetask.OneTaskStore.Intent
import com.balex.familyteam.presentation.onetask.OneTaskStore.Label
import com.balex.familyteam.presentation.onetask.OneTaskStore.State
import javax.inject.Inject

interface OneTaskStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    data class State(val todo: Unit)

    sealed interface Label {
    }
}

class OneTaskStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory
) {

    fun create(): OneTaskStore =
        object : OneTaskStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OneTaskStore",
            initialState = State(Unit),
            bootstrapper = BootstrapperImpl(),
            executorFactory = OneTaskStoreFactory::ExecutorImpl,
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
        override fun State.reduce(message: Msg): State = State(Unit)
    }
}
