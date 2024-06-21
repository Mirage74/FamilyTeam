package com.balex.familyteam

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Обработка сообщений FCM
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendNotification(messageBody: String?) {
        // Реализуйте этот метод для отображения уведомления
    }

    private fun sendRegistrationToServer(token: String?) {
        // Реализуйте этот метод для отправки токена на ваш сервер
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        //token e5Ybgaa4TqWH6r5H-YeTW_:APA91bGmcp04a36ukmHK3AU7Zk2sDy5blMxVCZOMsDasTrzP4aQ8GDzDHxrlTld3NaO7TqPTdYLj1Ari0BcEE4sAdhd-6AJXlpOesffIywgzDgp8ep6PlteJewoglwF5cPX6zsCrt4FY
    }
}
