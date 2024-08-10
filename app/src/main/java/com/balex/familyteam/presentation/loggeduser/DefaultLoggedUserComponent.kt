package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.balex.familyteam.extensions.componentScope
import com.balex.familyteam.presentation.customnavigation.CustomNavigationComponent
import com.balex.familyteam.presentation.loggeduser.adminpanel.AdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.adminpanel.DefaultAdminPanelComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.DefaultShopListComponent
import com.balex.familyteam.presentation.loggeduser.shoplist.ShopListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.DefaultTodoListComponent
import com.balex.familyteam.presentation.loggeduser.todolist.TodoListComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize


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

//    private val store = instanceKeeper.getStore { storeFactory.create() }
//    private val scope = componentScope()
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    override val model: StateFlow<LoggedUserStore.State> = store.stateFlow
//
//    //private val navigation = StackNavigation<ConfigLoggedUser>()
//    private val navigation = SimpleNavigation<(NavigationState) -> NavigationState>()
//    private val navState = BehaviorSubject<NavigationState?>(null)
//
//    override val children: Value<CustomNavigationComponent.Children> =
//        children(
//            source = navigation,
//            key = "children",
//            initialState =  { NavigationState() },
//            navTransformer = { navState, event -> event(navState) },
//            stateMapper = { _, children ->
//                (CustomNavigationComponent.Children(
//                    todoList = children.find {  it.configuration == Config.TodoList } as Child.Created<*, TodoListComponent>,
//                    shopList = children.find { it.configuration == Config.ShopList  } as Child.Created<*, ShopListComponent>,
//                    adminPanel = children.find {  it.configuration == Config.AdminPanel } as Child.Created<*, AdminPanelComponent>
//                ))
//            },
//            onStateChanged = { newState: NavigationState, _ -> navState.onNext(newState) },
//            childFactory = ::child,
//        )
//
//
//    private fun child(
//        config: Config, componentContext: ComponentContext
//    ): Any =
//        when (config) {
//            Config.AdminPanel -> {
//                val component = todoListComponentFactory.create(
//                    componentContext = componentContext
//                )
//                LoggedUserComponent.Children.TodoList(component)
//            }
//
//            Config.ShopList -> {
//                val component = shopListComponentFactory.create(
//                    componentContext = componentContext
//                )
//                LoggedUserComponent.Children.ShopList(component)
//            }
//
//            Config.TodoList -> {
//                val component = adminPanelComponentFactory.create(
//                    componentContext = componentContext
//                )
//                LoggedUserComponent.Children.AdminPanel(component)
//            }
//        }
//
//
//    @Parcelize
//    sealed interface Config : Parcelable  {
//
//        @Parcelize
//        data object TodoList : Config
//
//        @Parcelize
//        data object ShopList : Config
//
//        @Parcelize
//        data object AdminPanel : Config
//
//    }
//
//
//
//
//    @Parcelize
//    private data class NavigationState(
//        val todoList: ChildNavState<Config> = SimpleChildNavState(Config.TodoList, ChildNavState.Status.ACTIVE),
//        val shopList: ChildNavState<Config> = SimpleChildNavState(Config.ShopList, ChildNavState.Status.INACTIVE),
//        val adminPanel: ChildNavState<Config> = SimpleChildNavState(Config.AdminPanel, ChildNavState.Status.INACTIVE)
//    ) : NavState<Config>, Parcelable  {
//        override val children: List<ChildNavState<Config>> by lazy {
//            listOfNotNull(
//                todoList,
//                shopList,
//                adminPanel
//            )
//        }
//    }
//
//
//
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