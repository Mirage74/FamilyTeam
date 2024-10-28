package com.balex.common.domain.repository

import com.balex.common.data.local.model.ShopItemDBModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ShopListRepository {

    fun getShopList(): List<ShopItemDBModel>

    fun refreshShopList()

    fun observeShopList(): StateFlow<List<ShopItemDBModel>>

    suspend fun addToShopList(shopItem: ShopItemDBModel)

    suspend fun removeFromShopList(itemId: Long)

}