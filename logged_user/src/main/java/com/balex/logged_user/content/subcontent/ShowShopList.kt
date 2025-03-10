package com.balex.logged_user.content.subcontent


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balex.logged_user.LoggedUserComponent
import com.balex.logged_user.LoggedUserStore

@Composable
fun ShowShopList(
    state: LoggedUserStore.State,
    component: LoggedUserComponent,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(bottom = 64.dp)
    ) {
        items(state.shopItemsList.shopItems, key = { it.id }) { shopItem ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(bottom = 16.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 100f) {
                                    component.onClickDeleteShopItem(shopItem.id)
                                    offsetX = 0f
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX = (offsetX + dragAmount).coerceAtLeast(0f)
                            }
                        )
                    }
                    .offset { IntOffset(animatedOffsetX.dp.roundToPx(), 0) }
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()

                        drawRect(
                            color = Color.Black,
                            style = Stroke(width = strokeWidth)
                        )
                    },

                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shopItem.description,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(9f),
                    fontSize = 20.sp
                )


                IconButton(
                    onClick = {
                        component.onClickDeleteShopItem(shopItem.id)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = Color.Gray,
                    )
                }

            }
        }
    }
}

