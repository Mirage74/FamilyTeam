package com.balex.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, dimensionResource(id = R.dimen.top_bar_height).value.dp, 0.dp, 0.dp)
    ) {
        val textSize = dimensionResource(id = R.dimen.hamburger_text_size).value.sp
        items.forEach { item ->
            Text(
                text = item,
                fontSize = textSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        onItemClick(item)
                    }
            )
        }
    }
}