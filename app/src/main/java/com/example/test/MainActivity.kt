package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.test.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import com.example.test.databinding.ActivityColdBinding
import com.google.zxing.integration.android.IntentIntegrator


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val group = Intent(this, GroupActivity::class.java)
        binding.group.setOnClickListener { startActivity(group) }

        val mypage = Intent(this, MypageActivity::class.java)
        binding.mypage.setOnClickListener { startActivity(mypage) }

        val upload = Intent(this, UploadActivity::class.java)
        binding.upload.setOnClickListener { startActivity(upload) }

        initView()
    }

    private fun initView() {
        val viewPager = binding.swipe
        val tabLayout = binding.tapview
        val tabtitle = arrayOf("냉장고", "냉동고", "실온")

        val fragmentList = ArrayList<Fragment>()
        fragmentList.add(ColdActivity())
        fragmentList.add(FrozenActivity())
        fragmentList.add(RoomActivity())

        viewPager.adapter = ViewPagerAdapter(fragmentList, this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabtitle[position]
        }.attach()
    }


    private fun initSearchView() {
        // init SearchView
        binding.search.isSubmitButtonEnabled = true
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // @TODO
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // @TODO
                return true
            }
        })
    }

    var pressedTime : Long=0
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - pressedTime <= 2000) {
            finish()
        } else {
            pressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun start(view: View) {
        IntentIntegrator(this).initiateScan()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            // 스캔 결과를 ResultActivity로 전달
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("SCAN_RESULT", result.contents)
            startActivity(intent)
        }
    }
}