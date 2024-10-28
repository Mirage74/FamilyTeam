package com.balex.common.domain.usecases.shopList

import com.balex.common.data.local.model.ShopItemDBModel
import com.balex.common.domain.repository.ShopListRepository
import javax.inject.Inject

class AddToShopListUseCase @Inject constructor(
    private val repository: ShopListRepository
) {
    suspend operator fun invoke(shopItem: ShopItemDBModel) = repository.addToShopList(shopItem)
}