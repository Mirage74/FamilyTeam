package com.balex.common.domain.entity

import com.balex.common.data.local.model.ShopItemDBModel

data class ShopItems(
    val shopItems: List<ShopItemDBModel> = listOf()
)
