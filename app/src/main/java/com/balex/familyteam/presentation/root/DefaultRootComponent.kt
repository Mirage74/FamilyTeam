package com.balex.familyteam.presentation.root


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import com.balex.familyteam.presentation.about.DefaultAboutComponent
import com.balex.familyteam.presentation.loginadmin.DefaultLoginAdminComponent
import com.balex.familyteam.presentation.loginuser.DefaultLoginUserComponent
import com.balex.familyteam.presentation.notlogged.DefaultNotLoggedComponent
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import com.balex.familyteam.presentation.root.RootComponent.Child
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.balex.familyteam.presentation.loggeduser.DefaultLoggedUserComponent


class DefaultRootComponent @AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    private val loggedUserComponentFactory: DefaultLoggedUserComponent.Factory,
    private val aboutComponentFactory: DefaultAboutComponent.Factory,
    private val loginUserComponent: DefaultLoginUserComponent.Factory,
    private val loginAdminComponent: DefaultLoginAdminComponent.Factory,
    @Assisted("componentContext") componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val _stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.NotLogged,
        handleBackButton = true,
        childFactory = ::child
    )


    override val stack: Value<ChildStack<*, Child>> = _stack

    private fun child(
        config: Config, childComponentContext: ComponentContext
    ): Child {
        return when (config) {
             Config.NotLogged -> {
                val component = notLoggedComponentFactory.create(onRegAdminClicked = {
                    navigation.push(Config.RegAdmin)
                }, onLoginAdminClicked = {

                }, onLoginUserClicked = {

                }, onUserIsLogged = {
                    navigation.replaceAll(Config.LoggedUser)

                }, onAbout = {
                    navigation.push(Config.About)
                }, componentContext = childComponentContext
                )
                Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(onAdminRegisteredAndVerified = {
                    navigation.replaceAll(Config.LoggedUser)
                }, onBackClicked = {
                    navigation.pop()
                }, componentContext = childComponentContext
                )
                Child.RegAdmin(component)
            }

            Config.LoginAdmin -> {
                val component = loginAdminComponent.create(
                    componentContext = childComponentContext
                )
                Child.LoginAdmin(component)
            }

            Config.LoginUser -> {
                val component = loginUserComponent.create(
                    componentContext = childComponentContext
                )
                Child.LoginUser(component)
            }

            Config.About -> {
                val component = aboutComponentFactory.create(
                    componentContext = childComponentContext
                )
                Child.About(component)
            }

            is Config.LoggedUser -> {
                val component = loggedUserComponentFactory.create(
//                    onTodoListClicked = {},
//                    onShopListClicked = {},
//                    onAdminPanelClicked = {},
                    componentContext = childComponentContext
                )
                Child.LoggedUser(component)
            }
        }
    }

//    private fun notLoggedComponent(componentContext: ComponentContext): NotLoggedComponent =
//        DefaultMainComponent(
//            componentContext = componentContext,
//            onShowWelcome = { navigation.push(Config.Welcome) },
//        )

//    override fun onBackClicked(toIndex: Int) {
//        navigation.popTo(index = toIndex)
//    }

    @Serializable
    sealed interface Config {

        @Serializable
        data object NotLogged : Config

        @Serializable
        data object RegAdmin : Config

        @Serializable
        data object LoginAdmin : Config

        @Serializable
        data object LoginUser : Config

        @Serializable
        data object LoggedUser : Config

        @Serializable
        data object About : Config

    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }
}