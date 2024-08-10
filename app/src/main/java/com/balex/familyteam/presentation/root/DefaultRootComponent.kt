package com.balex.familyteam.presentation.root


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.essenty.parcelable.Parcelable
import com.balex.familyteam.presentation.about.DefaultAboutComponent
import com.balex.familyteam.presentation.loggeduser.DefaultLoggedUserComponent
import com.balex.familyteam.presentation.loginadmin.DefaultLoginAdminComponent
import com.balex.familyteam.presentation.loginuser.DefaultLoginUserComponent
import com.balex.familyteam.presentation.notlogged.DefaultNotLoggedComponent
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject



class DefaultRootComponent @AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    //private val loggedUserComponentFactory: DefaultLoggedUserComponent.Factory,
    private val aboutComponentFactory: DefaultAboutComponent.Factory,
    private val loginUserComponent: DefaultLoginUserComponent.Factory,
    private val loginAdminComponent: DefaultLoginAdminComponent.Factory,
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
                    onAbout = {
                        navigation.push(Config.About)
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(
                    onAdminRegisteredAndVerified = {
                        navigation.replaceAll(Config.LoggedUser)
                    },
                    onBackClicked = {
                        navigation.pop()
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.RegAdmin(component)
            }

            Config.LoginAdmin -> {
                val component = loginAdminComponent.create(
                    componentContext = componentContext
                )
                RootComponent.Child.LoginAdmin(component)
            }
            Config.LoginUser -> {
                val component = loginUserComponent.create(
                    componentContext = componentContext
                )
                RootComponent.Child.LoginUser(component)
            }
            Config.About -> {
                val component = aboutComponentFactory.create(
                    componentContext = componentContext
                )
                RootComponent.Child.About(component)
            }
            is Config.LoggedUser -> {
//                val component = loggedUserComponentFactory.create(
//                    onTodoListClicked = {},
//                    onShopListClicked = {},
//                    onAdminPanelClicked = {},
//                    componentContext = componentContext
//                )
//                RootComponent.Child.LoggedUser(component)
                val component = aboutComponentFactory.create(
                    componentContext = componentContext
                )
                RootComponent.Child.About(component)
            }
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
        data object LoggedUser : Config

        @Parcelize
        data object About : Config

    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }
}