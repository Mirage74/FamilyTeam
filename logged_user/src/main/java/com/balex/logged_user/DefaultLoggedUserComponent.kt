package com.balex.logged_user

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.data.local.model.ShopItemDBModel
import com.balex.common.data.repository.TaskMode
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.LogoutUserUseCase
import com.balex.common.domain.usecases.regLog.RefreshFCMLastTimeUpdatedUseCase
import com.balex.common.domain.usecases.regLog.StorageClearPreferencesUseCase
import com.balex.common.domain.usecases.user.EmitUsersNicknamesListNeedRefreshUseCase
import com.balex.common.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class DefaultLoggedUserComponent @AssistedInject constructor(
    private val storeFactory: LoggedUserStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val storageClearPreferencesUseCase: StorageClearPreferencesUseCase,
    private val refreshFCMLastTimeUpdatedUseCase: RefreshFCMLastTimeUpdatedUseCase,
    private val emitUsersNicknamesListNeedRefreshUseCase: EmitUsersNicknamesListNeedRefreshUseCase,
    @Assisted("sessionId") private val sessionId: String,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("onBackClicked") private val onBackClicked: () -> Unit,
    @Assisted("onLogout") private val onLogout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore { storeFactory.create(getLanguageUseCase(), sessionId) }
    private var scope = componentScope()

    init {
        scope.launch {
            refreshFCMLastTimeUpdatedUseCase()
        }

        lifecycle.doOnResume {
            onRefreshLanguage()
            startCollectingLabels()
        }
        lifecycle.doOnPause {
            stopCollectingLabels()
            store.stopBootstrapperCollectFlow()
        }

        lifecycle.doOnDestroy {
            stopCollectingLabels()
            store.stopBootstrapperCollectFlow()
            scope.cancel()
            store.dispose()
        }
    }

    private fun startCollectingLabels() {
        scope.launch {
            store.labels.collect {
                when (it) {
                    LoggedUserStore.Label.ClickedAbout -> {
                        onAbout()
                    }
                }
            }
        }
    }

    private fun stopCollectingLabels() {
        scope.coroutineContext.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<LoggedUserStore.State> = store.stateFlow

    override fun sendIntent(intent: LoggedUserStore.Intent) {
        store.accept(intent)
    }

    override fun onBackFromNewTaskFormClicked() {
        store.accept(LoggedUserStore.Intent.BackFromNewTaskFormClicked)
    }

    override fun onBackClickedHandle() {
        onBackClicked()
    }

    override fun onClickAddNewTask() {
        store.accept(LoggedUserStore.Intent.ClickedAddNewTask)
    }

    override fun onClickAddShopItem() {
        store.accept(LoggedUserStore.Intent.ClickedAddShopItem)
    }

    override fun onClickEditTask(
        externalTask: ExternalTask,
        taskType: UserRepositoryImpl.Companion.TaskType
    ) {
        store.accept(LoggedUserStore.Intent.ClickedEditTask(externalTask, taskType))
    }

    override fun onClickDeleteTask(
        externalTask: ExternalTask,
        taskType: UserRepositoryImpl.Companion.TaskType,
        token: String
    ) {
        store.accept(LoggedUserStore.Intent.ClickedDeleteTask(externalTask, taskType, token))
    }

    override fun onClickDeleteShopItem(itemId: Long) {
        store.accept(LoggedUserStore.Intent.ClickedDeleteShopItem(itemId))
    }

    override fun onClickAddNewTaskOrEditForMeToFirebase(
        task: Task,
        taskMode: TaskMode,
        token: String
    ) {
        store.accept(
            LoggedUserStore.Intent.ClickedAddPrivateTaskOrEditToFirebase(
                task,
                taskMode,
                token
            )
        )
    }

    override fun onClickedAddShopItemToDatabase(shopItem: ShopItemDBModel) {
        store.accept(LoggedUserStore.Intent.ClickedAddShopItemToDatabase(shopItem))
    }

    override fun onClickAddNewTaskOrEditForOtherUserToFirebase(
        externalTask: ExternalTask,
        taskMode: TaskMode,
        token: String
    ) {
        store.accept(
            LoggedUserStore.Intent.ClickedAddExternalTaskOrEditToFirebase(
                externalTask,
                taskMode,
                token
            )
        )
    }

    override fun onClickEditUsersList() {
        store.accept(LoggedUserStore.Intent.ClickedEditUsersList)
    }


    override fun onClickRemoveUserFromFirebase(nickName: String) {
        store.accept(LoggedUserStore.Intent.ClickedRemoveUserFromFirebase(nickName))
    }


    override fun onNavigateToBottomItem(page: PagesNames) {
        store.accept(LoggedUserStore.Intent.ChangePage(page))
    }

    override fun onAdminPageCreateNewUserClicked() {
        store.accept(LoggedUserStore.Intent.ClickedCreateNewUser)
    }

    override fun onAdminPageRegisterNewUserInFirebaseClicked() {
        store.accept(LoggedUserStore.Intent.ClickedRegisterNewUserInFirebase)
    }

    override fun onAdminPageEditUsersListClicked() {
        store.accept(LoggedUserStore.Intent.ClickedEditUsersList)
    }

    override fun onAdminPageCancelCreateNewUserClicked() {
        store.accept(LoggedUserStore.Intent.ClickedCancelRegisterNewUserInFirebase)
    }

    override fun onAdminPageRemoveUserClicked(nickName: String) {
        store.accept(LoggedUserStore.Intent.ClickedRemoveUserFromFirebase(nickName))
    }

    override fun onNickNameFieldChanged(currentNickNameText: String) {
        store.accept(LoggedUserStore.Intent.NickNameFieldChanged(currentNickNameText))
    }

    override fun onDisplayNameFieldChanged(currentDisplayNameText: String) {
        store.accept(LoggedUserStore.Intent.DisplayNameFieldChanged(currentDisplayNameText))
    }

    override fun onPasswordFieldChanged(currentPasswordText: String) {
        store.accept(LoggedUserStore.Intent.PasswordFieldChanged(currentPasswordText))
    }

    override fun onClickChangePasswordVisibility() {
        store.accept(LoggedUserStore.Intent.ClickedChangePasswordVisibility)
    }

    override fun onClickAbout() {
        store.accept(LoggedUserStore.Intent.ClickedAbout)
    }

    override suspend fun onClickLogout() {
        storageClearPreferencesUseCase()
        logoutUserUseCase()
        onLogout()
    }

    override fun onRefreshLanguage() {
        store.accept(LoggedUserStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(LoggedUserStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("sessionId") sessionId: String,
            @Assisted("onBackClicked") onBackClicked: () -> Unit,
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("onLogout") onLogout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoggedUserComponent
    }
}