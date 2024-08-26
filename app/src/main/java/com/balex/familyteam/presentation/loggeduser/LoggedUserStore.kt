package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.familyteam.domain.entity.ToDoList
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.Intent
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.Label
import com.balex.familyteam.presentation.loggeduser.LoggedUserStore.State
import javax.inject.Inject

interface LoggedUserStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ChangeLanguage(val language: String) : Intent

        data class ChangePage(val page: PagesNames) : Intent

    }

    data class State(
        val language: String,
        val activeBottomItem: PagesNames,
        val todoList: ToDoList,
        val loggedUserState: LoggedUserState
    ) {
        sealed interface LoggedUserState {

            data object Initial : LoggedUserState

            data object Loading : LoggedUserState

            data object Content : LoggedUserState
        }
    }

    sealed interface Label {
    }
}

class LoggedUserStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory
) {

    fun create(language: String): LoggedUserStore =
        object : LoggedUserStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoggedUserStore",
            initialState = State(language, PagesNames.TodoList, ToDoList(), State.LoggedUserState.Initial),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class LanguageIsChanged(val language: String) : Action

        data class PageIsChanged(val page: PagesNames) : Action

    }

    private sealed interface Msg {

        data class LanguageIsChanged(val language: String) : Msg

        data class PageIsChanged(val page: PagesNames) : Msg

    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.ChangeLanguage -> {
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }
                is Intent.ChangePage -> {
                    dispatch(Msg.PageIsChanged(intent.page))
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {

                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
                }

                is Action.PageIsChanged -> {
                    dispatch(Msg.PageIsChanged(action.page))
                }

            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {

                is Msg.LanguageIsChanged -> {
                    copy(language = msg.language)
                }

                is Msg.PageIsChanged -> {
                    copy(activeBottomItem = msg.page)
                }

            }
    }
}
