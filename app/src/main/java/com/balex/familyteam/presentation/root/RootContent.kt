package com.balex.familyteam.presentation.root

import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import androidx.compose.runtime.Composable
import com.balex.familyteam.presentation.about.AboutContent
import com.balex.familyteam.presentation.loggeduser.LoggedUserContent
import com.balex.familyteam.presentation.loginadmin.LoginAdminContent
import com.balex.familyteam.presentation.loginuser.LoginUserContent
import com.balex.familyteam.presentation.notlogged.NotLoggedContent
import com.balex.familyteam.presentation.regadmin.RegAdminContent
import com.balex.familyteam.presentation.ui.theme.FamilyTeamTheme

@Composable
fun RootContent(component: RootComponent) {
    FamilyTeamTheme {
        Children(
            stack = component.stack
        ) {
            when (val instance = it.instance) {
                is RootComponent.Child.NotLogged -> {
                    NotLoggedContent(component = instance.component)
                }

                is RootComponent.Child.RegAdmin -> {
                    RegAdminContent(component = instance.component)
                }

                is RootComponent.Child.LoginAdmin -> {
                    LoginAdminContent(component = instance.component)
                }
                
                is RootComponent.Child.LoginUser -> {
                    LoginUserContent(component = instance.component)
                }
                
                is RootComponent.Child.LoggedUser -> {
                    LoggedUserContent(component = instance.component)
                }

                is RootComponent.Child.About -> {
                    AboutContent(component = instance.component)
                }
            }
        }
    }
}