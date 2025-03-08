package com.balex.familyteam.presentation.root


import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.balex.common.domain.entity.User
import com.balex.familyteam.presentation.about.DefaultAboutComponent
import com.balex.familyteam.presentation.loginuser.DefaultLoginUserComponent
import com.balex.familyteam.presentation.notlogged.DefaultNotLoggedComponent
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import com.balex.familyteam.presentation.root.RootComponent.Child
import com.balex.familyteam.presentation.rules.DefaultRulesComponent
import com.balex.logged_user.DefaultLoggedUserComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import java.util.UUID

@Suppress("unused")
class DefaultRootComponent @AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    private val loggedUserComponentFactory: DefaultLoggedUserComponent.Factory,
    private val rulesComponentFactory: DefaultRulesComponent.Factory,
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

    @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
    private fun child(
        config: Config, childComponentContext: ComponentContext
    ): Child {
        return when (config) {
            Config.NotLogged -> {
                val component = notLoggedComponentFactory.create(
                    onRegAdminClicked = {
                        navigation.pushNew(Config.RegAdmin)
                    },
                    onLoginUserClicked = {
                        navigation.pushNew(Config.LoginUser(it))
                    },
                    onUserIsLogged = {
                        navigation.replaceAll(Config.LoggedUser(UUID.randomUUID().toString()))
                    },
                    onRules = {
                        navigation.pushNew(Config.Rules)
                    },
                    onAbout = {
                        navigation.pushNew(Config.About)
                    },
                    componentContext = childComponentContext
                )
                Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(
                    onAdminRegisteredAndVerified = {
                        navigation.replaceAll(Config.LoggedUser(UUID.randomUUID().toString()))
                    }, onAdminExistButWrongPassword = { userLogin ->
                        navigation.replaceAll(Config.NotLogged)
                        navigation.pushNew(Config.LoginUser(userLogin))
                    },
                    componentContext = childComponentContext
                )
                Child.RegAdmin(component)
            }

            is Config.LoginUser -> {
                val component = loginUserComponent.create(
                    user = config.user,
                    onUserLogged = {
                        navigation.replaceAll(Config.LoggedUser(UUID.randomUUID().toString()))
                    },
                    componentContext = childComponentContext
                )
                Child.LoginUser(component)
            }

            Config.Rules -> {
                val component = rulesComponentFactory.create(
                    onAbout = {
                        navigation.pop()
                        navigation.pushNew(Config.About)
                    },
                    componentContext = childComponentContext
                )
                Child.Rules(component)
            }


            Config.About -> {
                val component = aboutComponentFactory.create(
                    onRules = {
                        navigation.pop()
                        navigation.pushNew(Config.Rules)
                    },
                    onLogout = {
                        navigation.replaceAll(Config.NotLogged)
                    },
                    componentContext = childComponentContext
                )
                Child.About(component)
            }

            is Config.LoggedUser -> {
                val component = loggedUserComponentFactory.create(
                    sessionId = config.id,
                    onRules = {
                        navigation.pushNew(Config.Rules)
                    },
                    onAbout = {
                        navigation.pushNew(Config.About)
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

    @Serializable
    sealed interface Config {

        @Serializable
        data object NotLogged : Config

        @Serializable
        data object RegAdmin : Config

        @Serializable
        data class LoginUser(val user: User) : Config

        @Serializable
        data class LoggedUser(val id: String) : Config

        @Serializable
        data object About : Config

        @Serializable
        data object Rules : Config

    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }
}