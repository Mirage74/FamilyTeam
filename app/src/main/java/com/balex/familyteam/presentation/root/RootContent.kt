package com.balex.familyteam.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.about.AboutContent
import com.balex.logged_user.LoggedUserContent
import com.balex.familyteam.presentation.loginuser.LoginUserContent
import com.balex.familyteam.presentation.notlogged.NotLoggedContent
import com.balex.familyteam.presentation.regadmin.RegAdminContent
import com.balex.common.theme.FamilyTeamTheme
import com.balex.familyteam.presentation.rules.RulesContent

@Composable
fun RootContent(component: RootComponent, activity: MainActivity, deviceToken: String) {
    FamilyTeamTheme {
        Children(
            stack = component.stack,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
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

                is RootComponent.Child.Rules -> {
                    RulesContent(component = instance.component)
                }

                is RootComponent.Child.About -> {
                    AboutContent(component = instance.component)
                }
            }
        }
    }
}