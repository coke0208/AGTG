package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.test.databinding.ActivityColdBinding
import com.example.test.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager
    private lateinit var transaction: FragmentTransaction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mypage = Intent(this, MypageActivity::class.java)
        binding.mypage.setOnClickListener { startActivity(mypage) }

        val upload = Intent(this, UploadActivity::class.java)
        binding.upload.setOnClickListener { startActivity(upload) }

        val group = Intent(this, GroupActivity::class.java)
        binding.group.setOnClickListener { startActivity(group) }

        val product1 = Intent(this, ProductActivity::class.java)
        binding.add.setOnClickListener { startActivity(product1) }

        // 2. Main Fragment 설정
        transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.frameLayout, HomeFragment())
        transaction.commit()
    }

    private fun setOnQueryTextListener() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                updateItemCurrentFragment(newText)
                return false
            }
        })
    }
    private fun updateItemCurrentFragment(newText: String?) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        if (currentFragment is SearchableFragment) {
            currentFragment.updateSearchQuery(newText ?: "")
        }
    }

    interface SearchableFragment {
        fun updateSearchQuery(query: String)
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