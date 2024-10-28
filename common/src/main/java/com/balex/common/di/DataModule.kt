package com.balex.common.di

import android.content.Context
import com.balex.common.data.local.db.ShopDatabase
import com.balex.common.data.local.db.ShopItemsDao
import com.balex.common.data.repository.AdminRepositoryImpl
import com.balex.common.data.repository.RegLogRepositoryImpl
import com.balex.common.data.repository.ShopListRepositoryImpl
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.repository.AdminRepository
import com.balex.common.domain.repository.RegLogRepository
import com.balex.common.domain.repository.ShopListRepository
import com.balex.common.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @[ApplicationScope Binds]
    fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @[ApplicationScope Binds]
    fun bindRegLogRepository(impl: RegLogRepositoryImpl): RegLogRepository

    @[ApplicationScope Binds]
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @[ApplicationScope Binds]
    fun bindShopListRepository(impl: ShopListRepositoryImpl): ShopListRepository

    companion object {

        @[ApplicationScope Provides]
        fun provideShopDatabase(context: Context): ShopDatabase {
            return ShopDatabase.getInstance(context)
        }

        @[ApplicationScope Provides]
        fun provideShopItemsDao(database: ShopDatabase): ShopItemsDao {
            return database.shopItemsDao()
        }
    }
}