package com.balex.familyteam

import android.app.Application
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