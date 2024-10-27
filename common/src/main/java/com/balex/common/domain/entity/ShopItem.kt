package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ShopItem(
    @SerializedName("id")
    val id: Long = System.currentTimeMillis(),
    @SerializedName("description")
    val description: String = DEFAULT_SHOP_ITEM_DESCRIPTION,
) {
    companion object {
        const val DEFAULT_SHOP_ITEM_DESCRIPTION = "DEFAULT_SHOP_ITEM_DESCRIPTION"
    }
}
