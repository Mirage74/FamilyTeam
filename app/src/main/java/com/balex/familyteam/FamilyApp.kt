package com.balex.familyteam

import android.app.Application
import com.google.firebase.FirebaseApp

class FamilyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}