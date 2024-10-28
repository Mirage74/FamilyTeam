package com.balex.common.domain.usecases.shopList

import com.balex.common.domain.repository.ShopListRepository
import javax.inject.Inject

class RefreshShopListUseCase @Inject constructor(
    private val repository: ShopListRepository
) {
    operator fun invoke() = repository.refreshShopList()
}