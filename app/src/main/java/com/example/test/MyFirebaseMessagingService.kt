package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val TAG = "FirebaseService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirebase(token)
    }
    override fun onMessageReceived(message: RemoteMessage) {

        if (message.data.isNotEmpty()) {
            sendNotification(message.data["title"].toString(), message.data["body"].toString())
        } else if (message.notification != null) {
            sendNotification(message.notification?.title.toString(), message.notification?.body.toString())
        } else {
            Log.i("data값", message.data.toString())
        }

    }

    private fun saveTokenToFirebase(token: String) {
        val userId = "user123"
        val database = Firebase.database
        val tokenRef = database.getReference("tokens").child(userId)
        tokenRef.setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "Token saved to Firebase Database")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving token to Firebase Database", e)
            }
    }

    private fun sendNotification(title: String?, body: String?) {
        val uniID: Int = (System.currentTimeMillis() / 7).toInt()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, uniID, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelID = getString(R.string.firebase_notification_channel_id)

        val notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.intro)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager, channelID)
        }
        notificationManager.notify(uniID, notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager, channelID: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "소비기한 알림"
            val channelDescription = "소비기한이 임박한 상품에 대한 알림 채널"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}


