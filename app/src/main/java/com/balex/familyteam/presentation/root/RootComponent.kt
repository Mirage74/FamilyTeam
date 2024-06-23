package com.balex.familyteam.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.balex.familyteam.presentation.notlogged.NotLoggedComponent


interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        data class NotLogged(val component: NotLoggedComponent) : Child

    }
}