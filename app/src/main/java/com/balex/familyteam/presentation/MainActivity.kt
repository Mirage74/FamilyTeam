package com.balex.familyteam.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.balex.familyteam.FamilyApp
import com.balex.familyteam.appComponent
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.root.DefaultRootComponent
import com.balex.familyteam.presentation.root.RootContent
import javax.inject.Inject

//val auth: FirebaseAuth = FirebaseAuth.getInstance()

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory



    override fun onCreate(savedInstanceState: Bundle?) {
        //(applicationContext as FamilyApp).applicationComponent.inject(this)
        appComponent.inject(this)




        super.onCreate(savedInstanceState)


        setContent {
            RootContent(component = rootComponentFactory.create(defaultComponentContext()), this)
        }
    }
}

//        private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//            if (isGranted) {
//                val t = 6
//            } else {
//                val t = 6
//            }
//        }


//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
//                return@addOnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            // Log and toast
//            Log.d("MainActivity", "FCM Registration token: $token")
//        }
//
//        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)



//@Composable
//fun MyApp() {
//    Scaffold(
//        modifier = Modifier
//            .fillMaxSize()
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            var currentLanguage by remember { mutableStateOf("ru") }
//           Row (
//               modifier = Modifier
//                   .fillMaxWidth()
//           ) {
//
//
//               LocalizedContextProvider(languageCode = currentLanguage) {
//                   MyScreen(onLanguageChange = { newLanguage ->
//                       currentLanguage = newLanguage
//                   })
//               }
//           }
//           Row (
//               modifier = Modifier
//                   .fillMaxWidth()
//           ){
//               NotificationHandler()
//           }
//
//       }
//
//    }
//}
//
//


//@Composable
//fun NotificationHandler() {
//    var notification by remember { mutableStateOf("No notifications") }
//
//    LaunchedEffect(Unit) {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                notification = "Fetching FCM registration token failed"
//                return@addOnCompleteListener
//            }
//
//            val token = task.result
//            notification = "FCM Registration Token: $token"
//        }
//    }
//
//    Text(text = notification)
//}
//
