package com.balex.familyteam.di

import android.content.Context
import com.balex.common.di.ApplicationScope
import com.balex.common.di.DataModule
import com.balex.common.di.PresentationModule
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        PresentationModule::class,
        RegPhoneFirebaseModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: com.balex.familyteam.presentation.MainActivity)

    fun inject(firebaseMessagingService: com.balex.familyteam.MyFirebaseMessagingService)

    @Suppress("unused")
    fun provideDefaultRegAdminComponentFactory(): DefaultRegAdminComponent.Factory

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance context: Context
        ): ApplicationComponent
    }
}