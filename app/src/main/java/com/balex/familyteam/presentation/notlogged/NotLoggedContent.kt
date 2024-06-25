package com.balex.familyteam.presentation.notlogged

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.LocalLocalizedContext
import com.balex.familyteam.LocalizedContextProvider
import com.balex.familyteam.R
import com.balex.familyteam.domain.entity.LanguagesList

private val LANGUAGE_LIST_TEXT_SIZE = 20.sp
private val BUTTON_TEXT_SIZE = 20.sp

@Composable
fun NotLoggedContent(component: NotLoggedComponent) {
    val state by component.model.collectAsState()
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = colorResource(R.color.orange_gold),

        ) {
        LocalizedContextProvider(languageCode = state.language.lowercase()) {

            when (state.logChooseState) {
                NotLoggedStore.State.LogChooseState.Initial -> {
                    LanguageChooserScreen(false, language = state.language)
                }

                NotLoggedStore.State.LogChooseState.ErrorLoadingUserData -> {
                    TODO()
                }

                NotLoggedStore.State.LogChooseState.NoSavedUserFound -> {
                    LanguageChooserScreen(
                        true,
                        component,
                        state.language
                    )

                    LanguageChooserScreen(true, component, state.language)
                }
            }
        }
    }
}

@Composable
fun LanguageChooserScreen(
    isEnabled: Boolean,
    component: NotLoggedComponent? = null,
    language: String
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SwitchLanguage(language, component)
            ThreeButtonsScreen(isEnabled = isEnabled)
        }
    }



@Composable
fun SwitchLanguage(
    currentLanguage: String,
    component: NotLoggedComponent? = null
) {
    var isChooseOptionDropMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val languages = LanguagesList().languages

    Box(
        modifier = Modifier
            .padding(16.dp, 0.dp, 0.dp, 0.dp)
    ) {

        Row(
        ) {
            Spacer(modifier = Modifier.weight(1f))


            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(4.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(align = Alignment.End)
                    .background(Color.White)
                    .border(width = 1.dp, color = Color.Black),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End

            ) {


                IconButton(
                    onClick = {
                        isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_language),
                        contentDescription = "Switch language"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier.clickable {
                        isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
                    },
                    text = currentLanguage
                )
                //Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = {
                    isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
                }) {
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Switch language")
                }
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
                                fontSize = LANGUAGE_LIST_TEXT_SIZE
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeButtonsScreen(isEnabled: Boolean) {
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

            Button(
                onClick = { /* TODO: Handle button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = isEnabled
            ) {
                Text(
                    fontSize = BUTTON_TEXT_SIZE,
                    text = regAdmText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = { /* TODO: Handle button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = isEnabled
            ) {
                Text(
                    fontSize = BUTTON_TEXT_SIZE,
                    text = logAdmText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis

                )
            }
            Button(
                onClick = { /* TODO: Handle button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = isEnabled
            ) {
                Text(
                    fontSize = BUTTON_TEXT_SIZE,
                    text = logUserText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

