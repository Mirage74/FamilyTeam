package com.balex.common.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_items")
data class ShopItemDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = System.currentTimeMillis(),
    val description: String = DEFAULT_SHOP_ITEM_DESCRIPTION,
) {
    companion object {
        const val DEFAULT_SHOP_ITEM_DESCRIPTION = "DEFAULT_SHOP_ITEM_DESCRIPTION"
    }
}
