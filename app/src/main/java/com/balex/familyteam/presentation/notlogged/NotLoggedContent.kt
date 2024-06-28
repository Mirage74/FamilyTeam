package com.balex.familyteam.presentation.notlogged

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.LocalLocalizedContext
import com.balex.familyteam.LocalizedContextProvider
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.LanguagesList
import com.balex.familyteam.domain.entity.MenuItems
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotLoggedContent(component: NotLoggedComponent) {

    val state by component.model.collectAsState()

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

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = colorResource(R.color.orange_gold),

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
                            SwitchLanguage(state.language, component)
                        }
                    }
                )
                MainContent(component)
            }


        }
    }
}

@Composable
fun MainContent(component: NotLoggedComponent) {
    val state by component.model.collectAsState()
    LocalizedContextProvider(languageCode = state.language.lowercase()) {

        when (state.logChooseState) {
            NotLoggedStore.State.LogChooseState.Initial -> {
                ThreeButtonsScreen(true, component)
            }

            NotLoggedStore.State.LogChooseState.ErrorLoadingUserData -> {
                TODO()
            }

            NotLoggedStore.State.LogChooseState.NoSavedUserFound -> {
                ThreeButtonsScreen(true, component)
            }
        }
    }
}


@Composable
fun DrawerContent(
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, dimensionResource(id = R.dimen.top_bar_height).value.dp, 0.dp, 0.dp)
    ) {
        val textSize = dimensionResource(id = R.dimen.hamburger_text_size).value.sp
        items.forEach { item ->
            Text(
                text = item,
                fontSize = textSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        onItemClick(item)
                    }
            )
        }
    }
}


@Composable
fun SwitchLanguage(
    currentLanguage: String,
    component: NotLoggedComponent
) {
    var isChooseOptionDropMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val languages = LanguagesList().languages


    Box(
        modifier = Modifier
            .height(dimensionResource(id = R.dimen.top_bar_height).value.dp)
            .width(144.dp)
            .background(Color.Gray)
            //.border(width = 1.dp, color = Color.Black)
    ) {
        IconButton(
            onClick = {
                isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
            },
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.top_bar_height).value.dp)
                .align(Alignment.TopStart)

        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language),
                contentDescription = "Switch language"
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            val textSize = dimensionResource(id = R.dimen.current_language_text_size).value.sp
            Text(
                modifier = Modifier.clickable {
                    isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
                },


                fontSize = textSize,
                text = currentLanguage.uppercase()
            )
        }


        IconButton(
            onClick = {
                isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Switch language")
        }


        DropdownMenu(
            expanded = isChooseOptionDropMenuExpanded,
            onDismissRequest = {
                isChooseOptionDropMenuExpanded = false
            },

            modifier = Modifier
                .width(150.dp)
                .background(Color.White)
                .padding(0.dp)
                .border(1.dp, Color.Black)

        ) {
            val textSize = dimensionResource(id = R.dimen.language_list_text_size).value.sp
            languages.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(0.dp)
                        .border(1.dp, Color.LightGray),
                    onClick = {
                        component?.onLanguageChanged(option.symbol.uppercase())
                        isChooseOptionDropMenuExpanded = false
                    },
                    text = {
                        Text(
                            text = option.description.trim(),
                            fontSize = textSize
                        )
                    },
                )
            }
        }

    }
}

@Composable
fun ThreeButtonsScreen(
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
        val logAdmText = context.getString(R.string.log_adm)
        val logUserText = context.getString(R.string.log_user)
        val textSize = dimensionResource(id = R.dimen.button_text_size).value.sp

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
            onClick = { component.onClickLoginAdmin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEnabled
        ) {
            Text(
                fontSize = textSize,
                text = logAdmText,
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

