package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow


class DefaultLoggedUserComponent @AssistedInject constructor(
    private val storeFactory: LoggedUserStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(Language.DEFAULT_LANGUAGE.symbol) }
    private val scope = componentScope()


    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<LoggedUserStore.State> = store.stateFlow

    override fun onLanguageChanged(language: String) {
        store.accept(LoggedUserStore.Intent.ChangeLanguage(language))
    }

    override fun onNavigateToBottomItem(page: PagesNames) {
        store.accept(LoggedUserStore.Intent.ChangePage(page))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoggedUserComponent
    }
}