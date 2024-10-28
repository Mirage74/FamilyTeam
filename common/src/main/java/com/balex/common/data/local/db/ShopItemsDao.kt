package com.balex.common.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.balex.common.data.local.model.ShopItemDBModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopItemsDao {
    @Query("SELECT * FROM shop_items")
    fun getShopList(): Flow<List<ShopItemDBModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToShopList(shopItemDBModel: ShopItemDBModel)

    @Query("DELETE FROM shop_items WHERE id=:shopItemId")
    suspend fun removeFromShopList(shopItemId: Long)
}