package com.balex.familyteam.presentation.about

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.logged_user.LoggedUserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultAboutComponent @AssistedInject constructor(
    private val storeFactory: AboutStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("componentContext") componentContext: ComponentContext
) : AboutComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<AboutStore.State> = store.stateFlow

    override fun onRefreshLanguage() {
        store.accept(AboutStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(AboutStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultAboutComponent
    }
}