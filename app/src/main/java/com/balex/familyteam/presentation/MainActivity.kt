package com.balex.familyteam.presentation

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balex.familyteam.LocalLocalizedContext
import com.balex.familyteam.LocalizedContextProvider
import com.balex.familyteam.R
import com.google.firebase.FirebaseException
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

val auth: FirebaseAuth = FirebaseAuth.getInstance()
val TAG = "Family_TAG"
class MainActivity : ComponentActivity() {


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val t = 6
            } else {
                val t = 6
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


            //loginUser("balexvicx@gmail.com", "123456")
//        registerUser("balexvicx@gmail.com", "123456")

//        val phoneNumber = "+41789424340" // Формат номера телефона должен начинаться с "+" и кода страны
//        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
//            .setPhoneNumber(phoneNumber)       // Номер телефона для верификации
//            .setTimeout(60L, TimeUnit.SECONDS) // Время ожидания SMS-кода
//            .setActivity(this)                 // Текущая активити, где отображается входной экран
//            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                    // Автоматический вход в систему после успешной верификации
//                }
//
//                override fun onVerificationFailed(exception: FirebaseException) {
//                    // Обработка ошибок верификации номера телефона
//                    Log.e(TAG, "Verification failed: ${exception.message}")
//                }
//
//                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                    val smsCode = "123456"
//                    val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
//
//                    FirebaseAuth.getInstance().signInWithCredential(credential)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                // Вход в систему успешен после верификации номера телефона
//                            } else {
//                                // Обработка ошибок входа после верификации номера телефона
//                                val exception = task.exception
//                                Log.e(TAG, "Sign in failed: ${exception?.message}")
//                            }
//                        }
//                }
//            })
//            .build()
//
//        PhoneAuthProvider.verifyPhoneNumber(options)




        setContent {
            //MyApp()
        }
    }
}

fun registerUser(email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Регистрация успешна
                val user = auth.currentUser
//                user!!.sendEmailVerification()
//                    .addOnCompleteListener { emailVerification->
//                        if (emailVerification.isSuccessful) {
//                            Log.d(TAG, "Email sent.")
//                        }
//                    }

                val t = 6
                // Вы можете сохранить информацию о пользователе в Firestore или Realtime Database
            } else {
                // Регистрация не удалась, показать сообщение пользователю
                task.exception?.message?.let {
                    Log.e("Registration Error", it)
                }
            }
        }
}


fun loginUser(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Авторизация успешна
                val user = auth.currentUser
                val t = user?.email
                val emailVerified = user?.isEmailVerified
                val f = 5
                // Продолжить выполнение, например, перейти к главному экрану приложения
            } else {
                // Авторизация не удалась, показать сообщение пользователю
                task.exception?.message?.let {
                    Log.e("Login Error", it)
                }
            }
        }
}


@Composable
fun MyApp() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var currentLanguage by remember { mutableStateOf("ru") }
           Row (
               modifier = Modifier
                   .fillMaxWidth()
           ) {


               LocalizedContextProvider(languageCode = currentLanguage) {
                   MyScreen(onLanguageChange = { newLanguage ->
                       currentLanguage = newLanguage
                   })
               }
           }
           Row (
               modifier = Modifier
                   .fillMaxWidth()
           ){
               NotificationHandler()
           }

       }

    }
}





@Composable
fun MyScreen(onLanguageChange: (String) -> Unit) {
    val context = LocalLocalizedContext.current
    val exampleString = context.getString(R.string.app_name)

    Column {
        Text(text = exampleString)
        Button(onClick = { onLanguageChange("de") }) {
            Text(text = "Change to DE")
        }
        Button(onClick = { onLanguageChange("en") }) {
            Text(text = "Change to English")
        }
    }
}

@Composable
fun NotificationHandler() {
    var notification by remember { mutableStateOf("No notifications") }

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                notification = "Fetching FCM registration token failed"
                return@addOnCompleteListener
            }

            val token = task.result
            notification = "FCM Registration Token: $token"
        }
    }

    Text(text = notification)
}

