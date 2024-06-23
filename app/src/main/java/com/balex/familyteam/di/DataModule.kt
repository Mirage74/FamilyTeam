package com.balex.familyteam.di

import com.balex.familyteam.data.repository.AdminRepositoryImpl
import com.balex.familyteam.data.repository.RegLogRepositoryImpl
import com.balex.familyteam.data.repository.UserRepositoryImpl
import com.balex.familyteam.domain.repository.AdminRepository
import com.balex.familyteam.domain.repository.RegLogRepository
import com.balex.familyteam.domain.repository.UserRepository
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