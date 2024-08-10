package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.value.Value
import com.balex.familyteam.presentation.loggeduser.adminpanel.AdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.ShopListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.TodoListComponent
import kotlinx.coroutines.flow.StateFlow

interface LoggedUserComponent {

    //val children: Value<Children>


    sealed interface Children {

        data class TodoList(val component: TodoListComponent) : Children

        data class ShopList(val component: ShopListComponent) : Children

        data class AdminPanel(val component: AdminPanelComponent) : Children

    }

//    class Children<out C : Any, out T : Any>(
//        val items: List<Child.Created<C, T>>,
//        val index: Int,
//        val mode: Mode,
//    )

    enum class Mode {
        CAROUSEL, PAGER
    }

    //val model: StateFlow<LoggedUserStore.State>

}