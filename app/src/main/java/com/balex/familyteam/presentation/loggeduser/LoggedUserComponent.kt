package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.value.Value
import com.balex.familyteam.presentation.loggeduser.adminpanel.AdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.ShopListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.TodoListComponent


interface LoggedUserComponent {


    val children: Value<Children>

    data class Children (

        val todoList: Child.Created<*, TodoListComponent>,

        val shopList: Child.Created<*, ShopListComponent>,

        val adminPanel: Child.Created<*, AdminPanelComponent>,

    )

//    fun onSwitchToPagerClicked()
//    fun onSwitchToCarouselClicked()
//    fun onForwardClicked()
//    fun onBackwardClicked()

//    sealed interface Children<T, U, V, W> {
//
//        data class TodoList(val component: TodoListComponent) : Children<Any?, Any?, Any?, Any?>
//
//        data class ShopList(val component: ShopListComponent) : Children<Any?, Any?, Any?, Any?>
//
//        data class AdminPanel(val component: AdminPanelComponent) : Children<Any?, Any?, Any?, Any?>
//
//    }
//
//    class Children<out C : Any, out T : Any>(
//        val items: List<Child.Created<C, T>>,
//        val index: Int,
//        val mode: Mode
//    )
//
//    enum class Mode {
//        CAROUSEL, PAGER
//    }

    //val model: StateFlow<LoggedUserStore.State>

}