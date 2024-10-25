package com.balex.logged_user

import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import kotlinx.coroutines.flow.StateFlow

interface LoggedUserComponent {

    val model: StateFlow<LoggedUserStore.State>

    fun onClickAddNewTask()

    fun onClickDeleteTask(externalTask: ExternalTask)

    fun onClickAddNewTaskForMeToFirebase(task: Task)

    fun onClickAddNewTaskForOtherUserToFirebase(externalTask: ExternalTask)

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

    suspend fun onClickLogout()

    fun onRefreshLanguage()

    fun onLanguageChanged(language: String)

}