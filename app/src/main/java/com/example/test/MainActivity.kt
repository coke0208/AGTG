package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.databinding.ActivityMainBinding
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
//import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    //private lateinit var adapter: ProductAdapter

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = getSharedPreferences("token", Context.MODE_PRIVATE)
        val token = pref.getString("token", "")

        // 로그로 토큰 출력
        Log.i("!!!!!!!!", token ?: "Token not found")


        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, homeFragment).commit()

        setOnQueryTextListener()

        scheduleExpiryCheckWork()

        val mypage = Intent(this, MypageActivity::class.java)
        binding.mypage.setOnClickListener { startActivity(mypage) }

        val upload = Intent(this, UploadActivity::class.java)
        binding.upload.setOnClickListener { startActivity(upload) }

        val group = Intent(this, GroupActivity::class.java)
        binding.group.setOnClickListener { startActivity(group) }

        val product1 = Intent(this, ProductActivity::class.java)
        binding.add.setOnClickListener { startActivity(product1) }

        FirebaseApp.initializeApp(this)

    }


    private fun setOnQueryTextListener() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.search.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                updateItemCurrentFragment(newText)
                return false
            }
        })
    }

    private fun updateItemCurrentFragment(newText: String?) {
        val fragments = (homeFragment.childFragmentManager.fragments)
        fragments.forEach { fragment ->
            if (fragment is HomeFragment.SearchableFragment) {
                fragment.updateSearchQuery(newText ?: "")
            }
        }
    }

    private fun scheduleExpiryCheckWork() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
            //.setInitialDelay(30, TimeUnit.SECONDS) // 처음 1분 후 실행
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ExpiryCheckWorker>().build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiryCheckWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )

        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
    }



    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val outRect = android.graphics.Rect()
            currentFocus?.getGlobalVisibleRect(outRect)
            if (!outRect.contains(ev?.rawX?.toInt() ?: 0, ev?.rawY?.toInt() ?: 0)) {
                currentFocus?.clearFocus()
                binding.search.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    var pressedTime: Long = 0

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressedTime <= 2000) {
            finish()
        } else {
            pressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}