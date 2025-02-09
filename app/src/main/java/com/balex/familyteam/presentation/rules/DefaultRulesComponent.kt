package com.balex.familyteam.presentation.rules

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultRulesComponent @AssistedInject constructor(
    private val storeFactory: RulesStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext

) : RulesComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }

    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    RulesStore.Label.ClickedAbout -> {
                        onAbout()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<RulesStore.State> = store.stateFlow

    override fun onRefreshLanguage() {
        store.accept(RulesStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(RulesStore.Intent.ClickedChangeLanguage(language))
    }

    override fun onClickAbout() {
        store.accept(RulesStore.Intent.ClickedAbout)
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRulesComponent
    }
}