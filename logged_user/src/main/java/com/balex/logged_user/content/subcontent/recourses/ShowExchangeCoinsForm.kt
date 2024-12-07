package com.balex.logged_user.content.subcontent.recourses

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.common.R
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun ShowExchangeCoinsForm(
    state: LoggedUserStore.State,
    component: LoggedUserComponent

) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var selectedCoins by remember { mutableIntStateOf(state.user.teamCoins) }
        var selectedOption by remember { mutableStateOf("Tasks") }
        val rateResource = if (selectedOption == "Tasks") {
            context.resources.getInteger(R.integer.rate_one_coin_to_task)
        } else {
            context.resources.getInteger(R.integer.rate_one_coin_to_FCM)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 64.dp)
        ) {
            Text("Buy Coins")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_coin),
                contentDescription = "Coin icon",
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = " Sell ",
                style = MaterialTheme.typography.bodyLarge
            )

            DropdownMenuWithItems(
                items = (1..state.user.teamCoins).toList().reversed(),
                selectedValue = selectedCoins,
                onItemSelected = { selectedCoins = it },
                isEnabled = state.user.teamCoins > 0
            )

            Text(
                text = "Coins",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(
                text = "Rate: $rateResource",
                style = MaterialTheme.typography.bodyLarge
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

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

            val result = selectedCoins * rateResource
            Text(
                text = " Buy ",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "$result",
                style = MaterialTheme.typography.headlineMedium
            )

            DropdownMenuWithItems(
                items = listOf("Tasks", "Reminders"),
                selectedValue = selectedOption,
                onItemSelected = { selectedOption = it }
            )
        }


        Button(
            onClick = {
                val newCoins = state.user.teamCoins - selectedCoins
                val newTasks = if (selectedOption == "Tasks") {
                    state.user.availableTasksToAdd + selectedCoins * rateResource
                } else {
                    state.user.availableTasksToAdd
                }
                val newReminders = if (selectedOption == "Reminders") {
                    state.user.availableFCM + selectedCoins * rateResource
                } else {
                    state.user.availableFCM
                }
                component.onConfirmExchangeClicked(newCoins, newTasks, newReminders)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 64.dp),
            enabled = state.user.teamCoins > 0
        ) {
            Text("Exchange")
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
            Text(
                text = selectedValue.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "arrow",
                modifier = Modifier.size(16.dp)
            )
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
