package com.balex.familyteam.presentation.customnavigation

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.value.Value
import com.balex.familyteam.presentation.loggeduser.adminpanel.AdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.ShopListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.TodoListComponent

interface CustomNavigationComponent {

    //val children: Value<Children>

    data class Children(
        val todoList: Child.Created<*, TodoListComponent>,
        val shopList: Child.Created<*, ShopListComponent>,
        val adminPanel: Child.Created<*, AdminPanelComponent>
    )

    enum class Mode {
        CAROUSEL, PAGER
    }

}