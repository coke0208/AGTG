package com.example.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.ceil

class ExpiryCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val latch = CountDownLatch(3)

        var result: Result = Result.success()

        checkExpiryDates("ColdStorage") {
            result = it
            latch.countDown()
        }
        checkExpiryDates("FrostStorage") {
            result = it
            latch.countDown()
        }
        checkExpiryDates("RoomStorage") {
            result = it
            latch.countDown()
        }

        latch.await()
        return result
    }

    private fun checkExpiryDates(storageType: String, callback: (Result) -> Unit) {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val database = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
        val storageRef = database.getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("products").child(storageType)

        storageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val item = childSnapshot.getValue(StorageItem::class.java)
                    val expiryDate = item?.usebydate?.toDate()?.time ?: 0

                    if (expiryDate != 0L) {
                        val daysLeft = ceil(((expiryDate - today.time).toDouble() / (1000 * 60 * 60 * 24))).toInt()
                        if (daysLeft in listOf(14,7,1,0)) {
                            sendNotification(item?.name ?: "알 수 없음", daysLeft, storageType)
                        }
                    }
                }
                callback(Result.success())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Result.failure())
            }
        })
    }

    private fun sendNotification(itemName: String, daysLeft: Int, storageType: String) {
        val uniID = ("${itemName.hashCode()}_${daysLeft}_$storageType").hashCode()
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, uniID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelID = "expiry_notification_channel"

        val contentText = if (daysLeft == 0) {
            "$itemName 의 소비기한이 만료되었습니다."
        } else {
            "$itemName 의 소비기한이 $daysLeft 일 남았습니다."
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("소비기한 알림")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "소비기한 알림"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, channelName, importance).apply {
                description = "소비기한이 얼마 남지 않은 아이템에 대한 알림."
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.cancel(uniID)
        notificationManager.notify(uniID, notificationBuilder.build())
    }
}

data class StorageItem(
    val address: String? = null,
    val checked: Boolean? = null,
    val id: String? = null,
    val info: String? = null,
    val name: String? = null,
    val usebydate: String? = null
)

fun String.toDate(): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}
