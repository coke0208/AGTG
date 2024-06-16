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

        Log.i("로그: ", "성공적으로 토큰을 저장함:$token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "From: ${message.from}")

        Log.d(TAG, "From: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.i("바디: ", message.data["body"].toString())
            Log.i("타이틀: ", message.data["title"].toString())
            sendNotification(message.data["title"].toString(), message.data["body"].toString())
        } else if (message.notification != null) {
            // notification 필드가 비어 있지 않으면, 이를 사용하여 알림을 보냅니다.
            Log.i("바디: ", message.notification?.body.toString())
            Log.i("타이틀: ", message.notification?.title.toString())
            sendNotification(message.notification?.title.toString(), message.notification?.body.toString())
        } else {
            Log.i("수신에러: ", "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Log.i("data값", message.data.toString())
        }

        /*if(message.data.isNotEmpty()){
            Log.i("바디: ", message.data["body"].toString())
            Log.i("타이틀: ", message.data["title"].toString())
            sendNotification(message)
        }
        else{
            Log.i("수신에러: ", "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Log.i("data값", message.data.toString())
        }*/
    }

    private fun saveTokenToFirebase(token: String) {
        // 사용자 ID를 가져온다고 가정하고 여기에 사용자 ID를 넣어서 사용
        val userId = "user123" // 예시로 고정된 사용자 ID

        // 파이어베이스 데이터베이스에 사용자의 토큰을 저장
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

    private fun sendNotification(title: String?, body: String?) {//remoteMessage: String, toString: String){
        val uniID: Int = (System.currentTimeMillis() / 7).toInt()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, uniID, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelID = getString(R.string.firebase_notification_channel_id)

        val notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.intro)
            .setContentTitle(title)//.data["body"].toString())
            .setContentText(body)//.data["title"].toString())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager, channelID)
            //val channel = NotificationChannel(channelID, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            //notificationManager.createNotificationChannel(channel)
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


