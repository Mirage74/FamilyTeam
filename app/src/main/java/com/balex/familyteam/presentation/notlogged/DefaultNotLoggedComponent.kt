package com.balex.familyteam.presentation.notlogged

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.domain.usecase.regLog.GetLanguageUseCase
import com.balex.familyteam.domain.usecase.regLog.ResetUserToDefaultUseCase
import com.balex.familyteam.domain.usecase.regLog.ResetWrongPasswordUserToDefaultUseCase
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultNotLoggedComponent @AssistedInject constructor(
    private val storeFactory: NotLoggedStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val resetWrongPasswordUserToDefaultUseCase: ResetWrongPasswordUserToDefaultUseCase,
    @Assisted("onAdminExistButWrongPassword") private val onAdminExistButWrongPassword: (User) -> Unit,
    @Assisted("onRegAdminClicked") private val onRegAdminClicked: () -> Unit,
    @Assisted("onLoginUserClicked") private val onLoginUserClicked: (User) -> Unit,
    @Assisted("onUserIsLogged") private val onUserIsLogged: () -> Unit,
    @Assisted("onAbout") private val onAbout: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : NotLoggedComponent, ComponentContext by componentContext {


    private val store =
        instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }
    private val scope = componentScope()
    private var labelsJob: Job? = null

    init {
        lifecycle.doOnResume {
            onRefreshLanguage()
            startCollectingLabels()
            //Log.d("NotLoggedComponent", "doOnResume")
        }
        lifecycle.doOnPause {
            stopCollectingLabels()
        }

    }

    private fun startCollectingLabels() {
        labelsJob?.cancel()

        labelsJob = scope.launch {
            store.labels.collect {
                when (it) {

                    NotLoggedStore.Label.ClickedRegisterAdmin -> {
                        resetWrongPasswordUserToDefaultUseCase()
                        onRegAdminClicked()
                    }

                    NotLoggedStore.Label.ClickedLoginUser -> {
                        onLoginUserClicked(User())
                    }

                    NotLoggedStore.Label.UserIsLogged -> {
                        onUserIsLogged()
                    }

                    NotLoggedStore.Label.ClickedAbout -> {
                        onAbout()
                    }

                    is NotLoggedStore.Label.LoginPageWrongPassword -> {
                        //resetUserToDefaultUseCase()
                        onAdminExistButWrongPassword(it.user)
                    }
                }
            }
        }
    }

    private fun stopCollectingLabels() {
        labelsJob?.cancel()
        labelsJob = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<NotLoggedStore.State> = store.stateFlow

    override fun onRefreshLanguage() {
        store.accept(NotLoggedStore.Intent.RefreshLanguage)
    }

    override fun onClickRegAdmin() {
        store.accept(NotLoggedStore.Intent.ClickedRegisterAdmin)
    }

    override fun onClickLoginUser() {
        store.accept(NotLoggedStore.Intent.ClickedLoginUser)
    }

    override fun onClickAbout() {
        store.accept(NotLoggedStore.Intent.ClickedAbout)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(NotLoggedStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onAdminExistButWrongPassword") onAdminExistButWrongPassword: (User) -> Unit,
            @Assisted("onRegAdminClicked") onRegAdminClicked: () -> Unit,
            @Assisted("onLoginUserClicked") onLoginUserClicked: (User) -> Unit,
            @Assisted("onUserIsLogged") onUserIsLogged: () -> Unit,
            @Assisted("onAbout") onAbout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultNotLoggedComponent
    }
}
