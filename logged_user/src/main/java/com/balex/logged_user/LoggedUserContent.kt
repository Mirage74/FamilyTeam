package com.balex.logged_user

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.balex.common.DrawerContent
import com.balex.common.LocalLocalizedContext
import com.balex.common.SwitchLanguage
import com.balex.common.domain.entity.MenuItems
import com.balex.common.theme.DarkBlue
import com.balex.logged_user.content.AdminPanelContent
import com.balex.logged_user.content.MyTasksForOtherUsersContent
import com.balex.logged_user.content.ShopListContent
import com.balex.logged_user.content.TodoListContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.balex.common.R as commonR

@Composable
fun LoggedUserContent(
    component: LoggedUserComponent,
    deviceToken: String
) {


    val state by component.model.collectAsState(Dispatchers.Main.immediate)


    var previousSessionId by remember { mutableStateOf<String?>(null) }
    var isFirstRun by remember { mutableStateOf(true) }

    Log.d("LoggedUserContent", "state: $state")
    Log.d("LoggedUserContent", "state.loggedUserState: ${state.loggedUserState}")
    Log.d("LoggedUserContent", "state.sessionId: ${state.sessionId}")

    SideEffect {
        if (isFirstRun  || state.sessionId != previousSessionId ) {
            Log.d("LoggedUserContent", "deviceToken: $deviceToken")
            if (deviceToken.isNotEmpty()) {
                component.sendIntent(LoggedUserStore.Intent.SaveDeviceToken(deviceToken))
            } else {
                Log.d("LoggedUserContent", "deviceToken is empty")
            }
            isFirstRun = false
            previousSessionId = state.sessionId
        }
    }


    BackHandler {
        if (state.isAddTaskClicked || state.isEditTaskClicked || state.isAddShopItemClicked) {
            component.onBackFromNewTaskFormClicked()
        } else {
            component.onBackClickedHandle()
        }

    }

    com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {

        when (state.loggedUserState) {
            LoggedUserStore.State.LoggedUserState.Content -> {
                LoggedUserScreen(component, state, deviceToken)
            }

            LoggedUserStore.State.LoggedUserState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedUserScreen(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    deviceToken: String
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                items = MenuItems().items,
                onItemClick = {
                    scope.launch {
                        drawerState.close()
                        component.onClickAbout()
                    }

                }
            )
        }
    ) {
        Column {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = commonR.dimen.top_bar_height).value.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.LightGray
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(dimensionResource(id = commonR.dimen.top_bar_height).value.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = commonR.drawable.ic_menu_hamburger),
                                contentDescription = "Open Drawer"
                            )
                        }
                        val onLanguageChanged: (String) -> Unit = { newLanguage ->
                            component.onLanguageChanged(newLanguage)
                        }
                        SwitchLanguage(state.language, onLanguageChanged)
                    }
                }
            )
            MainContent(component, state, deviceToken)
        }
    }
}


@Composable
fun MainContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    deviceToken: String
) {
    val selectedColor = androidx.compose.material.MaterialTheme.colors.secondary
    val unSelectedColor = androidx.compose.material.MaterialTheme.colors.onSecondary
    val notAvailableColor = Color.DarkGray
    val context = LocalLocalizedContext.current
    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.TodoList,
                    onClick = { component.onNavigateToBottomItem(PagesNames.TodoList) },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "To-Do list",
                            tint = if (state.activeBottomItem == PagesNames.TodoList) selectedColor else unSelectedColor

                        )
                    },
                    label = {
                        Text(
                            context.getString(R.string.bottom_text_my_tasks),
                            maxLines = 1,
                            color = if (state.activeBottomItem == PagesNames.TodoList) selectedColor else unSelectedColor
                        )
                    },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary


                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.MyTasksForOtherUsers,
                    onClick = { component.onNavigateToBottomItem(PagesNames.MyTasksForOtherUsers) },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Tasks to other",
                            tint = if (state.activeBottomItem == PagesNames.MyTasksForOtherUsers) selectedColor else unSelectedColor
                        )
                    },
                    label = {
                        Text(
                            context.getString(R.string.bottom_text_tasks_for_other),
                            maxLines = 1,
                            color = if (state.activeBottomItem == PagesNames.MyTasksForOtherUsers) selectedColor else unSelectedColor
                        )
                    },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.ShopList,
                    onClick = { component.onNavigateToBottomItem(PagesNames.ShopList) },
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shop list",
                            tint = if (state.activeBottomItem == PagesNames.ShopList) selectedColor else unSelectedColor
                        )
                    },
                    label = {
                        Text(
                            context.getString(R.string.bottom_text_shop_list),
                            maxLines = 1,
                            color = if (state.activeBottomItem == PagesNames.ShopList) selectedColor else unSelectedColor
                        )
                    },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.AdminPanel,
                    enabled = state.user.hasAdminRights,
                    onClick = { component.onNavigateToBottomItem(PagesNames.AdminPanel) },
                    icon = {
                        var itemColor =
                            if (state.activeBottomItem == PagesNames.AdminPanel) selectedColor else unSelectedColor
                        if (!state.user.hasAdminRights) {
                            itemColor = notAvailableColor
                        }
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Admin panel",
                            tint = itemColor
                        )
                    },
                    label = {
                        var itemColor =
                            if (state.activeBottomItem == PagesNames.AdminPanel) selectedColor else unSelectedColor
                        if (!state.user.hasAdminRights) {
                            itemColor = notAvailableColor
                        }
                        Text(
                            context.getString(R.string.bottom_text_admin),
                            maxLines = 1,
                            color = itemColor
                        )
                    },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.Logout,
                    onClick = {
                        CoroutineScope(Dispatchers.Main.immediate).launch {
                            component.onClickLogout()
                        }
                    },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = if (state.activeBottomItem == PagesNames.Logout) selectedColor else unSelectedColor
                        )
                    },
                    label = {
                        Text(
                            context.getString(R.string.bottom_text_logout),
                            maxLines = 1,
                            color = if (state.activeBottomItem == PagesNames.Logout) selectedColor else unSelectedColor
                        )
                    },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )

            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state.activeBottomItem) {
                PagesNames.TodoList -> TodoListContent(
                    component = component,
                    state = state,
                    deviceToken = deviceToken,
                    paddingValues = paddingValues
                )

                PagesNames.ShopList -> ShopListContent(
                    component = component,
                    state = state
                )

                PagesNames.MyTasksForOtherUsers -> MyTasksForOtherUsersContent(
                    component = component,
                    state = state,
                    deviceToken = deviceToken,
                    paddingValues = paddingValues
                )

                PagesNames.AdminPanel -> AdminPanelContent(
                    component = component,
                    state = state,
                    paddingValues = paddingValues
                )

                PagesNames.Logout -> {

                }
            }
        }
    }
}




