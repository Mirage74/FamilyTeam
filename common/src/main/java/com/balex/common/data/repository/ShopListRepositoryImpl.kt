package com.balex.common.data.repository

import com.balex.common.data.local.db.ShopItemsDao
import com.balex.common.data.local.model.ShopItemDBModel
import com.balex.common.domain.repository.ShopListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShopListRepositoryImpl @Inject constructor(
    private val shopItemsDao: ShopItemsDao
) : ShopListRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    private var shopItems: List<ShopItemDBModel> = listOf()

    private val isShopItemsListNeedRefreshFlow = MutableSharedFlow<Unit>(replay = 1)

    override fun observeShopList(): StateFlow<List<ShopItemDBModel>> =
        combine(
            shopItemsDao.getShopList(),
            isShopItemsListNeedRefreshFlow
        ) { items, _ ->
            shopItems = items
            shopItems
        }
            .takeWhile { coroutineScope.isActive }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    override fun getShopList(): List<ShopItemDBModel> {
        return shopItems
    }

    override fun refreshShopList() {
        coroutineScope.launch {
            isShopItemsListNeedRefreshFlow.emit(Unit)
        }
    }


    override suspend fun addToShopList(shopItem: ShopItemDBModel) {
        shopItemsDao.addToShopList(shopItem)
    }

    override suspend fun removeFromShopList(itemId: Long) {
        shopItemsDao.removeFromShopList(itemId)
    }
}