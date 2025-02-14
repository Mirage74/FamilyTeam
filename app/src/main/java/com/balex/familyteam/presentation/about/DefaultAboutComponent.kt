package com.balex.familyteam.presentation.about

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.balex.common.domain.usecases.regLog.GetLanguageUseCase
import com.balex.common.domain.usecases.regLog.LogoutUserUseCase
import com.balex.common.domain.usecases.regLog.StorageClearPreferencesUseCase
import com.balex.common.extensions.componentScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultAboutComponent @AssistedInject constructor(
    private val storeFactory: AboutStoreFactory,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val storageClearPreferencesUseCase: StorageClearPreferencesUseCase,
    @Assisted("onRules") private val onRules: () -> Unit,
    @Assisted("onLogout") private val onLogout: () -> Unit,
    @Suppress("unused")
    @Assisted("componentContext") componentContext: ComponentContext
) : AboutComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(getLanguageUseCase()) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    AboutStore.Label.ClickedRules -> {
                        onRules()
                    }
                }
            }
        }
    }

    @Suppress("unused")
    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<AboutStore.State> = store.stateFlow

    override fun onLanguageChanged(language: String) {
        store.accept(AboutStore.Intent.ClickedChangeLanguage(language))
    }

    override fun onClickRules() {
        store.accept(AboutStore.Intent.ClickedRules)
    }

    override suspend fun onClickDeleteAccount(userName: String) {
        store.accept(AboutStore.Intent.DeleteAccount(userName, onLogout))
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onRules") onRules: () -> Unit,
            @Assisted("onLogout") onLogout: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultAboutComponent
    }
}