package com.balex.familyteam.presentation.loggeduser.adminpanel

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultAdminPanelComponent @AssistedInject constructor(
    private val storeFactory: AdminPanelStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : AdminPanelComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultAdminPanelComponent
    }
}