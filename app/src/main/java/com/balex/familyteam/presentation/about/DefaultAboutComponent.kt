package com.balex.familyteam.presentation.about

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultAboutComponent @AssistedInject constructor(
    private val storeFactory: AboutStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : AboutComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultAboutComponent
    }
}