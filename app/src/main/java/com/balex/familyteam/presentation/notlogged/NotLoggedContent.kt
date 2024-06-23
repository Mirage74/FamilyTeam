package com.balex.familyteam.presentation.notlogged

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

//        Column(
//            modifier = Modifier.fillMaxSize().padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = { /* Действие по переключению языка */ },
//                    modifier = Modifier.size(48.dp) ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_language), // Замените на ваш ресурс иконки
//                        contentDescription = "Переключить язык"
//                    )
//                }
//                Spacer(modifier = Modifier.width(16.dp)) // Пробел между значком и кнопкой
//                Button(
//                    onClick = { /* Действие кнопки */ },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Синий цвет кнопки
//                ) {
//                    Text(
//                        text = "Синяя кнопка",
//                        maxLines = 1, // Ограничение одной строкой
//                        overflow = TextOverflow.Ellipsis // Текст с многоточием, если не помещается
//                    )
//                }
//            }


            when (state.logChooseState) {
                NotLoggedStore.State.LogChooseState.Initial -> {
                    ThreeButtonsScreen(false)
                }

                NotLoggedStore.State.LogChooseState.ErrorLoadingUserData -> {
                    TODO()
                }

                NotLoggedStore.State.LogChooseState.NoSavedUserFound -> {
                    ThreeButtonsScreen(true)
                }
            }
        //}
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
        IconButton(onClick = { /* Действие по переключению языка */ },
            modifier = Modifier.size(148.dp) ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language), // Замените на ваш ресурс иконки
                contentDescription = "Switch language"
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

