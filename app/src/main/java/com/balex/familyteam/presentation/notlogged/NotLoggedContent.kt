package com.balex.familyteam.presentation.notlogged

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.LocalLocalizedContext
import com.balex.common.R
import com.balex.common.SwitchLanguage
import com.balex.common.domain.entity.MenuItems
import com.balex.common.theme.DarkBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.balex.common.R as commonR


@Composable
fun NotLoggedContent(component: NotLoggedComponent) {

    val state by component.model.collectAsState(Dispatchers.Main.immediate)

    com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {

        when (state.logChooseState) {

            NotLoggedStore.State.LogChooseState.NoSavedUserFound -> {
                NotLoggedScreen(component)
            }

            NotLoggedStore.State.LogChooseState.Initial -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            }

            NotLoggedStore.State.LogChooseState.ErrorLoadingUserData -> {
                ErrorScreen(state)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotLoggedScreen(component: NotLoggedComponent) {
    val state by component.model.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .background(Color.Cyan)
                    .padding(16.dp)
                    .width(192.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val context = LocalLocalizedContext.current
                val menuItems = MenuItems.fromResources(context)
                Text(
                    text = menuItems.getItem(MenuItems.MENU_ITEM_RULES),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch {
                                drawerState.close()
                                component.onClickRules()
                            }
                        },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )

                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = menuItems.getItem(MenuItems.MENU_ITEM_ABOUT),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch {
                                drawerState.close()
                                component.onClickAbout()
                            }
                        },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
            }
        }
    ) {

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = colorResource(R.color.orange_gold)

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

                ShowContent(true, component)

            }


        }
    }
}


@Composable
fun ShowContent(
    isEnabled: Boolean,
    component: NotLoggedComponent
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        val context = LocalLocalizedContext.current
        val regAdmText = context.getString(R.string.reg_adm)
        val logUserText = context.getString(R.string.login_button)
        val textSize = dimensionResource(id = commonR.dimen.button_text_size).value.sp

        Button(
            onClick = { component.onClickRegAdmin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEnabled
        ) {
            Text(
                fontSize = textSize,
                text = regAdmText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Button(
            onClick = { component.onClickLoginUser() },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEnabled
        ) {
            Text(
                fontSize = textSize,
                text = logUserText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ErrorScreen(state: NotLoggedStore.State) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = state.errorMessage,
            color = Color.Red,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}