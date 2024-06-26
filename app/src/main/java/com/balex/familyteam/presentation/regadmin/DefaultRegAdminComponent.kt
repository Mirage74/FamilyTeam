package com.balex.familyteam.presentation.regadmin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultRegAdminComponent  @AssistedInject constructor(
    private val storeFactory: RegAdminStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : RegAdminComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRegAdminComponent
    }

}

