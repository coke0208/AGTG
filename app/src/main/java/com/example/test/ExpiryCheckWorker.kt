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
        Log.d("ExpiryCheckWorker", "ExpiryCheckWorker doWork() 시작됨")

        // 3개의 스토리지를 비동기로 확인하기 위해 CountDownLatch 사용
        val latch = CountDownLatch(3) // ColdStorage, FrostStorage, RoomStorage 3개의 노드를 확인

        var result: Result = Result.success()

        // 각 스토리지의 소비기한을 체크
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

        // 모든 작업이 완료될 때까지 대기
        latch.await()
        return result
    }

    private fun checkExpiryDates(storageType: String, callback: (Result) -> Unit) {
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Log.d("ExpiryCheckWorker", "$storageType 의 소비기한을 확인합니다. 오늘: $today")

        // Firebase 데이터베이스 레퍼런스 설정
        val database = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
        val storageRef = database.getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("products").child(storageType)

        storageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ExpiryCheckWorker", "$storageType 데이터 스냅샷을 받았습니다. 개수: ${snapshot.childrenCount}")

                // 각 아이템의 소비기한 확인
                snapshot.children.forEach { childSnapshot ->
                    val item = childSnapshot.getValue(StorageItem::class.java)
                    val expiryDate = item?.usebydate?.toDate()?.time ?: 0

                    if (expiryDate != 0L) {
                        // 남은 일 수 계산
                        val daysLeft = ceil(((expiryDate - today.time).toDouble() / (1000 * 60 * 60 * 24))).toInt()
                        // 남은 일 수가 15, 7, 1, 0일 중 하나일 때만 알림을 보냄
                        if (daysLeft in listOf(14,7,1,0)) {
                            Log.d("ExpiryCheckWorker", "아이템 ${item?.name ?: "알 수 없음"}의 소비기한이 $daysLeft 일 남았습니다. 알림을 보냅니다.")
                            sendNotification(item?.name ?: "알 수 없음", daysLeft, storageType)
                        }
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

    private fun sendNotification(itemName: String, daysLeft: Int, storageType: String) {
        // 고유한 알림 ID 생성
        val uniID = ("${itemName.hashCode()}_${daysLeft}_$storageType").hashCode()

        // 알림 클릭 시 앱을 열기 위한 인텐트 설정
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

        // 알림 빌더 설정
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("소비기한 알림")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // 알림 매니저 설정
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android Oreo 이상 버전에서는 알림 채널을 설정해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "소비기한 알림"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, channelName, importance).apply {
                description = "소비기한이 얼마 남지 않은 아이템에 대한 알림."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 기존 알림 취소
        notificationManager.cancel(uniID)
        // 알림 전송
        notificationManager.notify(uniID, notificationBuilder.build())
    }
}

// Firebase 데이터베이스에서 사용되는 데이터 클래스
data class StorageItem(
    val address: String? = null,
    val checked: Boolean? = null,
    val id: String? = null,
    val info: String? = null,
    val name: String? = null,
    val usebydate: String? = null
)

// 문자열을 Date 객체로 변환하는 확장 함수
fun String.toDate(): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}
