package com.balex.familyteam.presentation.loginuser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import com.balex.familyteam.extensions.componentScope
import com.balex.familyteam.presentation.notlogged.NotLoggedStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow

class DefaultLoginUserComponent @AssistedInject constructor(
    private val storeFactory: LoginUserStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("user") private val user: User,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoginUserComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }
    private val scope = componentScope()

    init {
        lifecycle.doOnResume {
            onRefreshLanguage()
        }
    }


    override val model: StateFlow<LoginUserStore.State> = store.stateFlow

    override fun onClickAbout() {
        store.accept(LoginUserStore.Intent.ClickedAbout)
    }

    override fun onRefreshLanguage() {
        store.accept(LoginUserStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(LoginUserStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("user") user: User,
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoginUserComponent
    }
}