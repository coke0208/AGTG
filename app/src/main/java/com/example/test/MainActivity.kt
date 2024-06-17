package com.example.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.test.databinding.ActivityMainBinding
import com.example.test.productinfo.ProductDB
import com.example.test.productutils.ProductAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<ProductDB>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var targetUserId: String

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



        targetUserId = intent.getStringExtra("TARGET_USER_ID") ?: FirebaseAuth.getInstance().currentUser!!.uid

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        productAdapter = ProductAdapter(this, productList, "products",targetUserId)
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users")


        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, homeFragment).commit()

        setOnQueryTextListener()

        scheduleExpiryCheckWork()


        binding.mypage.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        binding.upload.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.group.setOnClickListener {
            startActivity(Intent(this, GroupActivity::class.java))
        }

        binding.add.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            intent.putExtra("TARGET_USER_UID", targetUserId)
            startActivity(intent)
        }


        val targetUserId = intent.getStringExtra("TARGET_USER_ID")
        targetUserId?.let {
            loadUserProducts(it)
        }

        createNotificationChannel()
        requestNotificationPermission()
        FirebaseApp.initializeApp(this)
    }

    private fun loadUserProducts(userId: String) {
        val userProductsReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users").child(userId).child("products")

        userProductsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<ProductDB>()
                for (storageSnapshot in snapshot.children) {
                    for (productSnapshot in storageSnapshot.children) {
                        val product = productSnapshot.getValue(ProductDB::class.java)
                        product?.let { products.add(it) }
                    }
                }
                setupRecyclerView(products)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load user products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupRecyclerView(products: List<ProductDB>) {
        productAdapter.updateList(products as ArrayList<ProductDB>)
    }



    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ExpiryNotificationChannel"
            val descriptionText = "Channel for expiry notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("expiry_notification_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
        val fragments = homeFragment.childFragmentManager.fragments
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
