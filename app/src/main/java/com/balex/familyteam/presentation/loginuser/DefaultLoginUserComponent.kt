package com.balex.familyteam.presentation.loginuser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow

class DefaultLoginUserComponent @AssistedInject constructor(
    private val storeFactory: LoginUserStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoginUserComponent, ComponentContext by componentContext {
    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }

    override val model: StateFlow<LoginUserStore.State>
        get() = TODO("Not yet implemented")

    override fun onClickAbout() {
        TODO("Not yet implemented")
    }

    override fun onRefreshLanguage() {
        TODO("Not yet implemented")
    }

    override fun onLanguageChanged(language: String) {
        TODO("Not yet implemented")
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoginUserComponent
    }
}