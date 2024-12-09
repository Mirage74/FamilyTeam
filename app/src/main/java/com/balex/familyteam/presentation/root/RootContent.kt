package com.balex.familyteam.presentation.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.about.AboutContent
import com.balex.logged_user.LoggedUserContent
import com.balex.familyteam.presentation.loginuser.LoginUserContent
import com.balex.familyteam.presentation.notlogged.NotLoggedContent
import com.balex.familyteam.presentation.regadmin.RegAdminContent
import com.balex.common.theme.FamilyTeamTheme

@Composable
fun RootContent(component: RootComponent, activity: MainActivity, deviceToken: String) {
    FamilyTeamTheme {
        Children(
            stack = component.stack
        ) {
            when (val instance = it.instance) {
                is RootComponent.Child.NotLogged -> {
                    NotLoggedContent(component = instance.component)
                }

                is RootComponent.Child.RegAdmin -> {
                    RegAdminContent(component = instance.component, activity = activity)
                }

                is RootComponent.Child.LoginUser -> {
                    LoginUserContent(component = instance.component)
                }

                is RootComponent.Child.LoggedUser -> {
                    LoggedUserContent(component = instance.component, deviceToken = deviceToken, activity = activity)
                }

                is RootComponent.Child.About -> {
                    AboutContent(component = instance.component)
                }
            }
        }
    }
}