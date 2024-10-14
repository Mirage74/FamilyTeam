package com.balex.familyteam.di

import com.balex.common.di.ApplicationScope
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.regadmin.PhoneFirebaseRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface RegPhoneFirebaseModule {

    @[ApplicationScope Binds]
    fun bindPhoneFirebaseRepository(impl: PhoneFirebaseRepositoryImpl): PhoneFirebaseRepository
}