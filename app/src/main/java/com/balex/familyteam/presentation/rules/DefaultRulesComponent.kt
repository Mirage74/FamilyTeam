package com.balex.familyteam.presentation.rules

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultRulesComponent @AssistedInject constructor(
    private val storeFactory: RulesStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : RulesComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRulesComponent
    }
}