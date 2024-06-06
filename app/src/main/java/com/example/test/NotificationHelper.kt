package com.example.test

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationHelper {
    fun sendExpiryNotification(context: Context, productName: String) {
        val builder = NotificationCompat.Builder(context, "expiry_notification_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("석빙고")
            .setContentText("냉장고에 있는 ${productName}의 남은 소비기간이 7일 남았습니다")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(productName.hashCode(), builder.build())
    }
}
