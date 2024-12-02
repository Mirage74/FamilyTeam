package com.balex.logged_user.content.subcontent

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.LocalLocalizedContext
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.logged_user.content.subcontent.recourses.ShowAvailableResources

@Composable
fun GreetingRow(
    userNameFromState: String,
    displayNameFromState: String
) {
    val context = LocalLocalizedContext.current
    val displayName = displayNameFromState.ifEmpty { userNameFromState }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.hello_text, displayName),
            style = MaterialTheme.typography.headlineMedium
        )
    }


//    if (!state.isExchangeCoinsClicked) {
//        ShowAvailableResources(state, onExchangeCoinsClicked)
//    } else {
//        ShowExchangeCoinsForm(state, onExchangeCoinsClicked, onBuyCoinsClicked)
//    }


}




@Composable
fun ShowExchangeCoinsForm(
    state: LoggedUserStore.State,
    onExchangeCoinsClicked: () -> Unit,
    onBuyCoinsClicked: () -> Unit,

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.isBuyCoinsClicked) {
            Button(
                onClick = { onBuyCoinsClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy Coins")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = com.balex.common.R.drawable.ic_coin),
                    contentDescription = "Coin icon",
                    modifier = Modifier.size(32.dp)
                )

                var selectedCoins by remember { mutableIntStateOf(state.user.teamCoins) }
                DropdownMenuWithItems(
                    items = (1..state.user.teamCoins).toList().reversed(),
                    selectedValue = selectedCoins,
                    onItemSelected = { selectedCoins = it },
                    isEnabled = state.user.teamCoins > 0
                )

                Text(
                    text = "Exchange",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )

                var selectedOption by remember { mutableStateOf("Tasks") }
                val icon = if (selectedOption == "Tasks") {
                    Icons.Default.AddTask
                } else {
                    Icons.AutoMirrored.Filled.Message
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                DropdownMenuWithItems(
                    items = listOf("Tasks", "Reminders"),
                    selectedValue = selectedOption,
                    onItemSelected = { selectedOption = it }
                )
            }

            Button(
                onClick = { onExchangeCoinsClicked() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.user.teamCoins > 0
            ) {
                Text("Exchange")
            }
        } else {
            BuyCoinsForm()
        }
    }
}

@Composable
fun <T> DropdownMenuWithItems(
    items: List<T>,
    selectedValue: T,
    onItemSelected: (T) -> Unit,
    isEnabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { if (isEnabled) expanded = true },
            enabled = isEnabled
        ) {
            Text(text = selectedValue.toString())
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.toString()) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    enabled = isEnabled
                )
            }
        }
    }
}

@Composable
fun BuyCoinsForm() {

}

//@Composable
//fun ShowExchangeCoinsForm(state: LoggedUserStore.State, onBuyCoinsClicked: () -> Unit, onExchangeCoinsClicked: () -> Unit) {
//
//        if (!state.isBuyCoinsClicked) {
//            строка с кнопкой "BuyCoins", при нажатии вызов onBuyCoinsClicked
//            строка с
//                    1) Image(            painterResource(id = com.balex.common.R.drawable.ic_coin),
//                    2) выпадающим списком, где значения от state.user.teamCoins до 1 по убыванию. по умолчанию state.user.teamCoins
//            если state.user.teamCoins == 0, то указан 0 и недоступно для выбора.
//            3) Text(text = "Exchange")
//            4) если в п.5. выбран Tasks, то Icon(
//            imageVector = Icons.Default.AddTask,
//            иначе
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.Message,
//
//            5) выпадающий список с текстом "Tasks" (по умолчанию) или "Reminders"
//            6) кнопка "Exchange" при нажатии вызов onExchangeCoinsClicked
//
//}
//}