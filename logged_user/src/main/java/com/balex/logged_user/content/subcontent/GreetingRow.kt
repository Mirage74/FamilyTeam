package com.balex.logged_user.content.subcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.balex.common.LocalLocalizedContext
import com.balex.common.data.datastore.Storage
import com.balex.common.domain.entity.User
import com.balex.logged_user.R

@Composable
fun GreetingRow(
    userNameFromState: String,
    displayNameFromState: String,
    isPremium: Boolean
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
        if (isPremium) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Premium icon",
                modifier = Modifier
                    .size(32.dp),
                tint = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.width(4.dp))
        }

        if (userNameFromState.isNotEmpty() && userNameFromState != User.DEFAULT_NICK_NAME &&
            userNameFromState != Storage.NO_USER_SAVED_IN_SHARED_PREFERENCES
        ) {
            Text(
                text = context.getString(R.string.hello_text, displayName),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}









