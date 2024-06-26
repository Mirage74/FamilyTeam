package com.balex.familyteam.presentation.notlogged

import com.balex.familyteam.domain.entity.Language
import kotlinx.coroutines.flow.StateFlow

interface NotLoggedComponent {

    val model: StateFlow<NotLoggedStore.State>

    fun onClickRegAdmin()

    fun onClickLoginAdmin()

    fun onClickLoginUser()

    fun onLanguageChanged(language: String)
}