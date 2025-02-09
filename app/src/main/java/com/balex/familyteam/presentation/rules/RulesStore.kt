package com.balex.familyteam.presentation.rules

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.rules.RulesStore.Intent
import com.balex.familyteam.presentation.rules.RulesStore.Label
import com.balex.familyteam.presentation.rules.RulesStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface RulesStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object RefreshLanguage : Intent

        data class ClickedChangeLanguage(val language: String) : Intent

        data object ClickedAbout : Intent

    }

    data class State(
        val language: String
    )

    sealed interface Label {
        data object ClickedAbout : Label
    }
}

class RulesStoreFactory @Inject constructor(
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val storeFactory: StoreFactory
) {

    fun create(language: String): RulesStore =
        object : RulesStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RulesStore",
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
        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.RefreshLanguage -> {
                    dispatch(Msg.LanguageIsChanged(state().language))
                }

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

                Intent.ClickedAbout -> {
                    publish(Label.ClickedAbout)
                }
            }
        }

        override fun executeAction(action: Action) {
            when (action) {
                is Action.LanguageIsChanged -> {
                    saveLanguageUseCase(action.language)
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
