package com.balex.familyteam

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
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

    private fun showNotification(title: String?, message: String?) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "default_channel_id"
//
//        // Создаем NotificationChannel для API 26+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Default Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // Создаем намерение для открытия приложения при нажатии на уведомление
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        // Создаем и показываем уведомление
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSmallIcon(R.drawable.ic_notification) // Убедитесь, что у вас есть иконка уведомления
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(0, notification)
//    }
}

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        //token e5Ybgaa4TqWH6r5H-YeTW_:APA91bGmcp04a36ukmHK3AU7Zk2sDy5blMxVCZOMsDasTrzP4aQ8GDzDHxrlTld3NaO7TqPTdYLj1Ari0BcEE4sAdhd-6AJXlpOesffIywgzDgp8ep6PlteJewoglwF5cPX6zsCrt4FY
    }
}
