package com.balex.familyteam.presentation.about

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.common.R
import com.balex.common.SwitchLanguage
import com.balex.common.data.datastore.Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
import com.balex.common.domain.entity.MenuItems
import com.balex.common.domain.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AboutContent(component: AboutComponent) {
    val state by component.model.collectAsState(Dispatchers.Main.immediate)

    com.balex.common.LocalizedContextProvider(languageCode = state.language.lowercase()) {
        val context = LocalLocalizedContext.current
        AboutScreen(component, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(component: AboutComponent, context: Context) {
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
                ShowAboutContent(component, state.user, context)
            }


        }
    }
}

@Composable
fun ShowAboutContent(component: AboutComponent, user: User, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 64.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ShowAboutText(context)
        if (user.nickName != User.DEFAULT_NICK_NAME && user.nickName != NO_USER_SAVED_IN_SHARED_PREFERENCES) {
            ShowDeleteUserButton(user, component)
        }
    }
}

@Composable
fun ShowAboutText(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: ""
    val aboutText = context.getString(R.string.about_text, versionName)

    Text(
        text = aboutText,
        textAlign = TextAlign.Justify,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxSize()
    )
}

@Composable
fun ShowDeleteUserButton(userForDelete: User, component: AboutComponent) {
    var showConfirmation by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { showConfirmation = true },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(2.dp, Color.Red)
        ) {
            Text(text = "Delete account ${userForDelete.nickName}")
        }

        if (showConfirmation) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Enter your username for confirm delete")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    isError = errorMessage,
                    label = {
                        if (errorMessage) Text(
                            "Wrong username",
                            color = Color.Red
                        ) else null
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(2.dp, Color.Red),
                    onClick = {
                        if (inputText.lowercase().trim() == userForDelete.nickName.lowercase().trim()) {
                            scope.launch {
                                component.onClickDeleteAccount(userForDelete.nickName)
                            }
                            showConfirmation = false
                        } else {
                            errorMessage = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WRONG_USERNAME_ERROR_DELAY_TIME_IN_MILLIS)
                                showConfirmation = false
                                errorMessage = false
                                inputText = ""
                            }
                        }
                    }) {
                    Text("OK")
                }
            }
        }
    }
}

const val WRONG_USERNAME_ERROR_DELAY_TIME_IN_MILLIS = 3000L

