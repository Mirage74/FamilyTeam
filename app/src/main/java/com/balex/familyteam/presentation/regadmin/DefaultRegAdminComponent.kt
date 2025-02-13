package com.balex.familyteam.presentation.regadmin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.entity.User
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.extensions.componentScope
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
    @Suppress("unused")
    override val phoneFirebaseRepository: PhoneFirebaseRepository,
    @Assisted("onAdminExistButWrongPassword") private val onAdminExistButWrongPassword: (User) -> Unit,
    @Assisted("onAdminRegisteredAndVerified") private val onAdminRegisteredAndVerified: () -> Unit,
    @Suppress("unused")
    @Assisted("componentContext") componentContext: ComponentContext
) : RegAdminComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }

    private val scope = componentScope()

    init {
        lifecycle.doOnResume {
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

                    RegAdminStore.Label.AdminIsRegisteredAndVerified -> {
                        onAdminRegisteredAndVerified()
                    }


                    is RegAdminStore.Label.LoginPageWrongPassword -> {
                        onAdminExistButWrongPassword(it.user)
                    }
                }
            }
        }
    }

    private fun stopCollectingLabels() {
        scope.coroutineContext.cancelChildren()
    }

    @Suppress("unused")
    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<RegAdminStore.State> = store.stateFlow


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

    override fun onLanguageChanged(language: String) {
        store.accept(RegAdminStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onAdminExistButWrongPassword") onAdminExistButWrongPassword: (User) -> Unit,
            @Assisted("onAdminRegisteredAndVerified") onAdminRegisteredAndVerified: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRegAdminComponent
    }


}

