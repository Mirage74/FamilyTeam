package com.balex.familyteam.presentation.root

import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import androidx.compose.runtime.Composable
import com.balex.familyteam.presentation.notlogged.NotLoggedContent
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
            }
        }
    }
}