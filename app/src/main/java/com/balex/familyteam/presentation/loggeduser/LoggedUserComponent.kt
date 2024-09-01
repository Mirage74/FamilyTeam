package com.balex.familyteam.presentation.loggeduser

import kotlinx.coroutines.flow.StateFlow

interface LoggedUserComponent {

    val model: StateFlow<LoggedUserStore.State>

    fun onClickEditUsersList()

    fun onClickRemoveUserFromFirebase(nickName: String)

    fun onNavigateToBottomItem(page: PagesNames)

    fun onAdminPageCreateNewUserClicked()

    fun onAdminPageRegisterNewUserInFirebaseClicked()

    fun onAdminPageEditUsersListClicked()

    fun onAdminPageCancelCreateNewUserClicked()

    fun onAdminPageRemoveUserClicked(nickName: String)

    fun onNickNameFieldChanged(currentNickNameText: String)

    fun onDisplayNameFieldChanged(currentDisplayNameText: String)

    fun onPasswordFieldChanged(currentPasswordText: String)

    fun onClickChangePasswordVisibility()

    fun onClickAbout()

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)

}