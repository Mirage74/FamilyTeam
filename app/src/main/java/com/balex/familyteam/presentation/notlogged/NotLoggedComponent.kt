package com.balex.familyteam.presentation.notlogged

import kotlinx.coroutines.flow.StateFlow

interface NotLoggedComponent {

    val model: StateFlow<NotLoggedStore.State>

    fun onRegAdminClicked()

    fun onLoginAdminClicked()

    fun onLoginUserClicked()
}