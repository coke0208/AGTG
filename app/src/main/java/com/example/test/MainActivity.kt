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
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.productutils.ProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.test.productinfo.ProductDB as ProductDB

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<ProductDB>
    private lateinit var databaseReference: DatabaseReference
    private var groupId: String? = null

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        productAdapter = ProductAdapter(this, productList, "products")
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("users")

        groupId = intent.getStringExtra("GROUP_ID")
        groupId?.let { loadGroupProducts(it) }

        homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, homeFragment).commit()

        setOnQueryTextListener()

        val mypage = Intent(this, MypageActivity::class.java)
        binding.mypage.setOnClickListener { startActivity(mypage) }

        val upload = Intent(this, UploadActivity::class.java)
        binding.upload.setOnClickListener { startActivity(upload) }

        val group = Intent(this, GroupActivity::class.java)
        binding.group.setOnClickListener { startActivity(group) }

        val product1 = Intent(this, ProductActivity::class.java)
        binding.add.setOnClickListener { startActivity(product1) }

        val receivedProductList = intent.getParcelableArrayListExtra<ProductDB>("PRODUCT_LIST")
        receivedProductList?.let {
            productList.clear()
            productList.addAll(it)
            productAdapter.notifyDataSetChanged()
        }
        //푸시알림
        createNotificationChannel()
        requestNotificationPermission()
    }

    private fun loadGroupProducts(groupId: String) {
        val groupProductsReference = FirebaseDatabase.getInstance("https://sukbinggotest-default-rtdb.firebaseio.com/")
            .getReference("groups").child(groupId).child("products")

        groupProductsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductDB::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                productAdapter.updateList(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load group products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

    //푸시알림
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
        val fragments = (homeFragment.childFragmentManager.fragments)
        fragments.forEach { fragment ->
            if (fragment is HomeFragment.SearchableFragment) {
                fragment.updateSearchQuery(newText ?: "")
            }
        }
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
