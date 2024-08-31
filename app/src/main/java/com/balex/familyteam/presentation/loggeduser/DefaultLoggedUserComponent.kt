package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow


class DefaultLoggedUserComponent @AssistedInject constructor(
    private val storeFactory: LoggedUserStoreFactory,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(Language.DEFAULT_LANGUAGE.symbol) }
    private val scope = componentScope()


    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<LoggedUserStore.State> = store.stateFlow


    override fun onClickEditUsersList() {
        store.accept(LoggedUserStore.Intent.ClickedEditUsersList)
    }


    override fun onClickRemoveUserFromFirebase(nickName: String) {
        store.accept(LoggedUserStore.Intent.ClickedRemoveUserFromFirebase(nickName))
    }

    override fun onLanguageChanged(language: String) {
        store.accept(LoggedUserStore.Intent.ClickedChangeLanguage(language))
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

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoggedUserComponent
    }
}