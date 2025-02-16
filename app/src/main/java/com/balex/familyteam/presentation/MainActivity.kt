package com.balex.familyteam.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.arkivanov.decompose.defaultComponentContext
import com.balex.familyteam.appComponent
import com.balex.familyteam.presentation.root.DefaultRootComponent
import com.balex.familyteam.presentation.root.RootContent
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject


class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionsIsGranted()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationaleDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    }

    private fun permissionsIsGranted() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            val component = rootComponentFactory.create(defaultComponentContext())
            setContent {
                RootContent(
                    component,
                    this,
                    token
                )
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionsIsGranted()
        } else {
            val component = rootComponentFactory.create(defaultComponentContext())
            setContent {
                RootContent(
                    component,
                    this,
                    NO_NOTIFICATION_PERMISSION_GRANTED
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission for notifications")
            .setMessage("This application requires permission to send notifications to remind you of your scheduled tasks at the time you specify. Please grant this permission.")
            .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("No thanks") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        const val NO_NOTIFICATION_PERMISSION_GRANTED = "NO_NOTIFICATION_PERMISSION_GRANTED"
    }
}

