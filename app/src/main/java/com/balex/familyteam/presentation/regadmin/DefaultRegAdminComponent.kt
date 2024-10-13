package com.balex.familyteam.presentation.regadmin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.entity.User
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultRegAdminComponent @AssistedInject constructor(
    private val storeFactory: RegAdminStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    override val phoneFirebaseRepository: PhoneFirebaseRepository,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("onAdminExistButWrongPassword") private val onAdminExistButWrongPassword: (User) -> Unit,
    @Assisted("onBackClicked") private val onBackClicked: () -> Unit,
    @Assisted("onAdminRegisteredAndVerified") private val onAdminRegisteredAndVerified: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : RegAdminComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }

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

                    RegAdminStore.Label.ClickedAbout -> {
                        onAbout()
                    }

                    RegAdminStore.Label.AdminIsRegisteredAndVerified -> {
                        onAdminRegisteredAndVerified()
                    }

                    RegAdminStore.Label.ClickedBack -> {
                        onBackClicked()
                    }

                    is RegAdminStore.Label.LoginPageWrongPassword -> {
                        //resetUserToDefaultUseCase()
                        onAdminExistButWrongPassword(it.user)
                    }
                }
            }
        }
    }

    private fun stopCollectingLabels() {
        scope.coroutineContext.cancelChildren()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<RegAdminStore.State> = store.stateFlow


//    override fun onClickBack() {
//        store.accept(RegAdminStore.Intent.ClickedBack)
//    }


    override fun onClickRegister() {
        store.accept(RegAdminStore.Intent.ClickedRegister)
    }


    override fun onClickEmailOrPhoneButton() {
        store.accept(RegAdminStore.Intent.ClickedEmailOrPhoneButton)
    }

    override fun onClickChangePasswordVisibility() {
        store.accept(RegAdminStore.Intent.ClickedChangePasswordVisibility)
    }

    override fun onClickTryAgain() {
        store.accept(RegAdminStore.Intent.ClickedTryAgain)
    }

    override fun onLoginFieldChanged(currentLoginText: String) {
        store.accept(RegAdminStore.Intent.LoginFieldChanged(currentLoginText))
    }

    override fun onNickNameFieldChanged(currentNickNameText: String) {
        store.accept(RegAdminStore.Intent.NickNameFieldChanged(currentNickNameText))
    }

    override fun onDisplayNameFieldChanged(currentDisplayNameText: String) {
        store.accept(RegAdminStore.Intent.DisplayNameFieldChanged(currentDisplayNameText))
    }

    override fun onPasswordFieldChanged(currentPasswordText: String) {
        store.accept(RegAdminStore.Intent.PasswordFieldChanged(currentPasswordText))
    }

    override fun onSmsNumberFieldChanged(currentSmsText: String) {
        store.accept(RegAdminStore.Intent.SmsNumberFieldChanged(currentSmsText))
    }

    override fun onClickAbout() {
        store.accept(RegAdminStore.Intent.ClickedAbout)
    }

    override fun onRefreshLanguage() {
        store.accept(RegAdminStore.Intent.RefreshLanguage)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(RegAdminStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("onAdminExistButWrongPassword") onAdminExistButWrongPassword: (User) -> Unit,
            @Assisted("onBackClicked") onBackClicked: () -> Unit,
            @Assisted("onAdminRegisteredAndVerified") onAdminRegisteredAndVerified: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRegAdminComponent
    }


}

