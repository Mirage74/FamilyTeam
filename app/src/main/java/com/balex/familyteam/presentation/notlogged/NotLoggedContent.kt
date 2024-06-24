package com.balex.familyteam.presentation.notlogged

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.familyteam.R
import com.balex.familyteam.presentation.ui.theme.FamilyTeamTheme


@Composable
fun NotLoggedContent(component: NotLoggedComponent) {
    val state by component.model.collectAsState()
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        //color = MaterialTheme.colorScheme.background
        color = colorResource(R.color.orange_gold)
    ) {

        when (state.logChooseState) {
            NotLoggedStore.State.LogChooseState.Initial -> {
                LanguageChooserScreen(false)
            }

            NotLoggedStore.State.LogChooseState.ErrorLoadingUserData -> {
                TODO()
            }

            NotLoggedStore.State.LogChooseState.NoSavedUserFound -> {
                LanguageChooserScreen(true)
            }
        }
        //}
    }
}

@Composable
fun LanguageChooserScreen(isEbabled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SwitchLanguage()
        ThreeButtonsScreen(isEbabled = isEbabled)
    }
}


@Composable
fun SwitchLanguage() {
    var isChooseOptionDropMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val languages = listOf("English", "Spanish", "French")

    Box(
        modifier = Modifier
            .padding(16.dp, 0.dp, 0.dp, 0.dp)
//            .background(Color.White)
//            .border(width = 1.dp, color = Color.Black),
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
                onClick = { /* Действие по переключению языка */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_language), // Замените на ваш ресурс иконки
                    contentDescription = "Переключить язык"
                )
            }
                Spacer(modifier = Modifier.width(4.dp)) // Пробел между значком и кнопкой
            Text(
                modifier = Modifier.clickable {
                    isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
                },
                text = "EN"
            )
            //Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = {
                isChooseOptionDropMenuExpanded = !isChooseOptionDropMenuExpanded
            }) {
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            //expanded = terminalState.value.isChooseOptionDropMenuExpanded,
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
                        val selectedLanguage = when (option) {
                            "English" -> "en"
                            "Spanish" -> "es"
                            "French" -> "fr"
                            else -> "en"
                        }
                        isChooseOptionDropMenuExpanded = false

//                        onDropDownMenuStateChanged(
//                            dropDownMenuState.value.copy(
//                                selectedOption = option,
//                                selectedAsset = selectedLanguage
//                            )
//                        )
                    },
                    text = {
                        Text(
                            text = option
                            //,
                            //fontSize = LIST_OPTIONS_TEXT_SIZE
                        )
                    },
                )
            }
        }
    }
}
}

@Composable
fun ThreeButtonsScreen(isEbabled: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
//        IconButton(onClick = { /* Действие по переключению языка */ },
//            modifier = Modifier.size(148.dp) ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_language), // Замените на ваш ресурс иконки
//                contentDescription = "Switch language"
//            )
//        }
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEbabled
        ) {
            Text(
                fontSize = 20.sp,
                text = stringResource(R.string.reg_adm),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEbabled
        ) {
            Text(
                fontSize = 20.sp,
                text = stringResource(R.string.log_adm),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis

            )
        }
        Button(
            onClick = { /* TODO: Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = isEbabled
        ) {
            Text(
                fontSize = 20.sp,
                text = stringResource(R.string.log_user),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

