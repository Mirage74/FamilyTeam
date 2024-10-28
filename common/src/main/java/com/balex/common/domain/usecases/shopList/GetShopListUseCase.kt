package com.balex.common.domain.usecases.shopList

import com.balex.common.domain.repository.ShopListRepository
import javax.inject.Inject

class GetShopListUseCase @Inject constructor(
    private val repository: ShopListRepository
) {
    operator fun invoke() = repository.getShopList()
}