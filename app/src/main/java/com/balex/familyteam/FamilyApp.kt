package com.balex.familyteam

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.balex.familyteam.di.ApplicationComponent
import com.balex.familyteam.di.DaggerApplicationComponent
import com.google.firebase.FirebaseApp
import java.lang.ref.WeakReference

class FamilyApp : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.factory().create(this)
        FirebaseApp.initializeApp(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                setCurrentActivity(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                setCurrentActivity(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                setCurrentActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }

    companion object {
        private var currentActivityRef: WeakReference<Activity>? = null

        val currentActivity: Activity?
            get() = currentActivityRef?.get()

        fun setCurrentActivity(activity: Activity) {
            currentActivityRef = WeakReference(activity)
        }
    }


}