package com.balex.logged_user

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.entity.ExternalTask
import com.balex.common.domain.entity.Task
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.LogoutUserUseCase
import com.balex.common.domain.usecases.regLog.StorageClearPreferencesUseCase
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
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("onLogout") private val onLogout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }
    private val scope = componentScope()

    init {
        lifecycle.doOnResume {
            onRefreshLanguage()
            startCollectingLabels()
        }
        lifecycle.doOnPause {
            stopCollectingLabels()
            store.stopBootstrapperCollectFlow()
        }

        lifecycle.doOnDestroy {
            scope.cancel()
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


    override fun onClickAddNewTaskForMe() {
        store.accept(LoggedUserStore.Intent.ClickedAddMyTask)
    }

    override fun onClickDeleteTask(externalTask: ExternalTask) {
        store.accept(LoggedUserStore.Intent.ClickedDeleteTask(externalTask))
    }

    override fun onClickAddNewTaskForMeToFirebase(task: Task) {
        store.accept(LoggedUserStore.Intent.ClickedAddTaskToFirebase(task))
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
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("onLogout") onLogout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoggedUserComponent
    }
}