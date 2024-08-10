package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.router.stack.childStack
import com.balex.familyteam.extensions.componentScope
import com.balex.familyteam.presentation.loggeduser.adminpanel.AdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.LoggedUserComponent.Children
import com.balex.familyteam.presentation.loggeduser.adminpanel.DefaultAdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.DefaultShopListComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.ShopListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.DefaultTodoListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.TodoListComponent
import com.balex.familyteam.presentation.root.DefaultRootComponent.Config
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable


class DefaultLoggedUserComponent @AssistedInject constructor(
    private val storeFactory: LoggedUserStoreFactory,
    private val todoListComponentFactory: DefaultTodoListComponent.Factory,
    private val shopListComponentFactory: DefaultShopListComponent.Factory,
    private val adminPanelComponentFactory: DefaultAdminPanelComponent.Factory,

    @Assisted("onTodoListClicked") private val onTodoListClicked: () -> Unit,
    @Assisted("onShopListClicked") private val onShopListClicked: () -> Unit,
    @Assisted("onAdminPanelClicked") private val onAdminPanelClicked: () -> Unit,
    @Assisted("componentContext") componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext {

    //private val store = instanceKeeper.getStore { storeFactory.create() }
    //private val scope = componentScope()


    //override val model: StateFlow<LoggedUserStore.State> = store.stateFlow

    //private val navigation = StackNavigation<ConfigLoggedUser>()
    private val navigation = SimpleNavigation<(NavigationState) -> NavigationState>()
    private val navState = BehaviorSubject<NavigationState?>(null)

    override val children: Value<Children> =
        children(
            source = navigation,
            stateSerializer = NavigationState.serializer(),
            key = "children",
            initialState = ::NavigationState,
            navTransformer = { navState, event -> event(navState) },
            stateMapper = { navState, children ->
                @Suppress("UNCHECKED_CAST")
                Children(
                    todoList = children.first { it.instance is TodoListComponent } as Child.Created<*, TodoListComponent>,
                    shopList = children.first { it.instance is ShopListComponent } as Child.Created<*, ShopListComponent>,
                    adminPanel = children.first { it.instance is AdminPanelComponent } as Child.Created<*, AdminPanelComponent>
                )
            },
            onStateChanged = { newState, _ -> navState.onNext(newState) },
            childFactory = ::child,
        )




    private fun child(
        config: Config, componentContext: ComponentContext
    ): Any =
        when (config) {
            Config.AdminPanel -> {
                todoListComponentFactory.create(
                    componentContext = componentContext
                )
            }

            Config.ShopList -> {
                shopListComponentFactory.create(
                    componentContext = componentContext
                )

            }

            Config.TodoList -> {
                adminPanelComponentFactory.create(
                    componentContext = componentContext
                )

            }
        }


    @Serializable
    sealed interface Config {

        @Serializable
        data object TodoList : Config

        @Serializable
        data object ShopList : Config

        @Serializable
        data object AdminPanel : Config

    }


    @Serializable
    private data class NavigationState(
        val todoList: ChildNavState<Config> = SimpleChildNavState(
            Config.TodoList,
            ChildNavState.Status.ACTIVE
        ),
        val shopList: ChildNavState<Config> = SimpleChildNavState(
            Config.ShopList,
            ChildNavState.Status.INACTIVE
        ),
        val adminPanel: ChildNavState<Config> = SimpleChildNavState(
            Config.AdminPanel,
            ChildNavState.Status.INACTIVE
        )
    ) : NavState<Config> {
        override val children: List<ChildNavState<Config>> by lazy {
            listOfNotNull(
                todoList,
                shopList,
                adminPanel
            )
        }
    }


    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("onTodoListClicked") onTodoListClicked: () -> Unit,
            @Assisted("onShopListClicked") onShopListClicked: () -> Unit,
            @Assisted("onAdminPanelClicked") onAdminPanelClicked: () -> Unit,
            @Assisted("componentContext") componentContext: ComponentContext
        ): DefaultLoggedUserComponent
    }
}