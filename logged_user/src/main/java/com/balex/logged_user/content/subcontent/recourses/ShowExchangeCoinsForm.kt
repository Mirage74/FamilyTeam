package com.balex.logged_user.content.subcontent.recourses

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.balex.common.data.repository.BillingRepositoryImpl
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import com.balex.common.R as commonR

@Composable
fun ShowExchangeCoinsForm(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    activity: Activity
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        SectionBuyCoins { component.onBuyCoinsClicked(activity) }

        StandardHorizontalSpacer()
        DashedHorizontalLine()

        SectionExchangeCoins(state, component)

        StandardHorizontalSpacer()
        DashedHorizontalLine()

        SectionBuyPremiumAccount(state, component)

    }
}

@Composable
fun SectionBuyCoins(onClickedBuyCoins: () -> Unit) {
    val context = LocalContext.current
    Button(
        onClick = { onClickedBuyCoins() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 64.dp)
    ) {
        val text = context.getString(R.string.buy_coins_button_text)
        Text(text)
    }
}


@Composable
fun SectionExchangeCoins(state: LoggedUserStore.State, component: LoggedUserComponent) {
    val context = LocalContext.current

    var selectedCoins by remember { mutableIntStateOf(state.user.teamCoins) }
    var selectedOption by remember { mutableStateOf("Tasks") }
    val rateResource = if (selectedOption == "Tasks") {
        context.resources.getInteger(commonR.integer.rate_one_coin_to_task)
    } else {
        context.resources.getInteger(commonR.integer.rate_one_coin_to_FCM)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = commonR.drawable.ic_coin),
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
            .height(48.dp)
            .padding(horizontal = 64.dp),
        enabled = state.user.teamCoins > 0
    ) {
        val text = context.getString(R.string.exchange_button_text)
        Text(text)
    }
}

@Composable
fun SectionBuyPremiumAccount(state: LoggedUserStore.State, component: LoggedUserComponent) {
    val context = LocalContext.current
    val oneMonth = BillingRepositoryImpl.Companion.PremiumStatus.ONE_MONTH
    val oneYear = BillingRepositoryImpl.Companion.PremiumStatus.ONE_YEAR
    val unlimited = BillingRepositoryImpl.Companion.PremiumStatus.UNLIMITED

    Column(
        modifier = Modifier.padding(start = 32.dp)
    ) {

        val appContext = context.applicationContext

        var defaultPremiumStatus = BillingRepositoryImpl.Companion.PremiumStatus.NO_PREMIUM
        if (state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_one_month_cost)) {
            defaultPremiumStatus = oneMonth
        }

        if (state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_one_year_cost)) {
            defaultPremiumStatus = oneYear
        }

        var selectedOption by remember {
            mutableStateOf(defaultPremiumStatus)
        }

        Text(
            text = context.getString(R.string.buy_premium_account_text),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isEnabled = state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_one_month_cost)
            RadioButton(
                selected = selectedOption == oneMonth,
                onClick = { selectedOption = oneMonth },
                enabled = isEnabled
            )
            val text = context.getString(R.string.premium_one_month) + " (" +
                    appContext.resources.getInteger(commonR.integer.premium_account_one_month_cost) + " coins )"
            Text(
                text = text,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isEnabled = state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_one_year_cost)
            RadioButton(
                selected = selectedOption == oneYear,
                onClick = { selectedOption = oneYear },
                enabled = state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_one_year_cost)
            )
            val text = context.getString(R.string.premium_one_year) + " (" +
                    appContext.resources.getInteger(commonR.integer.premium_account_one_year_cost) + " coins )"
            Text(
                text = text,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val isEnabled = state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_unlimited_cost)
            RadioButton(
                selected = selectedOption == unlimited,
                onClick = { selectedOption = unlimited },
                enabled = state.user.teamCoins >= context.resources.getInteger(commonR.integer.premium_account_unlimited_cost)
            )
            val text = context.getString(R.string.premium_unlimited) + " (" +
                    appContext.resources.getInteger(commonR.integer.premium_account_unlimited_cost) + " coins )"
            Text(
                text = text,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        val cost = when (selectedOption) {
            oneMonth -> context.resources.getInteger(commonR.integer.premium_account_one_month_cost)
            oneYear -> context.resources.getInteger(commonR.integer.premium_account_one_year_cost)
            else -> context.resources.getInteger(commonR.integer.premium_account_unlimited_cost)
        }

        Button(
            onClick = { selectedOption.let { component.onBuyPremiumClicked(it) } },
            enabled = cost <= state.user.teamCoins,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 64.dp),
        ) {
            Text(text = context.getString(R.string.buy_premium_account_button_text))
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

@Composable
fun DashedHorizontalLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                val dashWidth = 10.dp.toPx()
                val gapWidth = 5.dp.toPx()
                val lineWidth = size.width
                var startX = 0f

                while (startX < lineWidth) {
                    val endX = (startX + dashWidth).coerceAtMost(lineWidth)
                    drawLine(
                        color = Color.Gray,
                        start = Offset(startX, center.y),
                        end = Offset(endX, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    startX += dashWidth + gapWidth
                }
            }
    )
}

@Composable
fun StandardHorizontalSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}