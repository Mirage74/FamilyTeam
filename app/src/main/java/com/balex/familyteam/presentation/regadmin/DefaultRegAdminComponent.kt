package com.balex.familyteam.presentation.regadmin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultRegAdminComponent @AssistedInject constructor(
    private val storeFactory: RegAdminStoreFactory,
    @Assisted("onBackClicked") private val onBackClicked: () -> Unit,
    @Assisted("onAdminRegisteredAndVerified") private val onAdminRegisteredAndVerified: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : RegAdminComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {

                    RegAdminStore.Label.AdminIsRegisteredAndVerified -> {
                        onAdminRegisteredAndVerified()
                    }

                    RegAdminStore.Label.ClickedBack -> {
                        onBackClicked()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<RegAdminStore.State> = store.stateFlow


    override fun onClickBack() {
        store.accept(RegAdminStore.Intent.ClickedBack)
    }

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

    override fun onPasswordFieldChanged(currentPasswordText: String) {
        store.accept(RegAdminStore.Intent.PasswordFieldChanged(currentPasswordText))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onBackClicked") onBackClicked: () -> Unit,
            @Assisted("onAdminRegisteredAndVerified") onAdminRegisteredAndVerified: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRegAdminComponent
    }
}

