package com.balex.familyteam.presentation.customnavigation

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.arkivanov.decompose.router.children.children
import com.balex.familyteam.presentation.customnavigation.CustomNavigationComponent.Children
import com.balex.familyteam.presentation.customnavigation.CustomNavigationComponent.Mode
import com.arkivanov.decompose.value.Value
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

class DefaultCustomNavigationComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
) : CustomNavigationComponent, ComponentContext by componentContext {

    //private val navigation = SimpleNavigation<(NavigationState) -> NavigationState>()

//    private val _children: Value<CustomNavigationComponent.Children<Config, KittenComponent>> =
//        children(
//            source = navigation,
//            //stateSerializer = NavigationState.serializer(),
//            key = "carousel",
//            initialState = {
//                NavigationState(
//                    configurations = ImageResourceId.entries.map(::Config),
//                    index = 0,
//                    mode = Mode.CAROUSEL,
//                )
//            },
//            navTransformer = { state, transformer -> transformer(state) },
//            stateMapper = { state, children ->
//                Children(
//                    items = children.map { it as Child.Created },
//                    index = state.index,
//                    mode = state.mode,
//                )
//            },
//            backTransformer = {
//                it.takeIf { it.index > 0 }?.let { state ->
//                    { state.copy(index = state.index - 1) }
//                }
//            },
//            childFactory = { config, componentContext ->
//                DefaultKittenComponent(
//                    componentContext = componentContext,
//                    imageResourceId = config.imageResourceId,
//                )
//            },
//        )
//
//    override val children: Value<CustomNavigationComponent.Children<*, KittenComponent>> = _children
//
//    override fun onSwitchToPagerClicked() {
//        navigation.navigate { state ->
//            state.copy(mode = CustomNavigationComponent.Mode.PAGER)
//        }
//    }
//
//    override fun onSwitchToCarouselClicked() {
//        navigation.navigate { state ->
//            state.copy(mode = CustomNavigationComponent.Mode.CAROUSEL)
//        }
//    }
//
//    override fun onForwardClicked() {
//        navigation.navigate { state ->
//            state.copy(index = (state.index + 1) % state.configurations.size)
//        }
//    }
//
//    override fun onBackwardClicked() {
//        navigation.navigate { state ->
//            val size = state.configurations.size
//            state.copy(index = (size + state.index - 1) % size)
//        }
//    }
//
//    override fun onShuffleClicked() {
//        navigation.navigate { state ->
//            state.copy(configurations = state.configurations.shuffled())
//        }
//    }
//
//    override fun onCloseClicked() {
//        onFinished()
//    }
//
//    @Parcelize
//    private data class Config(val imageResourceId: ImageResourceId) : Parcelable
//
//    @Parcelize
//    private data class NavigationState(
//        val configurations: List<Config>,
//        val index: Int,
//        val mode: Mode,
//    ) : NavState<Config>, Parcelable {
//
//        override val children: List<SimpleChildNavState<Config>> by lazy {
//            configurations.mapIndexed { index, config ->
//                SimpleChildNavState(
//                    configuration = config,
//                    status = if (index == this.index) ChildNavState.Status.ACTIVE else ChildNavState.Status.INACTIVE,
//                )
//            }
//        }
//    }
}