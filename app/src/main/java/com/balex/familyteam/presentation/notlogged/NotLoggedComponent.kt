package com.balex.familyteam.presentation.notlogged

import com.balex.familyteam.domain.entity.Language
import kotlinx.coroutines.flow.StateFlow

interface NotLoggedComponent {

    val model: StateFlow<NotLoggedStore.State>

    fun onRegAdminClicked()

    fun onLoginAdminClicked()

    fun onLoginUserClicked()

    fun onLanguageChanged(language: String)
}