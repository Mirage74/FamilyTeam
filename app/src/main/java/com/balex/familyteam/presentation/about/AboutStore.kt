package com.balex.familyteam.presentation.about

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.balex.common.domain.entity.User
import com.balex.common.domain.usecases.admin.DeleteSelfAccountUseCase
import com.balex.common.domain.usecases.admin.DeleteTeamUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.regLog.ObserveLanguageUseCase
import com.balex.common.domain.usecases.regLog.SaveLanguageUseCase
import com.balex.familyteam.presentation.about.AboutStore.Intent
import com.balex.familyteam.presentation.about.AboutStore.Label
import com.balex.familyteam.presentation.about.AboutStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AboutStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data class ClickedChangeLanguage(val language: String) : Intent

        data object ClickedRules : Intent

        data class DeleteAccount(val userName: String, val navigateToNotloggedScreen: () -> Unit) : Intent

        data class DeleteTeam(val navigateToNotloggedScreen: () -> Unit) : Intent

    }

    @Suppress("unused")
    data class State(
        val language: String,
        val user: User,
    )

    sealed interface Label {
        data object ClickedRules : Label
    }
}

class AboutStoreFactory @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val deleteSelfAccountUseCase: DeleteSelfAccountUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val storeFactory: StoreFactory
) {

    @Suppress("unused")
    fun create(language: String): AboutStore =
        object : AboutStore, Store<Intent, State, Label> by storeFactory.create(
            name = "AboutStore",
            initialState = State(
                language = language,
                user = getUserUseCase()
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    @Suppress("unused")
    private sealed interface Action {
        data class LanguageIsChanged(val language: String) : Action
    }

    private sealed interface Msg {
        @Suppress("unused")
        data class LanguageIsChanged(val language: String) : Msg
    }

    @Suppress("unused")
    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                observeLanguageUseCase().collect {
                    dispatch(Action.LanguageIsChanged(it))
                }
            }
        }
    }

    @Suppress("unused")
    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.ClickedChangeLanguage -> {
                    saveLanguageUseCase(intent.language)
                    dispatch(Msg.LanguageIsChanged(intent.language))
                }

                Intent.ClickedRules -> {
                    publish(Label.ClickedRules)
                }

                is Intent.DeleteAccount -> {
                    scope.launch {
                        deleteSelfAccountUseCase(intent.userName, intent.navigateToNotloggedScreen)
                    }
                }
                is Intent.DeleteTeam -> {
                    scope.launch {
                        deleteTeamUseCase(intent.navigateToNotloggedScreen)
                    }
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

    @Suppress("unused")
    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.LanguageIsChanged -> {
                    copy(language = msg.language)
                }
            }
    }
}
