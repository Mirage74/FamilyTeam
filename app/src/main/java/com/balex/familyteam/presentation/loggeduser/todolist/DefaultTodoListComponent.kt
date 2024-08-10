package com.balex.familyteam.presentation.loggeduser.todolist

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DefaultTodoListComponent @AssistedInject constructor(
    private val storeFactory: TodoListStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : TodoListComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultTodoListComponent
    }
}