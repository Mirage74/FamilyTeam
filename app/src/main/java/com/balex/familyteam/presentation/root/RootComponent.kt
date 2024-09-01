package com.balex.familyteam.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.balex.familyteam.presentation.about.AboutComponent
import com.balex.familyteam.presentation.loggeduser.LoggedUserComponent
import com.balex.familyteam.presentation.loginuser.LoginUserComponent
import com.balex.familyteam.presentation.notlogged.NotLoggedComponent
import com.balex.familyteam.presentation.regadmin.RegAdminComponent


interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    //fun onBackClicked(toIndex: Int)

    sealed interface Child {

        data class NotLogged(val component: NotLoggedComponent) : Child

        data class RegAdmin(val component: RegAdminComponent) : Child

        data class LoginUser(val component: LoginUserComponent) : Child

        data class LoggedUser(val component: LoggedUserComponent) : Child

        data class About(val component: AboutComponent) : Child

    }
}