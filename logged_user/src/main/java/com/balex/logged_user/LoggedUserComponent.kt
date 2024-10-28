package com.balex.logged_user

import com.balex.common.data.local.model.ShopItemDBModel
import com.balex.common.data.repository.TaskMode
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import kotlinx.coroutines.flow.StateFlow

interface LoggedUserComponent {

    val model: StateFlow<LoggedUserStore.State>

    fun onBackFromNewTaskFormClicked()

    fun onBackClickedHandle()

    fun onClickAddNewTask()

    fun onClickAddShopItem()

    fun onClickEditTask(externalTask: ExternalTask, taskType: UserRepositoryImpl.Companion.TaskType)

    fun onClickDeleteTask(externalTask: ExternalTask, taskType: UserRepositoryImpl.Companion.TaskType)

    fun onClickDeleteShopItem(itemId: Long)

    fun onClickAddNewTaskOrEditForMeToFirebase(task: Task, taskMode: TaskMode)

    fun onClickedAddShopItemToDatabase(shopItem: ShopItemDBModel)

    fun onClickAddNewTaskOrEditForOtherUserToFirebase(externalTask: ExternalTask, taskMode: TaskMode)

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