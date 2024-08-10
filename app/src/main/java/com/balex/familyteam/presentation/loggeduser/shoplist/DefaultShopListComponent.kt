package com.balex.familyteam.presentation.loggeduser.shoplist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultShopListComponent @AssistedInject constructor(
    private val storeFactory: ShopListStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : ShopListComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultShopListComponent
    }
}