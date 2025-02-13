package com.balex.familyteam

import android.app.Application
import android.content.Context
import com.balex.familyteam.di.ApplicationComponent
import com.balex.familyteam.di.DaggerApplicationComponent
import com.google.firebase.FirebaseApp

class FamilyApp : Application() {
    lateinit var applicationComponent: ApplicationComponent
    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.factory().create(this)
        FirebaseApp.initializeApp(this)

    }
}

val Context.appComponent: ApplicationComponent
    get() = when (this) {
        is FamilyApp -> applicationComponent
        else -> this.applicationContext.appComponent
    }