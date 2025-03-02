package com.balex.logged_user.content.subcontent.recourses

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.balex.common.SwitchLanguage
import com.balex.common.data.repository.BillingRepositoryImpl
import com.balex.common.domain.entity.MenuItems
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R
import kotlinx.coroutines.launch
import com.balex.common.R as commonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowExchangeCoinsForm(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    activity: Activity,
    context: Context
) {
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

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = Color.Black
                )

                Text(
                    text = menuItems.getItem(MenuItems.MENU_ITEM_ABOUT),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            scope.launch {
                                drawerState.close()
                                component.onClickAbout()
                            }
                        },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
            }
        }
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
            ExchangeCoinsFormContent(state, component, activity, context)
        }
    }
}


@Composable
fun ExchangeCoinsFormContent(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    activity: Activity,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        SectionBuyCoins( { component.onBuyCoinsClicked(activity) }, context)

        StandardHorizontalSpacer()
        DashedHorizontalLine()

        SectionExchangeCoins(state, component, context)

        StandardHorizontalSpacer()
        DashedHorizontalLine()

        SectionBuyPremiumAccount(state, component, context)

    }
}

@Composable
fun SectionBuyCoins(onClickedBuyCoins: () -> Unit, context: Context) {
    Button(
        onClick = { onClickedBuyCoins() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        val text = context.getString(R.string.buy_coins_button_text)
        Text(text)
    }
}


@Composable
fun SectionExchangeCoins(state: LoggedUserStore.State, component: LoggedUserComponent, context: Context) {

    val coinName = ( context.getString(commonR.string.coins) ).trim()
    val taskName = ( context.getString(commonR.string.tasks) ).trim()
    val reminderName = ( context.getString(commonR.string.reminders) ).trim()

    var selectedCoinsQuantity by remember { mutableIntStateOf(state.user.teamCoins) }
    var selectedOption by remember(taskName) { mutableStateOf(taskName) }
    val rateResource = if (selectedOption == taskName) {
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
            text = context.getString(commonR.string.sell),
            style = MaterialTheme.typography.bodyLarge
        )

        DropdownMenuWithItems(
            items = (1..state.user.teamCoins).toList().reversed(),
            selectedValue = selectedCoinsQuantity,
            onItemSelected = { selectedCoinsQuantity = it },
            isEnabled = state.user.teamCoins > 0
        )

        Text(
            text = coinName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val rateText = context.getString(commonR.string.rate)
        Text(
            text = "$rateText: $rateResource",
            style = MaterialTheme.typography.bodyLarge
        )

    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val icon = if (selectedOption == taskName) {
            Icons.Default.AddTask
        } else {
            Icons.AutoMirrored.Filled.Message
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        val result = selectedCoinsQuantity * rateResource
        Text(
            text = context.getString(commonR.string.buy),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "$result",
            style = MaterialTheme.typography.headlineMedium
        )

        DropdownMenuWithItems(
            items = listOf(taskName, reminderName),
            selectedValue = selectedOption,
            onItemSelected = { selectedOption = it }
        )
    }


    Button(
        onClick = {
            val newCoins = state.user.teamCoins - selectedCoinsQuantity
            val newTasks = if (selectedOption == taskName) {
                state.user.availableTasksToAdd + selectedCoinsQuantity * rateResource
            } else {
                state.user.availableTasksToAdd
            }
            val newReminders = if (selectedOption == reminderName) {
                state.user.availableFCM + selectedCoinsQuantity * rateResource
            } else {
                state.user.availableFCM
            }
            component.onConfirmExchangeClicked(newCoins, newTasks, newReminders)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        enabled = state.user.teamCoins > 0
    ) {
        val text = context.getString(R.string.exchange_button_text)
        Text(text)
    }
}

@Composable
fun SectionBuyPremiumAccount(state: LoggedUserStore.State, component: LoggedUserComponent, context: Context) {
    val oneMonth = BillingRepositoryImpl.Companion.PremiumStatus.ONE_MONTH
    val oneYear = BillingRepositoryImpl.Companion.PremiumStatus.ONE_YEAR
    val unlimited = BillingRepositoryImpl.Companion.PremiumStatus.UNLIMITED

    Column(
        modifier = Modifier.padding(start = 32.dp)
    ) {

        val appContext = context.applicationContext

        val coinName = ( context.getString(commonR.string.coins) ).trim()

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
                    appContext.resources.getInteger(commonR.integer.premium_account_one_month_cost) + " $coinName )"
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
                    appContext.resources.getInteger(commonR.integer.premium_account_one_year_cost) + " $coinName )"
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
                    appContext.resources.getInteger(commonR.integer.premium_account_unlimited_cost) + " $coinName )"
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
                .height(96.dp)
                .padding(horizontal = 16.dp),
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
    Spacer(modifier = Modifier.height(8.dp))
}