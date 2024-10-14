package com.balex.familyteam.presentation.notlogged

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.balex.common.DrawerContent
import com.balex.common.R
import com.balex.common.SwitchLanguage
import com.balex.common.domain.entity.MenuItems
import com.balex.common.theme.DarkBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.balex.common.R as commonR


@Composable
fun NotLoggedContent(component: NotLoggedComponent) {

    val state by component.model.collectAsState(context = CoroutineScope(Dispatchers.Main.immediate).coroutineContext)

    var showLoader by remember { mutableStateOf(false) }

//    LaunchedEffect(state.logChooseState) {
//        if (state.logChooseState == NotLoggedStore.State.LogChooseState.Initial) {
//            delay(3000)
//            showLoader = true
//        } else {
//            showLoader = false
//        }
//    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotLoggedScreen(component: NotLoggedComponent) {
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
                //MainContent(component)
                com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {
                    ShowContent(true, component)
                }
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

        val context = com.balex.common.LocalLocalizedContext.current
        val regAdmText = context.getString(R.string.reg_adm)
        //val logAdmText = context.getString(R.string.log_adm)
        val logUserText = context.getString(R.string.log_user)
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

