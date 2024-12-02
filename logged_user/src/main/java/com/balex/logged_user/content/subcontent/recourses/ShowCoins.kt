package com.balex.logged_user.content.subcontent.recourses

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.balex.logged_user.LoggedUserStore

@Composable
fun ShowAvailableResources(state: LoggedUserStore.State, onExchangeCoinsClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painterResource(id = com.balex.common.R.drawable.ic_coin),
            contentDescription = "Tasks available",
            modifier = Modifier
                .size(48.dp)
                .clickable { onExchangeCoinsClicked() }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.teamCoins.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.AddTask,
            contentDescription = "Tasks available",
            modifier = Modifier
                .size(32.dp)
                .clickable { onExchangeCoinsClicked() }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableTasksToAdd.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "FCM available",
            modifier = Modifier
                .size(32.dp)
                .clickable { onExchangeCoinsClicked() }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableFCM.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}