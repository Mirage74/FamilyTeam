package com.balex.familyteam.presentation.loginuser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.entity.User
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

class DefaultLoginUserComponent @AssistedInject constructor(
    private val storeFactory: LoginUserStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    @Assisted("user") private val user: User,
    @Assisted("onUserLogged") private val onUserLogged: () -> Unit,
    @Suppress("unused")
    @Assisted("componentContext") componentContext: ComponentContext
) : LoginUserComponent, ComponentContext by componentContext {


    private val store = instanceKeeper.getStore { storeFactory.create(user.adminEmailOrPhone, getLanguageUseCase()) }
    private val scope = componentScope()

    init {
//        lifecycle.subscribe(object : Lifecycle.Callbacks {
//
//            override fun onResume() {
//                super.onResume()
//            }
//        })
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
                    LoginUserStore.Label.UserIsLogged -> {
                        onUserLogged()
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
    override val model: StateFlow<LoginUserStore.State> = store.stateFlow

    override fun onClickLogin() {
        store.accept(LoginUserStore.Intent.ClickedLoginButton)
    }

    override fun onLoginFieldChanged(currentLoginText: String) {
        store.accept(LoginUserStore.Intent.LoginAdminFieldChanged(currentLoginText))
    }

    override fun onNickNameFieldChanged(currentNickNameText: String) {
        store.accept(LoginUserStore.Intent.NickNameFieldChanged(currentNickNameText))
    }

    override fun onPasswordFieldChanged(currentPasswordText: String) {
        store.accept(LoginUserStore.Intent.PasswordFieldChanged(currentPasswordText))
    }

    override fun onClickChangePasswordVisibility() {
        store.accept(LoginUserStore.Intent.ClickedChangePasswordVisibility)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(LoginUserStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("user") user: User,
             @Assisted("onUserLogged") onUserLogged: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoginUserComponent
    }
}