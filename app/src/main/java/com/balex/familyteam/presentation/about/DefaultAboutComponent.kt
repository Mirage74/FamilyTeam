package com.balex.familyteam.presentation.about

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.extensions.componentScope
import com.balex.familyteam.presentation.notlogged.NotLoggedStore
import com.balex.logged_user.LoggedUserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultAboutComponent @AssistedInject constructor(
    private val storeFactory: AboutStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("onRules") private val onRules: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : AboutComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    AboutStore.Label.ClickedRules -> {
                        onRules()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<AboutStore.State> = store.stateFlow

    override fun onRefreshLanguage() {
        store.accept(AboutStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(AboutStore.Intent.ClickedChangeLanguage(language))
    }

    override fun onClickRules() {
        store.accept(AboutStore.Intent.ClickedRules)
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onRules") onRules: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultAboutComponent
    }
}