package com.balex.familyteam.presentation.loggeduser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balex.familyteam.domain.entity.ExternalTasks
import com.balex.familyteam.domain.entity.MenuItems
import com.balex.familyteam.domain.entity.PrivateTasks
import com.balex.familyteam.presentation.notlogged.DrawerContent
import com.balex.familyteam.presentation.ui.theme.DarkBlue
import kotlinx.coroutines.launch

@Composable
fun LoggedUserContent(component: LoggedUserComponent) {
    val state by component.model.collectAsState()

    when (state.loggedUserState) {
        LoggedUserStore.State.LoggedUserState.Content -> {
            LoggedUserScreen(component, state)
        }

        LoggedUserStore.State.LoggedUserState.Initial -> {
            LoggedUserScreen(component, state)
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

@Composable
fun LoggedUserScreen(component: LoggedUserComponent, state: LoggedUserStore.State) {
    //val state by component.model.collectAsState()
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
                        //component.onClickAbout()
                    }

                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigation {
                    BottomNavigationItem(
                        selected = state.activeBottomItem == PagesNames.TodoList,
                        onClick = { component.onNavigateToBottomItem(PagesNames.TodoList) },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "To-Do List"
                            )
                        },
                        label = { Text("To-Do List") },
                        selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                        unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                    )
                    BottomNavigationItem(
                        selected = state.activeBottomItem == PagesNames.ShopList,
                        onClick = { component.onNavigateToBottomItem(PagesNames.ShopList) },
                        icon = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Shop List"
                            )
                        },
                        label = { Text("Shop List") },
                        selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                        unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                    )
                    BottomNavigationItem(
                        selected = state.activeBottomItem == PagesNames.AdminPanel,
                        onClick = { component.onNavigateToBottomItem(PagesNames.AdminPanel) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Admin Panel") },
                        label = { Text("Admin Panel") },
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
                        state = state,
                        paddingValues = paddingValues
                    )

                    PagesNames.ShopList -> ShopListContent(
                        listToShop = state.todoList.listToShop
                    )

                    PagesNames.AdminPanel -> AdminPanelContent()
                }
            }
        }
    }
}

@Composable
fun TodoListContent(
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    val displayName = state.user.displayName.ifEmpty { state.user.nickName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Hello, $displayName!",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Text(
                text = "Add Todo Item"
            )
        }
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
        ) {
            items(state.todoList.thingsToDoShared.externalTasks) { task ->
                Text(text = "Shared Task: ${task.task.description}")
            }
            items(state.todoList.thingsToDoPrivate.privateTasks) { task ->
                Text(text = "Private Task: ${task.description}")
            }
        }
    }
}

@Composable
fun ShopListContent(listToShop: List<String>) {

    LazyColumn {
        items(listToShop) { item ->
            Text(text = "Shop Item: $item")
        }
    }


}

@Composable
fun AdminPanelContent() {
    Text(text = "Admin Page")
}

@Composable
fun BottomNavigationPages() {

}