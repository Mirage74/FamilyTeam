package com.balex.familyteam.di

import android.content.Context
import com.balex.familyteam.presentation.MainActivity
import com.balex.familyteam.presentation.regadmin.DefaultRegAdminComponent
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        PresentationModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun provideDefaultRegAdminComponentFactory(): DefaultRegAdminComponent.Factory

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance context: Context
        ): ApplicationComponent
    }
}