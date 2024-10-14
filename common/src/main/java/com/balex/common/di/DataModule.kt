package com.balex.common.di

import com.balex.common.data.repository.AdminRepositoryImpl
import com.balex.common.data.repository.RegLogRepositoryImpl
import com.balex.common.data.repository.UserRepositoryImpl
import com.balex.common.domain.repository.AdminRepository
import com.balex.common.domain.repository.RegLogRepository
import com.balex.common.domain.repository.UserRepository
import dagger.Binds
import dagger.Module

@Module
interface DataModule {

    @[ApplicationScope Binds]
    fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @[ApplicationScope Binds]
    fun bindRegLogRepository(impl: RegLogRepositoryImpl): RegLogRepository

    @[ApplicationScope Binds]
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

}