package com.balex.familyteam.presentation.root

import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.balex.familyteam.domain.entity.User
import com.balex.familyteam.presentation.notlogged.DefaultNotLoggedComponent
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.parcelize.Parcelize


class DefaultRootComponent @AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    @Assisted("componentContext") componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = Config.NotLogged,
        handleBackButton = true,
        childFactory = ::child
    )

    private fun child(
        config: Config, componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is Config.NotLogged -> {
                val component = notLoggedComponentFactory.create(
                    onRegAdminClicked = {
                        navigation.push(Config.RegAdmin)
                    },
                    onLoginAdminClicked = {

                    },
                    onLoginUserClicked = {

                    },
                    onUserIsLogged = {

                    },
                    componentContext = componentContext
                )
                RootComponent.Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(
                    componentContext = componentContext
                )
                RootComponent.Child.RegAdmin(component)
            }

            Config.LoginAdmin -> TODO()
            Config.LoginUser -> TODO()
            is Config.LoggedUser -> TODO()

        }
    }

    sealed interface Config : Parcelable {

        @Parcelize
        data object NotLogged : Config

        @Parcelize
        data object RegAdmin : Config

        @Parcelize
        data object LoginAdmin : Config

        @Parcelize
        data object LoginUser : Config

        @Parcelize
        data class LoggedUser(val user: User) : Config

    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }
}