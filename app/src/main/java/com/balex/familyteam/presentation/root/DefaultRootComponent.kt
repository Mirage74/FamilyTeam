package com.balex.familyteam.presentation.root


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.balex.common.domain.entity.User
import com.balex.familyteam.presentation.about.DefaultAboutComponent
import com.balex.logged_user.DefaultLoggedUserComponent
import com.balex.familyteam.presentation.loginuser.DefaultLoginUserComponent
import com.balex.familyteam.presentation.notlogged.DefaultNotLoggedComponent
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import com.balex.familyteam.presentation.root.RootComponent.Child
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable


class DefaultRootComponent @AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    private val loggedUserComponentFactory: DefaultLoggedUserComponent.Factory,
    private val aboutComponentFactory: DefaultAboutComponent.Factory,
    private val loginUserComponent: DefaultLoginUserComponent.Factory,
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
                val component = notLoggedComponentFactory.create(
                    onRegAdminClicked = {
                        navigation.push(Config.RegAdmin)
                    },
                    onLoginUserClicked = {
                        navigation.push(Config.LoginUser(it))
                    },
                    onUserIsLogged = {
                        navigation.replaceAll(Config.LoggedUser)
                    },
                    onAbout = {
                        navigation.push(Config.About)
                    },
                    componentContext = childComponentContext
                )
                Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(
                    onAdminRegisteredAndVerified = {
                        navigation.replaceAll(Config.LoggedUser)
                    }, onAbout = {
                        navigation.push(Config.About)
                    }, onAdminExistButWrongPassword = { userLogin ->
                        navigation.push(Config.LoginUser(userLogin))
                        navigation.popWhile {
                            it == Config.RegAdmin
                        }
                    },
                    onBackClicked = {
                        navigation.pop()
                    }, componentContext = childComponentContext
                )
                Child.RegAdmin(component)
            }

            is Config.LoginUser -> {
                val component = loginUserComponent.create(
                    user = config.user,
                    onAbout = {
                        navigation.push(Config.About)
                    },
                    onUserLogged = {
                        navigation.replaceAll(Config.LoggedUser)
                    },
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
                    onAbout = {
                        navigation.push(Config.About)
                    },
                    onLogout = {
                        navigation.replaceAll(Config.NotLogged)
                    },
                    componentContext = childComponentContext
                )
                Child.LoggedUser(component)
            }
        }
    }


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
        data class LoginUser(val user: User) : Config

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