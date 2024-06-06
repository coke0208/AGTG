package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.example.test.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private var isSearchViewOpen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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