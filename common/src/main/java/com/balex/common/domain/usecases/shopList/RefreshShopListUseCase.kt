package com.balex.common.domain.usecases.shopList

import com.balex.common.domain.repository.ShopListRepository
import javax.inject.Inject

@Suppress("unused")
class RefreshShopListUseCase @Inject constructor(
    private val repository: ShopListRepository
) {
    operator fun invoke() = repository.refreshShopList()
}