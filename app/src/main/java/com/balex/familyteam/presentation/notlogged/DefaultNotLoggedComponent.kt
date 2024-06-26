package com.balex.familyteam.presentation.notlogged

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.familyteam.domain.entity.Language
import com.balex.familyteam.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultNotLoggedComponent  @AssistedInject constructor(
    private val storeFactory: NotLoggedStoreFactory,
    @Assisted("onRegAdminClicked") private val onRegAdminClicked: () -> Unit,
    @Assisted("onLoginAdminClicked") private val onLoginAdminClicked: () -> Unit,
    @Assisted("onLoginUserClicked") private val onLoginUserClicked: () -> Unit,
    @Assisted("onUserIsLogged") private val onUserIsLogged: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : NotLoggedComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(Language.DEFAULT_LANGUAGE.symbol) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {

                    is NotLoggedStore.Label.ClickedRegisterAdmin -> {
                        Log.d("NotLoggedComponent", "ClickedRegisterAdmin")
                        onRegAdminClicked()
                    }

                    NotLoggedStore.Label.ClickedLoginAdmin -> {
                        Log.d("NotLoggedComponent", "ClickedLoginAdmin")
                        onLoginAdminClicked()
                    }
                    NotLoggedStore.Label.ClickedLoginUser -> {
                        Log.d("NotLoggedComponent", "ClickedLoginUser")
                        onLoginUserClicked()
                    }

                    NotLoggedStore.Label.UserIsLogged -> {
                        Log.d("NotLoggedComponent", "UserIsLogged")
                        onUserIsLogged()
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<NotLoggedStore.State> = store.stateFlow


    override fun onClickRegAdmin() {
        store.accept(NotLoggedStore.Intent.ClickedRegisterAdmin)
    }

    override fun onClickLoginAdmin() {
        store.accept(NotLoggedStore.Intent.ClickedLoginAdmin)
    }

    override fun onClickLoginUser() {
        store.accept(NotLoggedStore.Intent.ClickedLoginUser)
    }

    override fun onLanguageChanged(language: String) {
        store.accept(NotLoggedStore.Intent.ClickedChangeLanguage(language))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onRegAdminClicked") onRegAdminClicked: () -> Unit,
            @Assisted("onLoginAdminClicked") onLoginAdminClicked: () -> Unit,
            @Assisted("onLoginUserClicked") onLoginUserClicked: () -> Unit,
            @Assisted("onUserIsLogged") onUserIsLogged: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultNotLoggedComponent
    }
}
