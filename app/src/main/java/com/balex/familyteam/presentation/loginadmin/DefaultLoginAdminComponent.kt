package com.balex.familyteam.presentation.loginadmin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultLoginAdminComponent@AssistedInject constructor(
    private val storeFactory: LoginAdminStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoginAdminComponent, ComponentContext by componentContext {
    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoginAdminComponent
    }
}