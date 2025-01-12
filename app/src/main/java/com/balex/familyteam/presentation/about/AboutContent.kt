package com.balex.familyteam.presentation.about

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.common.R
import com.balex.common.SwitchLanguage
import com.balex.common.domain.entity.MenuItems
import kotlinx.coroutines.Dispatchers
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
                    .width(144.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = MenuItems.MENU_ITEM_RULES,
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
                ShowAboutText(context)

            }


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

