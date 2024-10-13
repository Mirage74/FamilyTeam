package com.balex.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.entity.LanguagesList

@Composable
fun SwitchLanguage(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit
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
                        onLanguageChanged(option.symbol.uppercase())
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarOnlyLanguage(
    language: String,
    onLanguageChanged: (String) -> Unit
) {

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.top_bar_height).value.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.LightGray
        ),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier
                    .weight(1f))
                SwitchLanguage(language, onLanguageChanged)
            }
        }
    )
}