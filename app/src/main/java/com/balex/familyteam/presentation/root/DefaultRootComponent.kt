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


class DefaultRootComponent @OptIn(ExperimentalDecomposeApi::class)
@AssistedInject constructor(
    private val notLoggedComponentFactory: DefaultNotLoggedComponent.Factory,
    private val regAdminComponentFactory: DefaultRegAdminComponent.Factory,
    private val loggedUserComponentFactory: DefaultLoggedUserComponent.Factory,
    private val aboutComponentFactory: DefaultAboutComponent.Factory,
    private val loginUserComponent: DefaultLoginUserComponent.Factory,
    private val loginAdminComponent: DefaultLoginAdminComponent.Factory,
    deepLink: DeepLink = DeepLink.None,
    webHistoryController: WebHistoryController? = null,
    @Assisted("componentContext") componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val nav = StackNavigation<Config>()

    @OptIn(ExperimentalDecomposeApi::class)
    private val _stack =
        childStack(
        source = nav,
        serializer = Config.serializer(),
            initialStack = { getInitialStack(webHistoryPaths = webHistoryController?.historyPaths, deepLink = deepLink) },

            handleBackButton = true,
        childFactory = ::child
    )



    override val stack: Value<ChildStack<*, RootComponent.Child>> = _stack

    private fun child(
        config: Config, componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is Config.NotLogged -> {
                val component = notLoggedComponentFactory.create(
                    onRegAdminClicked = {
                        nav.push(Config.RegAdmin)
                    },
                    onLoginAdminClicked = {

                    },
                    onLoginUserClicked = {

                    },
                    onUserIsLogged = {

                    },
                    onAbout = {
                        nav.push(Config.About)
                    },
                    componentContext = componentContext
                )
                RootComponent.Child.NotLogged(component)
            }

            Config.RegAdmin -> {
                val component = regAdminComponentFactory.create(
                    onAdminRegisteredAndVerified = {
                        nav.replaceAll(Config.LoggedUser)
                    },
                    onBackClicked = {
                        nav.pop()
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


    init {
        @OptIn(ExperimentalDecomposeApi::class)
        webHistoryController?.attach(
            navigator = nav,
            //serializer = Config.serializer(),
            stack = _stack,
            getPath = ::getPathForConfig,
            getConfiguration = ::getConfigForPath,
        )
    }



    private companion object {
        private const val WEB_PATH_NOT_LOGGED = "not-logged"
        private const val WEB_PATH_REG_ADMIN = "reg-admin"
        private const val WEB_PATH_LOGIN_ADMIN = "login-admin"
        private const val WEB_PATH_LOGIN_USER = "login-user"
        private const val WEB_PATH_LOGGED_USER = "logged-user"
        private const val WEB_PATH_ABOUT = "about"

        private fun getInitialStack(webHistoryPaths: List<String>?, deepLink: DeepLink): List<Config> =
            webHistoryPaths
                ?.takeUnless(List<*>::isEmpty)
                ?.map(::getConfigForPath)
                ?: getInitialStack(deepLink)

        private fun getInitialStack(deepLink: DeepLink): List<Config> =
            when (deepLink) {
                is DeepLink.None -> listOf(Config.NotLogged)
                is DeepLink.Web -> listOf(Config.NotLogged, getConfigForPath(deepLink.path)).distinct()
            }


        private fun getPathForConfig(config: Config): String =
            when (config) {
                Config.NotLogged -> "/$WEB_PATH_NOT_LOGGED"
                Config.RegAdmin -> "/$WEB_PATH_REG_ADMIN"
                Config.LoginAdmin -> "/$WEB_PATH_LOGIN_ADMIN"
                Config.LoginUser -> "/$WEB_PATH_LOGIN_USER"
                Config.About -> "/$WEB_PATH_ABOUT"
                Config.LoggedUser -> "/$WEB_PATH_LOGGED_USER"
            }

        private fun getConfigForPath(path: String): Config =
            when (path.removePrefix("/")) {
                WEB_PATH_NOT_LOGGED -> Config.NotLogged
                WEB_PATH_REG_ADMIN -> Config.RegAdmin
                WEB_PATH_LOGIN_ADMIN -> Config.LoginAdmin
                WEB_PATH_LOGIN_USER -> Config.LoginUser
                WEB_PATH_ABOUT -> Config.About
                WEB_PATH_LOGGED_USER -> Config.LoggedUser
                else -> {
                    Config.NotLogged
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
        data object LoginAdmin : Config

        @Serializable
        data object LoginUser : Config

        @Serializable
        data object LoggedUser : Config

        @Serializable
        data object About : Config

    }

    sealed interface DeepLink {
        data object None : DeepLink
        class Web(val path: String) : DeepLink
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultRootComponent
    }



}