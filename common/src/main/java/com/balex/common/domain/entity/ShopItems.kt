package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ShopItems(
    @SerializedName("shopItems")
    val shopItems: List<ShopItem> = listOf()
)
