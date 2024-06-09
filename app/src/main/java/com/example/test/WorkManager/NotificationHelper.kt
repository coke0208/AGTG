package com.example.test.WorkManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.test.R

object NotificationHelper {
    fun sendExpiryNotification(context: Context, productName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("expiry_channel", "Expiry Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "expiry_channel")
            .setContentTitle("석빙고")
            .setContentText("냉장고에 ${productName}의 남은 소비기한이 7일 남았습니다.")
            .setSmallIcon(R.drawable.logo)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
