package com.balex.familyteam.presentation.loginuser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultLoginUserComponent @AssistedInject constructor(
    private val storeFactory: LoginUserStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoginUserComponent, ComponentContext by componentContext {
    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoginUserComponent
    }
}