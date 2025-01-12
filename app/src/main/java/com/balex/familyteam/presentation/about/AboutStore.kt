package com.balex.familyteam.presentation.about

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.about.AboutStore.Intent
import com.balex.familyteam.presentation.about.AboutStore.Label
import com.balex.familyteam.presentation.about.AboutStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AboutStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

        data object ClickedRules : Intent

    }

    data class State(
        val language: String
    )

    sealed interface Label {
        data object ClickedRules : Label
    }
}

class AboutStoreFactory @Inject constructor(
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val storeFactory: StoreFactory
) {

    fun create(language: String): AboutStore =
        object : AboutStore, Store<Intent, State, Label> by storeFactory.create(
            name = "AboutStore",
            initialState = State(
                language = language
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class LanguageIsChanged(val language: String) : Action
    }

    private sealed interface Msg {
        data class LanguageIsChanged(val language: String) : Msg
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
                Intent.RefreshLanguage -> {
                    dispatch(Msg.LanguageIsChanged(getState().language))
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

                Intent.ClickedRules -> {
                    publish(Label.ClickedRules)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.LanguageIsChanged -> {
                    dispatch(Msg.LanguageIsChanged(action.language))
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
            }
    }
}
