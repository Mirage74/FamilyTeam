package com.balex.familyteam.presentation.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.active
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.about.AboutContent
import com.balex.familyteam.presentation.loggeduser.LoggedUserContent
import com.balex.familyteam.presentation.loginadmin.LoginAdminContent
import com.balex.familyteam.presentation.loginuser.LoginUserContent
import com.balex.familyteam.presentation.notlogged.NotLoggedContent
import com.balex.familyteam.presentation.regadmin.RegAdminContent
import com.balex.familyteam.presentation.ui.theme.FamilyTeamTheme

@Composable
fun RootContent(component: RootComponent, activity: MainActivity) {
    FamilyTeamTheme {

        when (val child = component.stack.active.instance) {
                is RootComponent.Child.NotLogged -> {
                    NotLoggedContent(component = child.component)
                }

                is RootComponent.Child.RegAdmin -> {
                    RegAdminContent(component = child.component, activity = activity)
                }

                is RootComponent.Child.LoginAdmin -> {
                    LoginAdminContent(component = child.component)
                }
                
                is RootComponent.Child.LoginUser -> {
                    LoginUserContent(component = child.component)
                }
                
                is RootComponent.Child.LoggedUser -> {
                    LoggedUserContent(component = child.component)
                }

                is RootComponent.Child.About -> {
                    AboutContent(component = child.component)
                }
            }

    }
}