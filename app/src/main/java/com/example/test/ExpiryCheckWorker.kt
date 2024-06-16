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

class ExpiryCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("ExpiryCheckWorker", "ExpiryCheckWorker doWork() 시작됨")

        val latch = CountDownLatch(3) // ColdStorage, FrostStorage, RoomStorage 3개의 노드를 확인

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
        val sevenDaysLater = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        Log.d("ExpiryCheckWorker", "$storageType 의 소비기한을 확인합니다. 오늘: $today, 7일 후: $sevenDaysLater")

        val database = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
        val storageRef = database.getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("products").child(storageType)

        storageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ExpiryCheckWorker", "$storageType 데이터 스냅샷을 받았습니다. 개수: ${snapshot.childrenCount}")

                snapshot.children.forEach { childSnapshot ->
                    val item = childSnapshot.getValue(StorageItem::class.java)
                    val expiryDate = item?.prod?.toDate()?.time ?: 0

                    if (expiryDate != 0L && expiryDate <= sevenDaysLater) {
                        Log.d("ExpiryCheckWorker", "아이템 ${item?.name ?: "알 수 없음"}의 소비기한이 7일 이내입니다. 알림을 보냅니다.")
                        sendNotification(item?.name ?: "알 수 없음", expiryDate)
                    }
                }
                callback(Result.success())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExpiryCheckWorker", "$storageType 데이터베이스 읽기 취소됨. 에러: ${error.message}")
                callback(Result.failure())
            }
        })
    }

    private fun sendNotification(itemName: String, expiryDate: Long) {
        val uniID: Long = System.currentTimeMillis() / 7

        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, uniID.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelID = "expiry_notification_channel"

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("소비기한 알림")
            .setContentText("$itemName 의 소비기한이 7일 이내입니다.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "소비기한 알림"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, channelName, importance).apply {
                description = "소비기한이 7일 이내인 아이템에 대한 알림."
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(uniID.toInt(), notificationBuilder.build())
    }
}

data class StorageItem(
    val address: String? = null,
    val checked: Boolean? = null,
    val id: String? = null,
    val info: String? = null,
    val name: String? = null,
    val prod: String? = null
)

fun String.toDate(): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

