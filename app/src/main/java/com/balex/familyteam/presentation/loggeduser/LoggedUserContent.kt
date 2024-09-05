package com.balex.familyteam.presentation.loggeduser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.balex.familyteam.LocalLocalizedContext
import com.balex.familyteam.LocalizedContextProvider
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.MenuItems
import com.balex.familyteam.presentation.SwitchLanguage
import com.balex.familyteam.presentation.loggeduser.content.DisplayNameTextField
import com.balex.familyteam.presentation.loggeduser.content.NickNameTextField
import com.balex.familyteam.presentation.loggeduser.content.PasswordTextField
import com.balex.familyteam.presentation.loggeduser.content.RegisterNewUserButton
import com.balex.familyteam.presentation.notlogged.DrawerContent
import com.balex.familyteam.presentation.ui.theme.DarkBlue
import kotlinx.coroutines.launch

@Composable
fun LoggedUserContent(component: LoggedUserComponent) {
    val state by component.model.collectAsState()

    LocalizedContextProvider(languageCode = state.language.lowercase()) {

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedUserScreen(component: LoggedUserComponent, state: LoggedUserStore.State) {
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
                    .height(dimensionResource(id = R.dimen.top_bar_height).value.dp),
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
                                .size(dimensionResource(id = R.dimen.top_bar_height).value.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu_hamburger),
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
            MainContent(component, state)
        }
    }
}


@Composable
fun MainContent(component: LoggedUserComponent, state: LoggedUserStore.State) {
    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.TodoList,
                    onClick = { component.onNavigateToBottomItem(PagesNames.TodoList) },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "To-Do list"
                        )
                    },
                    label = { Text("My") },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.MyTasksForOtherUsers,
                    onClick = { component.onNavigateToBottomItem(PagesNames.MyTasksForOtherUsers) },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Tasks to other"
                        )
                    },
                    label = { Text("Others") },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.ShopList,
                    onClick = { component.onNavigateToBottomItem(PagesNames.ShopList) },
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shop list"
                        )
                    },
                    label = { Text("Shop") },
                    selectedContentColor = androidx.compose.material.MaterialTheme.colors.onPrimary,
                    unselectedContentColor = androidx.compose.material.MaterialTheme.colors.onSecondary
                )
                BottomNavigationItem(
                    selected = state.activeBottomItem == PagesNames.AdminPanel,
                    onClick = { component.onNavigateToBottomItem(PagesNames.AdminPanel) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Admin panel") },
                    label = { Text("Admin") },
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
                    paddingValues = paddingValues
                )

                PagesNames.ShopList -> ShopListContent(
                    component = component,
                    state = state,
                    paddingValues = paddingValues
                )

                PagesNames.MyTasksForOtherUsers -> MyTasksForOtherUsersContent(
                    component = component,
                    state = state,
                    paddingValues = paddingValues
                )

                PagesNames.AdminPanel -> AdminPanelContent(
                    component = component,
                    state = state,
                    paddingValues = paddingValues
                )


            }
        }
    }
}

@Composable
fun TodoListContent(
    component: LoggedUserComponent,
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
fun ShopListContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        items(state.todoList.listToShop) { item ->
            Text(text = "Shop Item: $item")
        }
    }
}

@Composable
fun MyTasksForOtherUsersContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
    ) {
        items(state.myTasksForOtherUsersList.externalTasks) { item ->
            Text(text = "Task: $item")
        }
    }
}

@Composable
fun AdminPanelContent(
    component: LoggedUserComponent,
    state: LoggedUserStore.State,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        val context = LocalLocalizedContext.current

        Text(text = "Admin Page")
        if (!state.isEditUsersListClicked) {
            Button(
                onClick = { component.onAdminPageCreateNewUserClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Text(
                    text = "Create new user"
                )
            }
        }

        if (!state.isCreateNewUserClicked) {
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Text(
                    text = "Edit user's list"
                )
            }
        }


        if (state.isCreateNewUserClicked) {
            NickNameTextField(state, component, context)
            Text(
                text = context.getString(R.string.optional)
            )
            DisplayNameTextField(state, component, context)
            PasswordTextField(state, component, context)
            Spacer(modifier = Modifier.height(24.dp))
            RegisterNewUserButton(state, component, context)
        }
    }


}

