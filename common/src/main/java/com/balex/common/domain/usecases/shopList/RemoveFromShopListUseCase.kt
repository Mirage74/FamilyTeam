package com.balex.common.domain.usecases.shopList

import com.balex.common.domain.repository.ShopListRepository
import javax.inject.Inject

class RemoveFromShopListUseCase @Inject constructor(
    private val repository: ShopListRepository
) {
    suspend operator fun invoke(itemId: Long) = repository.removeFromShopList(itemId)
}