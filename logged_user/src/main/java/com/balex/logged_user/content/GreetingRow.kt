package com.balex.logged_user.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.logged_user.LoggedUserStore
import com.balex.logged_user.R

@Composable
fun GreetingRow(
    state: LoggedUserStore.State
) {
    val context = LocalLocalizedContext.current
    val displayName = state.user.displayName.ifEmpty { state.user.nickName }

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

        Spacer(modifier = Modifier.width(24.dp))

        Icon(
            imageVector = Icons.Default.AddTask,
            contentDescription = "Tasks available"
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableTasksToAdd.toString(),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "FCM available"
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = state.user.availableFCM.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}